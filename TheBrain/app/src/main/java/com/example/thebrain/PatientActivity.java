package com.example.thebrain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.thebrain.datamodels.Patient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;

public class PatientActivity extends AppCompatActivity {
    RelativeLayout my_profile, my_sessions;
    FirebaseUser mUser;
    DatabaseReference mRef;

    TextView name, title, logout;
    CircleImageView dp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        my_profile = findViewById(R.id.my_profile);
        my_sessions = findViewById(R.id.my_sessions);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = mUser.getUid();

        dp = findViewById(R.id.pat_dp);
        name = findViewById(R.id.name);
        title = findViewById(R.id.pat_title);
        logout = findViewById(R.id.logout);

        mRef = FirebaseDatabase.getInstance().getReference("users").child("Patient").child(userId);

        // home activity username, profile, title
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Patient patient = snapshot.getValue(Patient.class);
                    if(!patient.getImage().isEmpty() && patient.getImage()!= null){
                        Picasso.get().load(patient.getImage()).into(dp);
                    }
                    name.setText(patient.getFirstname()+" "+patient.getLastname());
                    title.setText(patient.getRole());
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        my_profile.setOnClickListener(V->{
            Intent PatientIntent = new Intent(PatientActivity.this, EditProfileActivity.class);
            PatientIntent.putExtra("role", "Patient");
            startActivity(PatientIntent);
        });

        my_sessions.setOnClickListener(V->{
            Intent sessionIntent = new Intent(PatientActivity.this, MySessionsActivity.class);
            sessionIntent.putExtra("role", "Patient");
            startActivity(sessionIntent);
        });

        logout.setOnClickListener(V -> {

            FirebaseAuth.getInstance().signOut();
            Intent logoutUser = new Intent(PatientActivity.this, LoginActivity.class);
            logoutUser.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logoutUser);
            finishAffinity();
        });
    }
}