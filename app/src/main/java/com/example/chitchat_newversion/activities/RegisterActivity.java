package com.example.chitchat_newversion.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.chitchat_newversion.R;
import com.example.chitchat_newversion.databinding.ActivityLoginBinding;
import com.example.chitchat_newversion.databinding.ActivityRegisterBinding;
import com.example.chitchat_newversion.utilities.Constants;
import com.example.chitchat_newversion.utilities.PreferenceManger;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private PreferenceManger preferenceManger;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManger = new PreferenceManger(getApplicationContext());

        setListeners();
    }

    private void setListeners()
    {
        binding.textLogin.setOnClickListener(v -> onBackPressed());
        binding.RegisterButton.setOnClickListener(v -> {
            if(isValidRegisterDetails())
            {
                register();
            }
        });

        // once click image, then open mediastore in android
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            selectImage.launch(intent);
        });

        binding.checkbox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                {
                    binding.inputPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    binding.inputPasswordAgain.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else
                {
                    binding.inputPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    binding.inputPasswordAgain.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
    }

    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void register()
    {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManger.putBoolean(Constants.KEY_IS_LOGINED, true);
                    // documentReference -> document location in firestore database
                    preferenceManger.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManger.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                    preferenceManger.putString(Constants.KEY_IMAGE, encodedImage);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                }).addOnFailureListener(exception ->{
                    loading(false);
                    showToast(exception.getMessage());
        });
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
                            binding.userImage.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodedImage(bitmap);
                        }
                        catch (FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }

                    }
                }
            }

    );

    private Boolean isValidRegisterDetails()
    {
        if(encodedImage == null)
        {
            showToast("Please select one profile image!");
            return false;
        }
        else if(binding.inputName.getText().toString().trim().isEmpty())
        {
            showToast("Please input your username!");
            return false;
        }
        else if(!isNameRegax(binding.inputName.getText().toString()))
        {
            showToast("The total number of characters of username is limited to 6-10, it must start with a letter, and allow letters, digits and underscores");
            return false;
        }
        else if(binding.inputEmail.getText().toString().trim().isEmpty())
        {
            showToast("Please input email!");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches())
        {
            showToast("Your email format is not correct!!");
            return false;
        }
        else if(binding.inputPassword.getText().toString().trim().isEmpty())
        {
            showToast("Please input password!");
            return false;
        }
        else if(!isPasswordRegax(binding.inputPassword.getText().toString()))
        {
            showToast("The total number of characters of password is limited to 6-20, only allows letters and digits");
            return false;
        }
        else if(binding.inputPasswordAgain.getText().toString().trim().isEmpty())
        {
            showToast("Please input password again!");
            return false;
        }
        else if(!binding.inputPassword.getText().toString().equals(binding.inputPasswordAgain.getText().toString()))
        {
            showToast("Passwords you entered twice should be the same");
            return false;
        }
        else
        {
            return true;
        }
    }

    // verify the format of username
    // the total number of characters is limited to 6-10, it must start with a letter
    // and allow letters, digits and underscores

    private Boolean isNameRegax(String name)
    {
        String namePattern = "^[a-zA-Z][\\w_]{5,9}$";
        return Pattern.matches(namePattern, name);
    }

    // verify the format of password
    // the total number of characters is limited to 6-20, only allows letters and digits
    private Boolean isPasswordRegax(String password)
    {
        String passwordPattern = "^[\\w]{6,20}$";
        return Pattern.matches(passwordPattern, password);
    }

    private void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            binding.RegisterButton.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.RegisterButton.setVisibility(View.VISIBLE);
        }
    }

}