package com.example.chitchat_newversion.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.example.chitchat_newversion.R;
import com.example.chitchat_newversion.databinding.ActivityMainBinding;
import com.example.chitchat_newversion.utilities.Constants;
import com.example.chitchat_newversion.utilities.PreferenceManger;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PreferenceManger preferenceManger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManger = new PreferenceManger(getApplicationContext());
        loadUserDetails();
        getToken();
        setListeners();
    }

    private void setListeners()
    {
        binding.imagelogout.setOnClickListener(v -> logout());
        binding.fabNewChat.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
    }


    // show users' profile on the main page
    private void loadUserDetails()
    {
        binding.textName.setText(preferenceManger.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManger.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.userImage.setImageBitmap(bitmap);
    }

    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void getToken()
    {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token)
    {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManger.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(unused -> showToast("Welcome to Chit Chat App's main page!"))
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

    private void logout()
    {
        showToast("Logout");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManger.getString(Constants.KEY_USER_ID)
        );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(unused -> {
            preferenceManger.clear();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        }).addOnFailureListener(e -> showToast("Unable to logout"));
    }
}