package com.kumar.fitpulse.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.kumar.fitpulse.R;
import com.kumar.fitpulse.databinding.ActivityResetScreenBinding;

public class resetScreen extends AppCompatActivity {
    ActivityResetScreenBinding binding;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String email = binding.emailFieldReset.getText().toString();

        mAuth = FirebaseAuth.getInstance();

        binding.cancelReset.setOnClickListener(v -> {
            onBackPressed();
        });

        binding.resetbuttonReset.setOnClickListener(v -> {
            String getEmail = binding.emailFieldReset.getText().toString();

            if (TextUtils.isEmpty(getEmail)){
                Toast.makeText(this, "Email field is empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.buttonAnimationReset.setVisibility(View.VISIBLE);
            binding.buttonAnimationReset.playAnimation();

            binding.buttonTextReset.setVisibility(View.GONE);
            binding.resetbuttonReset.setEnabled(false);

            sendResetLink(getEmail);
        });
    }

    private void sendResetLink(String email){
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        resetButton();
                        if (task.isSuccessful()){
                            binding.resetText.setVisibility(View.VISIBLE);
                            binding.resetText.setText("Email with reset link is send to "+email);
                            Toast.makeText(resetScreen.this, "Reset link send!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(resetScreen.this, "Failed to send!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void resetButton() {
        binding.buttonAnimationReset.pauseAnimation();
        binding.buttonAnimationReset.setVisibility(View.GONE);

        binding.buttonTextReset.setVisibility(View.VISIBLE);
        binding.resetbuttonReset.setEnabled(true);
    }
}