package com.example.chitchat_newversion.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chitchat_newversion.databinding.ActivityAsksafetyquestionBinding;
import com.example.chitchat_newversion.databinding.ActivityForgotpasswordBinding;
import com.example.chitchat_newversion.utilities.Constants;
import com.example.chitchat_newversion.utilities.PreferenceManger;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class askSafetyQuestionActivity extends AppCompatActivity {

    private ActivityAsksafetyquestionBinding binding;
    private PreferenceManger preferenceManger;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAsksafetyquestionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManger = new PreferenceManger(getApplicationContext());

        setListeners();

        binding.SafetyQuestion.setText(preferenceManger.getString(Constants.KEY_SQUESTION));
    }

    private void setListeners()
    {
        binding.returnButton.setOnClickListener(v -> onBackPressed());
        binding.confirmButton.setOnClickListener(v -> {
            askSaftyQuestion();
        });

    }

    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }



    private void askSaftyQuestion()
    {
        if (preferenceManger.getString(Constants.KEY_SANSWER).equals(binding.inputSafetyAnswer.getText().toString())){
            Intent intent = new Intent(getApplicationContext(),ResetPasswordActivity.class);
            startActivity(intent);
        }else{
            showToast("incorrect answer for the safety question");
        }
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