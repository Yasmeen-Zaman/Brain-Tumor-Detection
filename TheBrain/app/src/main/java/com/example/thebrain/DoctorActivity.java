package com.example.thebrain;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.thebrain.datamodels.Doctor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorActivity extends AppCompatActivity {
    FirebaseUser mUser;
    DatabaseReference doctorRef;

    CircleImageView doc_dp;
    TextView name, logout;
    RelativeLayout doctor_list, patient_list, profile, upload_mri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        doc_dp = findViewById(R.id.doc_dp);
        name = findViewById(R.id.name);
        logout = findViewById(R.id.logout);
        doctor_list = findViewById(R.id.doctor_list);
        patient_list = findViewById(R.id.patient_list);
        profile = findViewById(R.id.profile);
        upload_mri = findViewById(R.id.upload_mri);

        doctorRef = FirebaseDatabase.getInstance().getReference("users").child("Doctor").child(mUser.getUid());
        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Doctor doctor = snapshot.getValue(Doctor.class);
                    if(!doctor.getImage().isEmpty() && doctor.getImage()!= null) {
                        Picasso.get().load(doctor.getImage()).into(doc_dp);
                    }
                    name.setText(doctor.getFirstname()+" "+doctor.getLastname());
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        logout.setOnClickListener(V -> {

            FirebaseAuth.getInstance().signOut();
            Intent logoutUser = new Intent(DoctorActivity.this, LoginActivity.class);
            logoutUser.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logoutUser);
            finishAffinity();

        });

        upload_mri.setOnClickListener(v -> startActivity(new Intent(DoctorActivity.this, MRIActivity.class)));

        profile.setOnClickListener(V -> {
            Intent doctorIntent = new Intent(DoctorActivity.this, EditProfileActivity.class);
            doctorIntent.putExtra("role", "Doctor");
            startActivity(doctorIntent);
        });

        doctor_list.setOnClickListener(V -> {
            Intent doctorIntent = new Intent(DoctorActivity.this, UserListActivity.class);
            doctorIntent.putExtra("role", "Doctor");
            doctorIntent.putExtra("caller", "Doctor");
            startActivity(doctorIntent);
        });

        patient_list.setOnClickListener(V -> {
            Intent doctorIntent = new Intent(DoctorActivity.this, UserListActivity.class);
            doctorIntent.putExtra("role", "Patient");
            doctorIntent.putExtra("caller", "Doctor");
            startActivity(doctorIntent);

        });

    }
}