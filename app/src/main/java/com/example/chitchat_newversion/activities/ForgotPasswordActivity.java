package com.example.chitchat_newversion.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chitchat_newversion.adapters.UsersAdapter;
import com.example.chitchat_newversion.databinding.ActivityForgotpasswordBinding;
import com.example.chitchat_newversion.databinding.ActivityRegisterBinding;
import com.example.chitchat_newversion.models.Users;
import com.example.chitchat_newversion.utilities.Constants;
import com.example.chitchat_newversion.utilities.PreferenceManger;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotpasswordBinding binding;
    private PreferenceManger preferenceManger;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotpasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManger = new PreferenceManger(getApplicationContext());

        setListeners();
    }

    private void setListeners()
    {
        binding.textLogin.setOnClickListener(v -> onBackPressed());
        binding.confirmButton.setOnClickListener(v -> {
            askSafetyQuestion();
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
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0)
                    {
                        // documentSnapshot contains data read from the document in firestore database
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManger.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                        preferenceManger.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManger.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        preferenceManger.putString(Constants.KEY_SQUESTION,documentSnapshot.getString(Constants.KEY_SQUESTION));
                        preferenceManger.putString(Constants.KEY_SANSWER,documentSnapshot.getString(Constants.KEY_SANSWER));
                        Intent intent = new Intent(getApplicationContext(), askSafetyQuestionActivity.class);
                        startActivity(intent);
                    }
                    else
                    {
                        loading(false);
                        showToast("This email has not been registered");
                    }
                });
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

}