package com.example.covid_help_family;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.covid_help_family.Models.PatientModel;
import com.example.covid_help_family.Models.UserDetailsFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class Login extends AppCompatActivity {
    SharedPreferences emailShared, isRegistered;
    TextView email, password, errorText, loginTextView;
    Button emailSignIn;
    ImageView showPassword, hidePassword;
    ProgressBar emailLoginProgress;
    FirebaseAuth mAuth;
    SharedPreferences userDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailShared = getSharedPreferences("email", MODE_PRIVATE);
        isRegistered = getSharedPreferences("isRegistered", MODE_PRIVATE);

        email = findViewById(R.id.emailText);
        password = findViewById(R.id.passwordText);
        emailSignIn = findViewById(R.id.signInBtn);
        emailLoginProgress = findViewById(R.id.emailLoginProgress);
        errorText = findViewById(R.id.errorText);

        showPassword = findViewById(R.id.passwordVisible);
        hidePassword = findViewById(R.id.passwordInvisible);

        mAuth = FirebaseAuth.getInstance();

        userDetails = getSharedPreferences("userDetails", MODE_PRIVATE);

        emailSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String notFoundError = "no user record";
                if(email.getText().toString().isEmpty()){
                    email.setError("Please enter email");
                    email.requestFocus();
                }
                if(password.getText().toString().isEmpty()){
                    password.setError("Please enter password");
                }
                else{
                    errorText.setVisibility(View.INVISIBLE);
                    emailLoginProgress.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                                    .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Sign in success, update UI with the signed-in user's information
//                                    Log.d(TAG, "signInWithEmail:success");
                                                emailLoginProgress.setVisibility(View.GONE);
                                                checkIfRegistered(mAuth.getCurrentUser());
                                            } else {
                                                // If sign in fails, display a message to the user.
//                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                                Toast.makeText(Login.this, "Authentication failed.",
                                                        Toast.LENGTH_SHORT).show();
                                                emailLoginProgress.setVisibility(View.INVISIBLE);
                                                errorText.setText(task.getException().getMessage());
                                                if(task.getException().getMessage().contains(notFoundError)){
                                                    errorText.setText("No User found with that email :(");
                                                }else{
                                                    errorText.setText("Some error occurred! Please retry.");
                                                }
                                                errorText.setVisibility(View.VISIBLE);

//                                    updateUI(null);
                                                // ...
                                            }
                                            // ...
                                        }
                                    });
                        }
                    }, 800);
                }
            }
        });

        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPassword.setVisibility(View.INVISIBLE);
                hidePassword.setVisibility(View.VISIBLE);
                password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        });

        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePassword.setVisibility(View.INVISIBLE);
                showPassword.setVisibility(View.VISIBLE);
                password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
    }
    public void checkIfRegistered (final FirebaseUser mCurrentUser){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String email = mCurrentUser.getEmail();

        db.collection("patients")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult().isEmpty()){
                                Toast.makeText(Login.this, "No user found", Toast.LENGTH_SHORT).show();
                            }else{
//                                DocumentSnapshot ds = task.getResult();
                               // List<PatientModel> patients = task.getResult().toObjects(PatientModel.class);
                                PatientModel patient = new PatientModel("name","bed","Hospital",23 );
                                for (QueryDocumentSnapshot doc: task.getResult()){
                                    if(doc.getData().containsValue(email)){
                                        patient.name = String.valueOf(doc.getData().get("name"));
                                        patient.bed = String.valueOf(doc.getData().get("bed"));
                                        patient.spo2 = Integer.parseInt(String.valueOf(doc.getData().get("spo2")));
                                        patient.pulse = Integer.parseInt(String.valueOf(doc.getData().get("pulse")));
                                        patient.sysBp = Integer.parseInt(String.valueOf(doc.getData().get("sysBp")));
                                        patient.diaBp = Integer.parseInt(String.valueOf(doc.getData().get("diaBp")));
                                        patient.glucoFast = Integer.parseInt(String.valueOf(doc.getData().get("glucoFast")));
                                        patient.glucoRandom = Integer.parseInt(String.valueOf(doc.getData().get("glucoRandom")));

                                    }
                                }
                              //  PatientModel patient = patients.get(0);
                                Intent intent = new Intent(Login.this, Dashboard.class);
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
                        } else {
                            Toast.makeText(Login.this, "Some error occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
    public void sendUserToHome() {

        Intent intent = new Intent(Login.this, Dashboard.class);

        emailLoginProgress.setVisibility(View.GONE);
        startActivity(intent);
        finish();
    }
}