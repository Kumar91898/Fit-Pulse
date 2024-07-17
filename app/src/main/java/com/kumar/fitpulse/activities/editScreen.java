package com.kumar.fitpulse.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kumar.fitpulse.R;
import com.kumar.fitpulse.databinding.ActivityEditScreenBinding;

import java.util.HashMap;

public class editScreen extends AppCompatActivity {
    ActivityEditScreenBinding binding;
    String name, url, email;
    Double height, weight;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        name = getIntent().getStringExtra("name");
        email = getIntent().getStringExtra("email");
        url = getIntent().getStringExtra("url");
        height = getIntent().getDoubleExtra("height", 0);
        weight = getIntent().getDoubleExtra("weight", 0);

        binding.nameFieldEdit.setText(name);
        binding.heightFieldEdit.setText(String.valueOf(height));
        binding.weightFieldEdit.setText(String.valueOf(weight));

        binding.changeImage.setOnClickListener(v -> {
            selectImage();
        });

        binding.editButton.setOnClickListener(v -> {
            binding.buttonAnimationEdit.setVisibility(View.VISIBLE);
            binding.buttonAnimationEdit.playAnimation();

            binding.buttonTextEdit.setVisibility(View.GONE);
            binding.editButton.setEnabled(false);

            handleUpdateButton();
        });

        loadImage(url);
    }

    private void resetButton() {
        binding.buttonAnimationEdit.pauseAnimation();
        binding.buttonAnimationEdit.setVisibility(View.GONE);

        binding.buttonTextEdit.setVisibility(View.VISIBLE);
        binding.editButton.setEnabled(true); // Re-enable the edit button
    }

    private void handleUpdateButton() {
        String newName = binding.nameFieldEdit.getText().toString();
        String newHeight = binding.heightFieldEdit.getText().toString();
        String newWeight = binding.weightFieldEdit.getText().toString();

        // Check if there's a new image to upload
        if (imageUri != null) {
            uploadImageToStorage(newName, newHeight, newWeight);
        } else {
            updateFirestoreWithoutImage(newName, newHeight, newWeight);
        }
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
            binding.profileEdit.setImageURI(imageUri);
        }
    }

    private void uploadImageToStorage(String newName, String newHeight, String newWeight) {
        String fileName = newName; // Use the new name as the filename or identifier

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("profiles/" + fileName);
        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageURL = uri.toString();
                        updateFirestoreWithImage(newName, newHeight, newWeight, imageURL);
                    });

                    Toast.makeText(this, "Successfully uploaded!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload!", Toast.LENGTH_SHORT).show();
                    resetButton();
                });
    }

    private void updateFirestoreWithImage(String newName, String newHeight, String newWeight, String imageURL) {
        Double height = Double.parseDouble(newHeight);
        Double weight = Double.parseDouble(newWeight);

        HashMap<String, Object> newRecord = new HashMap<>();
        newRecord.put("name", newName);
        newRecord.put("height", height);
        newRecord.put("weight", weight);
        newRecord.put("ImageUrl", imageURL);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("email", email) // Use the original name to find the document
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        String documentID = documentSnapshot.getId();
                        db.collection("users")
                                .document(documentID)
                                .update(newRecord)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Successfully Uploaded!", Toast.LENGTH_SHORT).show();
                                    resetButton();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to upload!", Toast.LENGTH_SHORT).show();
                                    resetButton();
                                });
                    } else {
                        Toast.makeText(editScreen.this, "Document not found in Firestore!",
                                Toast.LENGTH_SHORT).show();
                        resetButton();
                    }
                });
    }

    private void updateFirestoreWithoutImage(String newName, String newHeight, String newWeight) {
        Double height = Double.parseDouble(newHeight);
        Double weight = Double.parseDouble(newWeight);
        
        HashMap<String, Object> newRecord = new HashMap<>();
        newRecord.put("name", newName);
        newRecord.put("height", height);
        newRecord.put("weight", weight);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("email", email) // Use the original name to find the document
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        String documentID = documentSnapshot.getId();
                        db.collection("users")
                                .document(documentID)
                                .update(newRecord)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Successfully Uploaded!", Toast.LENGTH_SHORT).show();
                                    resetButton();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to upload!", Toast.LENGTH_SHORT).show();
                                    resetButton();
                                });
                    } else {
                        Toast.makeText(this, "Document not found!", Toast.LENGTH_SHORT).show();
                        resetButton();
                    }
                });
    }

    private void loadImage(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.profile) // Placeholder image while loading
                .error(R.drawable.profile) // Image to show if loading fails
                .into(binding.profileEdit);
    }
}