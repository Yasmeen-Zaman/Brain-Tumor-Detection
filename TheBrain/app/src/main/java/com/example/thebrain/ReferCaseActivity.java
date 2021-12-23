package com.example.thebrain;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.thebrain.dataAdapter.DoctorAdapter;
import com.example.thebrain.dataAdapter.ReferAdapter;
import com.example.thebrain.datamodels.Doctor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ReferCaseActivity extends AppCompatActivity {
    private ReferAdapter mReferAdapter;
    private List<Doctor> mDoctorList;

    ImageView close;
    RecyclerView doctor_rv;

    DatabaseReference mReference;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refer_case);

        close= findViewById(R.id.close);
        doctor_rv = findViewById(R.id.doctors);

        close.setOnClickListener(V -> {
            startActivity(new Intent(ReferCaseActivity.this, DoctorActivity.class));
            finish();
        });

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mReference = FirebaseDatabase.getInstance().getReference("users").child("Doctor");

        String patient_id = getIntent().getStringExtra("patient_id");
        String doctor_id = getIntent().getStringExtra("doctor_id");

        doctor_rv.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        doctor_rv.setLayoutManager(linearLayoutManager);

        mDoctorList = new ArrayList<>();
        mReferAdapter = new ReferAdapter(ReferCaseActivity.this, mDoctorList, patient_id, doctor_id);
        doctor_rv.setAdapter(mReferAdapter);
        mReference = FirebaseDatabase.getInstance().getReference("users").child("Doctor");
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                mDoctorList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Doctor doctor = dataSnapshot.getValue(Doctor.class);
                    if (!doctor.getId().equals(mUser.getUid()))
                        mDoctorList.add(doctor);
                }
                mReferAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}