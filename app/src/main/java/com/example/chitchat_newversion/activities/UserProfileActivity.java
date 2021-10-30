package com.example.chitchat_newversion.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.chitchat_newversion.databinding.ActivityMainBinding;
import com.example.chitchat_newversion.databinding.ActivityRegisterBinding;
import com.example.chitchat_newversion.databinding.ActivityUserProfileBinding;
import com.example.chitchat_newversion.utilities.PreferenceManger;

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
    }
    private void setListeners()
    {
        binding.BacktoChat.setOnClickListener(v -> onBackPressed());
    }
}