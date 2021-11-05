package com.example.chitchat_newversion.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import com.example.chitchat_newversion.databinding.ActivityMainBinding;
import com.example.chitchat_newversion.databinding.ActivityRegisterBinding;
import com.example.chitchat_newversion.databinding.ActivityUserProfileBinding;
import com.example.chitchat_newversion.utilities.Constants;
import com.example.chitchat_newversion.utilities.PreferenceManger;
import com.example.chitchat_newversion.models.Users;

public class UserProfileActivity extends AppCompatActivity {

    private ActivityUserProfileBinding binding;
    private PreferenceManger preferenceManger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManger = new PreferenceManger(getApplicationContext());
        setListeners();
        Intent intent= this.getIntent();
        Users receiverUser = (Users) intent.getSerializableExtra(Constants.KEY_USER);
        binding.userImage.setImageBitmap(getBitmapFromEncodedString(receiverUser.image));
        binding.userEmail.setText(receiverUser.email);
        binding.username.setText(receiverUser.name);
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
    private void setListeners()
    {
        binding.BacktoChat.setOnClickListener(v -> onBackPressed());
    }
}