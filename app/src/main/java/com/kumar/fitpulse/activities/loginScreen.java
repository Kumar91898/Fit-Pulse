package com.kumar.fitpulse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kumar.fitpulse.R;
import com.kumar.fitpulse.databinding.ActivityLoginScreenBinding;

public class loginScreen extends AppCompatActivity {

    ActivityLoginScreenBinding binding;
    FirebaseAuth mAuth;
    public static final int TIMER = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.registerText.setOnClickListener(v -> {
            binding.registerText.setTextColor(getColor(R.color.orange));
            startActivity(new Intent(this, registerScreen.class));
            finish();
        });

        binding.forgotText.setOnClickListener(v -> {
            new Handler().postDelayed(this::resetForgot, TIMER);
            binding.forgotText.setTextColor(getColor(R.color.orange));
            startActivity(new Intent(this, resetScreen.class));
        });

        binding.loginButton.setOnClickListener(v -> {
            binding.buttonAnimationLogin.setVisibility(View.VISIBLE);
            binding.buttonAnimationLogin.playAnimation();

            binding.buttonTextLogin.setVisibility(View.GONE);
            binding.loginButton.setEnabled(false); // Disable the login button

            loginWithEmail();
        });
    }

    private void resetForgot() {
        binding.forgotText.setTextColor(getColor(R.color.silver));
    }

    public void loginWithEmail(){
        String email = binding.emailField.getText().toString();
        String password = binding.passwordField.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show();
            resetButton();
            return;
        }

        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            resetButton();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        resetButton(); // Ensure the button is reset regardless of success or failure
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(loginScreen.this, "Logged In!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(loginScreen.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(loginScreen.this, "Email or Password is incorrect!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void resetButton() {
        binding.buttonAnimationLogin.pauseAnimation();
        binding.buttonAnimationLogin.setVisibility(View.GONE);

        binding.buttonTextLogin.setVisibility(View.VISIBLE);
        binding.loginButton.setEnabled(true); // Re-enable the login button
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(loginScreen.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}