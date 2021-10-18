package com.example.chitchat_newversion.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.chitchat_newversion.R;
import com.example.chitchat_newversion.databinding.ActivityLoginBinding;
import com.example.chitchat_newversion.utilities.Constants;
import com.example.chitchat_newversion.utilities.PreferenceManger;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    // enable viewBinding for this project
    private ActivityLoginBinding binding;
    private PreferenceManger preferenceManger;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManger = new PreferenceManger(getApplicationContext());

        // sign in status
        if(preferenceManger.getBoolean(Constants.KEY_IS_LOGINED))
        {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        // https://developer.android.google.cn/topic/libraries/view-binding?hl=zh-cn
        // similar with findViewbyid
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners()
    {
        binding.textRegister.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), RegisterActivity.class)));
        binding.LoginButton.setOnClickListener(v -> {
            if(isValidLoginDetails())
            {
                login();
            }
        });
//        binding.LoginButton.setOnClickListener(v -> addDataToFirestore());

        binding.checkbox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                {
                    binding.inputPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else
                {
                    binding.inputPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        binding.ForgetPassword.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class)));
    }

    private void login()
    {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0)
                    {
                        // documentSnapshot contains data read from the document in firestore database
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManger.putBoolean(Constants.KEY_IS_LOGINED, true);
                        preferenceManger.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManger.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManger.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else
                    {
                        loading(false);
                        showToast("can't login successfully");
                    }
                });

    }

    private void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            binding.LoginButton.setVisibility((View.INVISIBLE));
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.LoginButton.setVisibility((View.VISIBLE));

        }
    }

    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidLoginDetails()
    {
        if(binding.inputEmail.getText().toString().trim().isEmpty())
        {
            showToast("Please input your email");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches())
        {
            showToast("You have to input correct email!");
            return false;
        }
        else if(binding.inputPassword.getText().toString().trim().isEmpty())
        {
            showToast("Please input password");
            return false;
        }
        else
        {
            return true;
        }
    }


    // test code, to verify firebase database is set successful or not

//    private void addDataToFirestore()
//    {
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        HashMap<String, Object> data = new HashMap<>();
//        data.put("first name", "yu");
//        data.put("last name", "tianqi");
//        database.collection("users")
//                .add(data)
//                .addOnSuccessListener(documentReference -> {
//                    Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(exception -> {
//                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
//
//                });
//    }

}