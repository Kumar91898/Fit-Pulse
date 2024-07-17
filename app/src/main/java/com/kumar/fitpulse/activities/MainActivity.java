package com.kumar.fitpulse.activities;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.kumar.fitpulse.R;
import com.kumar.fitpulse.databinding.ActivityMainBinding;
import com.kumar.fitpulse.fragments.homeFragment;
import com.kumar.fitpulse.fragments.profileFragment;
import com.kumar.fitpulse.fragments.stepsFragment;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragments(new homeFragment());

        binding.home.setOnClickListener(v -> {
            binding.home.setBackgroundResource(R.drawable.nav_background);
            binding.steps.setBackgroundResource(0);
            binding.profile.setBackgroundResource(0);

            binding.homeImage.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            binding.stepsImage.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
            binding.profileImage.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));

            replaceFragments(new homeFragment());
        });

        binding.steps.setOnClickListener(v -> {
            binding.home.setBackgroundResource(0);
            binding.profile.setBackgroundResource(0);
            binding.steps.setBackgroundResource(R.drawable.nav_background);

            binding.homeImage.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
            binding.stepsImage.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            binding.profileImage.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));


            replaceFragments(new stepsFragment());
        });

        binding.profile.setOnClickListener(v -> {
            binding.home.setBackgroundResource(0);
            binding.steps.setBackgroundResource(0);
            binding.profile.setBackgroundResource(R.drawable.nav_background);

            binding.homeImage.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
            binding.stepsImage.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
            binding.profileImage.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));


            replaceFragments(new profileFragment());
        });
    }

    public void replaceFragments(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
}