package com.example.chitchat_newversion.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
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
    }

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
}