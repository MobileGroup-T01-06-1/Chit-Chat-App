package com.example.chitchat_newversion.activities;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.chitchat_newversion.adapters.ChatAdapter;
import com.example.chitchat_newversion.databinding.ActivityChatBinding;
import com.example.chitchat_newversion.models.ChatMessage;
import com.example.chitchat_newversion.models.Users;
import com.example.chitchat_newversion.network.APIClient;
import com.example.chitchat_newversion.network.APIService;
import com.example.chitchat_newversion.utilities.Constants;
import com.example.chitchat_newversion.utilities.PreferenceManger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ActivityChatBinding binding;
    private Users receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManger preferenceManger;
    private FirebaseFirestore database;
    private String conversionId = null;
    private Boolean isReceivedAvailable = false;


    private Boolean isMainFabVisible, isSubFabVisible;

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
        if(encodedImage != null)
        {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        else
        {
            return null;
        }

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
        if(!binding.inputMessage.getText().toString().equals(""))
        {
            HashMap<String, Object> message = new HashMap<>();
            message.put(Constants.KEY_SENDER_ID, preferenceManger.getString(Constants.KEY_USER_ID));
            message.put(Constants.KEY_RECEIVED_ID, receiverUser.id);
            message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
            //the key that distinguish whether it is image.
            message.put(Constants.KEY_MESSAGE_TYPE,"1");
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
        }

        if(!isReceivedAvailable)
        {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);
                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManger.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManger.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManger.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
                sendNotification(body.toString());
            }
            catch (Exception exception)
            {
                showToast(exception.getMessage());
            }
        }
        binding.inputMessage.setText(null);
    }

    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody)
    {
        APIClient.getClient().create(APIService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful())
                {
                    try {
                        if(response.body() != null)
                        {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results:");
                            if(responseJson.getInt("failure") == 1)
                            {
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                    showToast("Notification sent successfully");
                }
                else
                {
                    showToast("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());

            }
        });
    }


    private void listenAvailabilityOfReceiver()
    {
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if(error != null)
            {
                return;
            }
            if(value != null)
            {
                if(value.getLong(Constants.KEY_AVAILABILITY) != null)
                {
                    int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)).intValue();
                    isReceivedAvailable = availability == 1;
                }
                receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                if(receiverUser.image == null)
                {
                    receiverUser.image = value.getString(Constants.KEY_IMAGE);
                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
                    chatAdapter.notifyItemChanged(0, chatMessages.size());
                }
            }
            if(isReceivedAvailable)
            {
                binding.textAvailability.setVisibility(View.VISIBLE);
            }
            else
            {
                binding.textAvailability.setVisibility(View.GONE);
            }
        });
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

    private void sendImages(String encodedImage)
    {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManger.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVED_ID, receiverUser.id);
        //the key that distinguish whether it is image
        message.put(Constants.KEY_MESSAGE_TYPE,"2");
        message.put(Constants.KEY_MESSAGE, encodedImage);
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
            conversion.put(Constants.KEY_LAST_MESSAGE, "[Image]");
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        binding.inputMessage.setText(null);
    }


    // read the image from phone
    // first read image into buffer stream as byte array
    // then compress the picture as jpeg format
    // then convert byte array to string format
    private String encodedImage(Bitmap bitmap)
    {
        int width = 150;

        // get height of images
        int height = bitmap.getHeight() * width / bitmap.getWidth();

        // create a new scales picture according to the original large picture
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, width, height,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // https://www.jianshu.com/p/7096fd250c0d
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }


    private final ActivityResultLauncher<Intent> selectImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK)
                {
                    if(result.getData() != null)
                    {
                        Uri imageUri = result.getData().getData();
                        try
                        {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            String encodedImage = encodedImage(bitmap);
                            sendImages(encodedImage);
                        }
                        catch (FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }

                    }
                }
            }

    );


    private final ActivityResultLauncher<Void> takephoto = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(), result -> {
                sendImages(encodedImage(result));
            });



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
                  //MESSAGE_IMAGE value
                  chatMessage.message_type = Integer.parseInt(documentChange.getDocument().getString(Constants.KEY_MESSAGE_TYPE));
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

    //all the onclick actions
    private void setListeners()
    {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
        binding.imageInfo.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), UserProfileActivity.class)));
        //map listener
        binding.sideLocation.setOnClickListener(v -> sendLocation());
        binding.camera.setOnClickListener(v -> {
            takephoto.launch(null);
        });
        binding.sideImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            selectImage.launch(intent);});
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
            if (lastknownlocation != null) {
                String currentposition = "https://www.google.com/maps/place/" + lastknownlocation.getLatitude() + "," + lastknownlocation.getLongitude();
                binding.inputMessage.setText(currentposition);
                sendMessage();
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

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }

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
        binding.sideImage.setOnClickListener(view ->{
            subFabHide();
            mainFabHide();
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            selectImage.launch(intent);
        });
        binding.sideLocation.setOnClickListener(view ->{
            subFabHide();
            mainFabHide();
            sendLocation();
        });
        binding.sideFile.setOnClickListener(view ->{
            subFabHide();
            mainFabHide();
        });
        binding.camera.setOnClickListener(view ->{
            subFabHide();
            mainFabHide();
            takephoto.launch(null);
        });
        binding.phoneCall.setOnClickListener(view ->{
            subFabHide();
            mainFabHide();
        });
        binding.videoCall.setOnClickListener(view ->{
            //Call the agora Application
            Intent intent = new Intent(Intent.ACTION_MAIN);
            ComponentName componentName = new ComponentName("io.agora.openvcall","io.agora.openvcall.ui.MainActivity");
            intent.setComponent(componentName);
            startActivity(intent);
            subFabHide();
            mainFabHide();
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