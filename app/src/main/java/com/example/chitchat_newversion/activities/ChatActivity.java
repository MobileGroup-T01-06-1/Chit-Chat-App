package com.example.chitchat_newversion.activities;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.chitchat_newversion.R;
import com.example.chitchat_newversion.adapters.ChatAdapter;
import com.example.chitchat_newversion.databinding.ActivityChatBinding;
import com.example.chitchat_newversion.models.ChatMessage;
import com.example.chitchat_newversion.models.Users;
import com.example.chitchat_newversion.utilities.Constants;
import com.example.chitchat_newversion.utilities.PreferenceManger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.common.collect.Collections2;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private Users receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManger preferenceManger;
    private FirebaseFirestore database;
    private String conversionId = null;

    // TODO: 2021/10/20  UI to check whether sub FABs are visible or not & drag
    private Boolean isMainFabVisible, isSubFabVisible, isDrag;
    private int lastX,lastY;
    //THis variable used in side-location
    private static String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
        sideFab();
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage)
    {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void init()
    {
        preferenceManger = new PreferenceManger(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManger.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage()
    {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManger.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVED_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversionId != null)
        {
            updateConversion(binding.inputMessage.getText().toString());
        }
        else
        {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferenceManger.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManger.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManger.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVED_ID, receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        binding.inputMessage.setText(null);
    }

    private void listenMessages()
    {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManger.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVED_ID, receiverUser.id)
                .addSnapshotListener(eventlistener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVED_ID, preferenceManger.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventlistener);
    }




    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventlistener = (value, error) -> {
      if(error != null)
      {
          return;
      }
      if(value != null) {
          int count = chatMessages.size();
          for (DocumentChange documentChange : value.getDocumentChanges()) {
              if (documentChange.getType() == DocumentChange.Type.ADDED) {
                  ChatMessage chatMessage = new ChatMessage();
                  chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                  chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVED_ID);
                  chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                  chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                  chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                  chatMessages.add(chatMessage);
              }
          }

          Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
          if(count == 0)
          {
              chatAdapter.notifyDataSetChanged();
          }
          else
          {
              chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
              binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
          }
          binding.chatRecyclerView.setVisibility(View.VISIBLE);
      }
      binding.progressBar.setVisibility(View.GONE);
      if(conversionId == null)
      {
          checkForConversion();
      }
    };

    private void loadReceiverDetails()
    {
        receiverUser = (Users) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }

    private void setListeners()
    {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
        //map listener
        binding.sideLocation.setOnClickListener(v -> sendLocation());
    }

    private void sendLocation() {
        String serviceString = Context.LOCATION_SERVICE;
        LocationManager locationManager = (LocationManager) getSystemService(serviceString);
        List<String> list = locationManager.getProviders(true);
        if (list.contains(locationManager.GPS_PROVIDER)){
            provider = locationManager.GPS_PROVIDER;
        }
        else if (list.contains(locationManager.NETWORK_PROVIDER)){
            provider = locationManager.NETWORK_PROVIDER;
        }
        if (provider != null){
            if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }
            locationManager.requestLocationUpdates(provider, 2000, 10,locationListener);

            Location lastknownlocation = locationManager.getLastKnownLocation(provider);
            String currentposition = "latitude is " + lastknownlocation.getLatitude()  + "longitude is " + lastknownlocation.getLongitude();
            if (lastknownlocation != null) {
                binding.inputMessage.setText(currentposition);
                Toast.makeText(this, "success", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this, "Please Try again!", Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(this,"No permission",Toast.LENGTH_LONG).show();
            return;
        }
    }

    //THis function used in send location function
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onProviderDisabled(String arg0) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onProviderEnabled(String arg0) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
            // TODO Auto-generated method stub
        }
    };

    private String getReadableDateTime(Date date)
    {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversion(HashMap<String, Object> conversion)
    {
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateConversion(String message)
    {
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }

    private void checkForConversion()
    {
        if(chatMessages.size() != 0)
        {
            checkForConversionRemotely(
                    preferenceManger.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkForConversionRemotely(
                    receiverUser.id,
                    preferenceManger.getString(Constants.KEY_USER_ID)
            );
        }
    }


    private void checkForConversionRemotely(String senderId, String receiverId)
    {
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVED_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
      if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0)
      {
          DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
          conversionId = documentSnapshot.getId();
      }
    };


    private void sideFab() {
        binding.moreFab.setVisibility(View.GONE);
        binding.sideImage.setVisibility(View.GONE);
        binding.sideFile.setVisibility(View.GONE);
        binding.phoneCall.setVisibility(View.GONE);
        binding.videoCall.setVisibility(View.GONE);
        binding.camera.setVisibility(View.GONE);
        binding.sideLocation.setVisibility(View.GONE);
        binding.sideMenu.shrink();
        isMainFabVisible = false;
        isSubFabVisible = false;


        binding.sideMenu.setOnClickListener(view -> {
            if (!isMainFabVisible) {
                mainFabShow();
            }
            else if(isSubFabVisible){
                subFabHide();
                mainFabHide();
            }
            else {
                mainFabHide();
            }
        });
        binding.moreFab.setOnClickListener(view -> {
            if (!isSubFabVisible){
                subFabShow();
            }else{
                subFabHide();
            }
        });
    }
    private void mainFabShow(){
        binding.sideMenu.extend();
        binding.sideImage.show();
        binding.sideFile.show();
        binding.sideLocation.show();
        binding.moreFab.show();
        isMainFabVisible = true;
    }
    private void subFabShow(){
        binding.camera.show();
        binding.phoneCall.show();
        binding.videoCall.show();
        isSubFabVisible = true;
    }
    private void mainFabHide(){
        binding.moreFab.hide();
        binding.sideImage.hide();
        binding.sideFile.hide();
        binding.sideLocation.hide();
        binding.sideMenu.shrink();
        isMainFabVisible = false;
    }
    private void subFabHide(){
        binding.camera.hide();
        binding.phoneCall.hide();
        binding.videoCall.hide();
        isSubFabVisible = false;
    }
}