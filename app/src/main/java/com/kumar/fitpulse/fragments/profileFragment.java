package com.kumar.fitpulse.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kumar.fitpulse.R;
import com.kumar.fitpulse.activities.editScreen;
import com.kumar.fitpulse.activities.loginScreen;
import com.kumar.fitpulse.databinding.FragmentProfileBinding;

public class profileFragment extends Fragment {

    FragmentProfileBinding binding;
    FirebaseFirestore db;
    FirebaseUser currentUser;
    String email, name, url;
    Double height, weight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser!=null){
            email = currentUser.getEmail();
            fetchData();
        } else {
            Toast.makeText(getContext(), "User not found!", Toast.LENGTH_SHORT).show();
        }

        binding.edit.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), editScreen.class);
            intent.putExtra("name", name);
            intent.putExtra("email", email);
            intent.putExtra("url", url);
            intent.putExtra("height", height);
            intent.putExtra("weight", weight);
            startActivity(intent);
        });

        binding.logout.setOnClickListener(v -> {
            signOut();
        });

        refreshFragment();
    }

    private void refreshFragment() {
        binding.refreshProfile.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchData();

                Toast.makeText(getContext(), "Page Refreshed!", Toast.LENGTH_SHORT).show();
                binding.refreshProfile.setRefreshing(false);
            }
        });
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getContext(), loginScreen.class);

        Toast.makeText(getContext(), "Logged Out!", Toast.LENGTH_SHORT).show();

        startActivity(intent);
        getActivity().finish();
    }

    private void fetchData(){
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                name = document.getString("name");
                                url = document.getString("ImageUrl");
                                height = document.getDouble("height");
                                weight = document.getDouble("weight");

                                binding.nameTextView.setText(name);
                                binding.emailTextView.setText(email);
                                binding.heightTextView.setText(height + " meters");
                                binding.weightTextView.setText(weight + " kg");

                                loadImage(url);
                            }
                        }
                    }
                });
    }

    private void loadImage(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.profile) // Placeholder image while loading
                .error(R.drawable.profile) // Image to show if loading fails
                .into(binding.profilePictureAccount);
    }
}