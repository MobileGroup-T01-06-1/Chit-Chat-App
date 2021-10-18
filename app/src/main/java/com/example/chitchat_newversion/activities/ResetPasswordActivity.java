package com.example.chitchat_newversion.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chitchat_newversion.databinding.ActivityForgotpasswordBinding;
import com.example.chitchat_newversion.databinding.ActivityResetpasswordBinding;
import com.example.chitchat_newversion.models.Users;
import com.example.chitchat_newversion.utilities.Constants;
import com.example.chitchat_newversion.utilities.PreferenceManger;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;
import java.util.regex.Pattern;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetpasswordBinding binding;
    private PreferenceManger preferenceManger;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetpasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManger = new PreferenceManger(getApplicationContext());

        setListeners();
    }

    private void setListeners()
    {
        binding.returnButton.setOnClickListener(v -> onBackPressed());
        binding.confirmButton.setOnClickListener(v -> {
            if (isValidPassword()){
                askSafetyQuestion();
            }
        });

    }

    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    private void askSafetyQuestion()
    {
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManger.getString(Constants.KEY_USER_ID))
                .update(Constants.KEY_PASSWORD,binding.inputNewPassword.getText().toString());
        showToast("Your password has been changed!");
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            binding.confirmButton.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.confirmButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean isValidPassword(){
        if(binding.inputNewPassword.getText().toString().trim().isEmpty())
        {
            showToast("Please input password!");
            return false;
        }
        else if(!isPasswordRegax(binding.inputNewPassword.getText().toString()))
        {
            showToast("The total number of characters of password is limited to 6-20, only allows letters and digits");
            return false;
        }
        else if(binding.inputNewPasswordAgain.getText().toString().trim().isEmpty())
        {
            showToast("Please input password again!");
            return false;
        }
        else if(!binding.inputNewPassword.getText().toString().equals(binding.inputNewPasswordAgain.getText().toString()))
        {
            showToast("Passwords you entered twice should be the same");
            return false;
        }
        return true;
    }

    private Boolean isPasswordRegax(String password)
    {
        String passwordPattern = "^[\\w]{6,20}$";
        return Pattern.matches(passwordPattern, password);
    }

}