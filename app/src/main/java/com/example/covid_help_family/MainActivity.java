package com.example.covid_help_family;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.covid_help_family.Models.PatientModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private FirebaseUser mCurrentUser;
    FirebaseDatabase database;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Disabling dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        sharedPreferences = getSharedPreferences("userDetails", MODE_PRIVATE);
        String email = sharedPreferences.getString("email","");


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mCurrentUser != null) {
                    db.collection("patients").whereEqualTo("email",email)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull  Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()){
                                        if(task.getResult().isEmpty()){
                                            Toast.makeText(MainActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            List<PatientModel> patients = task.getResult().toObjects(PatientModel.class);
                                            PatientModel patient = patients.get(0);
                                            Intent intent = new Intent(MainActivity.this, Dashboard.class);
                                            intent.putExtra("patientName", patient.name);
                                            intent.putExtra("patientBed", patient.bed);
                                            intent.putExtra("spo2", patient.spo2);
                                            intent.putExtra("pulse", patient.pulse);
                                            intent.putExtra("sysBp", patient.sysBp);
                                            intent.putExtra("diaBp", patient.diaBp);
                                            intent.putExtra("glucoFast", patient.glucoFast);
                                            intent.putExtra("glucoRandom", patient.glucoRandom);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                }
                            });
                } else {
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    startActivity(intent);
                    finish();
                }

            }
        }, 1500);
    }
}