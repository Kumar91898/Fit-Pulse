package com.kumar.fitpulse.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kumar.fitpulse.R;
import com.kumar.fitpulse.databinding.FragmentHomeBinding;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class homeFragment extends Fragment {

    FragmentHomeBinding binding;
    private FirebaseUser currentUser;
    private String email;
    private Double height, weight;
    private int counter = 1;
    private int waterCounter = 0;
    private boolean isStart = false;
    private long timePaused = 0;
    private long startTime;
    private float totalHours;
    Dialog dialogWater;
    Dialog dialogSleep;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
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
            Toast.makeText(getContext(), "No user found!", Toast.LENGTH_SHORT).show();
        }

        binding.date.setText(getTodayDate());

        binding.waterLayout.setOnClickListener(v -> openWaterDialogue());
        binding.sleepLayout.setOnClickListener(v -> openSleepDialog());

        binding.walkLayout.setOnClickListener(v -> {
            replaceFragment(new stepsFragment());
        });

        refreshFragment();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void openWaterDialogue() {
        dialogWater = new Dialog(getContext());
        dialogWater.setContentView(R.layout.custom_dialogue_box);
        dialogWater.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogWater.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        dialogWater.setCancelable(true);
        dialogWater.show();

        Button resetBtn = dialogWater.findViewById(R.id.resetButton);
        Button addBtn = dialogWater.findViewById(R.id.addButton);
        TextView counterText = dialogWater.findViewById(R.id.counter);
        LinearLayout add = dialogWater.findViewById(R.id.add);
        LinearLayout remove = dialogWater.findViewById(R.id.remove);

        add.setOnClickListener(v -> {
            counter++;
            counterText.setText(String.valueOf(counter));
        });

        remove.setOnClickListener(v -> {
            if (counter > 1){
                counter--;
                counterText.setText(String.valueOf(counter));
            }
        });

        addBtn.setOnClickListener(v -> {
            int newData = waterCounter + counter;
            addToFireStore(newData);
            dialogWater.dismiss();
            counter = 1;
        });

        resetBtn.setOnClickListener(v -> {
            counter = 0;
            addToFireStore(counter);
            dialogWater.dismiss();
            fetchData();
        });
    }

    private void openSleepDialog() {
        dialogSleep = new Dialog(getContext());
        dialogSleep.setContentView(R.layout.sleep_dialogue_box);
        dialogSleep.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogSleep.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        dialogSleep.setCancelable(true);

        Button resetBtn = dialogSleep.findViewById(R.id.resetButton_sleep);
        Button startBtn = dialogSleep.findViewById(R.id.startButton_sleep);
        TextView timerTextView = dialogSleep.findViewById(R.id.timer_sleep);

        // Initialize the timer variables
        Handler handler = new Handler();
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long millis = currentTime - startTime;
                int hours = (int) (millis / (1000 * 60 * 60));
                int minutes = (int) ((millis / (1000 * 60)) % 60);
                int seconds = (int) ((millis / 1000) % 60);

                timerTextView.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));

                totalHours = hours + ((float) minutes / 60) + ((float) seconds / 3600);

                handler.postDelayed(this, 1000); // Update every second
            }
        };

        startBtn.setOnClickListener(v -> {
            if (!isStart) {
                isStart = true;
                startBtn.setText("Pause");
                startTime = System.currentTimeMillis() - timePaused;
                handler.postDelayed(timerRunnable, 0);

            } else {
                isStart = false;
                startBtn.setText("Start");
                handler.removeCallbacks(timerRunnable);
                timePaused = System.currentTimeMillis() - startTime;
                addTimeToFireStore(totalHours);
            }
        });

        resetBtn.setOnClickListener(v -> {
            isStart = false;
            startBtn.setText("Start");
            handler.removeCallbacks(timerRunnable);
            timerTextView.setText("00:00:00");
            startTime = 0;
            timePaused = 0;
            totalHours = 0.00f;
            addTimeToFireStore(totalHours);
        });

        // Restore timer state if previously running
        if (isStart) {
            startBtn.setText("Pause");
            handler.postDelayed(timerRunnable, 0);
        }

        dialogSleep.setOnDismissListener(dialog -> {
            // Store timePaused if paused
            if (startTime!=0 && timePaused!=0){
                if (!isStart) {
                    timePaused = System.currentTimeMillis() - startTime;
                }
            }
        });

        dialogSleep.show();
    }

    private void calculateBMI(){
        double BMI = weight / (height * height);

        String formattedBMI = String.format(Locale.getDefault(), "%.2f", BMI);
        binding.bmiValue.setText(formattedBMI);

        binding.progressBmi.setMax(40);

        if (BMI < 18.5) {
            binding.bmiText.setText("Underweight");
            binding.progressBmi.setProgress(10);
        } else if (BMI >= 18.5 && BMI < 24.9) {
            binding.bmiText.setText("Healthy Weight");
            binding.progressBmi.setProgress(20);
        } else if (BMI >= 25 && BMI < 30) {
            binding.bmiText.setText("Overweight");
            binding.progressBmi.setProgress(30);
        } else if (BMI >= 30) {
            binding.bmiText.setText("Obesity");
            binding.progressBmi.setProgress(40);
        }
    }

    private void loadImage(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.profile) // Placeholder image while loading
                .error(R.drawable.profile) // Image to show if loading fails
                .into(binding.profilePicture);
    }

    private void refreshFragment() {
        binding.swipeHome.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchData();

                Toast.makeText(getContext(), "Page Refreshed!", Toast.LENGTH_SHORT).show();
                binding.swipeHome.setRefreshing(false);
            }
        });
    }

    private void addToFireStore(int waterCounter){
        HashMap<String, Object> data = new HashMap<>();
        data.put("water", waterCounter);

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                String documentId = document.getId();

                                db.collection("users")
                                        .document(documentId)
                                        .update(data)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(getContext(), "updated!", Toast.LENGTH_SHORT).show();
                                            }
                                        }) .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getContext(), "failed!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void addTimeToFireStore(float totalHour){
        String formattedTime = String.format(Locale.getDefault(), "%.2f", totalHour);

        HashMap<String, Object> data = new HashMap<>();
        data.put("totalHours", formattedTime);

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                String documentId = document.getId();

                                db.collection("users")
                                        .document(documentId)
                                        .update(data)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(getContext(), "updated!", Toast.LENGTH_SHORT).show();
                                            }
                                        }) .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getContext(), "failed!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
                });
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
                                String name = document.getString("name");
                                String url = document.getString("ImageUrl");
                                height = document.getDouble("height");
                                weight = document.getDouble("weight");
                                waterCounter = Math.toIntExact(document.getLong("water"));
                                String getDistance = document.getString("distance");
                                int getSteps = Math.toIntExact(document.getLong("steps"));
                                String fetchCalories = document.getString("calories");
                                String fetchTimer = document.getString("totalHours");

                                float getCalories = Float.parseFloat(fetchCalories);

                                binding.nameText.setText(name);

                                binding.waterCounter.setText(String.valueOf(waterCounter));
                                binding.stepsHome.setText(String.valueOf(getSteps));
                                binding.progressWalk.setMax(2000);
                                binding.progressWalk.setProgress(getSteps);
                                binding.distanceHome.setText(getDistance + " km");

                                binding.calories.setText(fetchCalories);
                                binding.progressCalories.setMax(200);
                                binding.progressCalories.setProgress((int) getCalories);

                                binding.sleepTimer.setText(fetchTimer);

                                calculateBMI();
                                loadImage(url);
                            }
                        }
                    }
                });
    }
    
    public static String getTodayDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM");
        return dateFormat.format(calendar.getTime());
    }
}