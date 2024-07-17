package com.kumar.fitpulse.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kumar.fitpulse.R;
import com.kumar.fitpulse.databinding.ActivityRegisterScreenBinding;

import java.util.HashMap;
import java.util.Map;

public class registerScreen extends AppCompatActivity {

    ActivityRegisterScreenBinding binding;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    StorageReference storageReference;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.addImage.setOnClickListener(v -> {
            selectImage();
        });

        binding.loginText.setOnClickListener(v -> {
            binding.loginText.setTextColor(getColor(R.color.orange));
            startActivity(new Intent(this, loginScreen.class));
            finish();
        });

        binding.registerButton.setOnClickListener(v -> {
            if (validateInputFields()){
                binding.buttonAnimationRegister.setVisibility(View.VISIBLE);
                binding.buttonAnimationRegister.playAnimation();

                binding.buttonTextRegister.setVisibility(View.GONE);
                binding.registerButton.setEnabled(false);

                signupWithEmail();
            }
        });
    }

    private boolean validateInputFields() {
        String email = binding.emailFieldRegister.getText().toString();
        String password = binding.passwordFieldRegister.getText().toString();
        String name = binding.nameFieldRegister.getText().toString();
        String height = binding.heightFieldRegister.getText().toString();
        String weight = binding.weightFieldRegister.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Enter email!", Toast.LENGTH_SHORT).show();
            resetButton();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter password!", Toast.LENGTH_SHORT).show();
            resetButton();
            return false;
        }

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Enter name!", Toast.LENGTH_SHORT).show();
            resetButton();
            return false;
        }

        if (TextUtils.isEmpty(height)) {
            Toast.makeText(this, "Enter height!", Toast.LENGTH_SHORT).show();
            resetButton();
            return false;
        }

        if (TextUtils.isEmpty(weight)) {
            Toast.makeText(this, "Enter weight!", Toast.LENGTH_SHORT).show();
            resetButton();
            return false;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Select a profile picture!", Toast.LENGTH_SHORT).show();
            resetButton();
            return false;
        }

        return true;
    }

    public void signupWithEmail(){
        String email = binding.emailFieldRegister.getText().toString();
        String password = binding.passwordFieldRegister.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        resetButton();

                        if (task.isSuccessful()) {
                            uploadImage();
                            moveToHomeScreen();
                        } else {

                            // If sign up fails, display a message to the user.
                            Toast.makeText(registerScreen.this, "Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void upload_data_to_firebase(String imageURL){
        String email = binding.emailFieldRegister.getText().toString();
        String password = binding.passwordFieldRegister.getText().toString();
        String name = binding.nameFieldRegister.getText().toString();
        String height = binding.heightFieldRegister.getText().toString();
        String weight = binding.weightFieldRegister.getText().toString();

        Map<String,Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("password", password);
        user.put("height", Double.parseDouble(height));
        user.put("weight", Double.parseDouble(weight));
        user.put("ImageUrl", imageURL);
        user.put("water", 0);
        user.put("calories", "0");
        user.put("distance", "0");
        user.put("steps", 0);
        user.put("totalHours", "0");

        db.collection("users")
                .add(user);
    }

    private void resetButton() {
        binding.buttonAnimationRegister.pauseAnimation();
        binding.buttonAnimationRegister.setVisibility(View.GONE);

        binding.buttonTextRegister.setVisibility(View.VISIBLE);
        binding.registerButton.setEnabled(true); // Re-enable the login button
    }

    public void moveToHomeScreen(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && data != null && data.getData() != null){
            imageUri = data.getData();
            binding.profileRegister.setImageURI(imageUri);
        }
    }

    private void uploadImage() {
        String fileName = binding.nameFieldRegister.getText().toString();

        storageReference = FirebaseStorage.getInstance().getReference("profiles/" + fileName);
        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageURL = uri.toString();
                                upload_data_to_firebase(imageURL);
                            }
                        });

                        Toast.makeText(registerScreen.this, "Account created!!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(registerScreen.this, "Failed to upload!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}