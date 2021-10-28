package com.example.mybrain;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mybrain.Adapters.DoctorAdapter;
import com.example.mybrain.Adapters.PatientAdapter;
import com.example.mybrain.model.Doctor;
import com.example.mybrain.model.Patient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class UserListActivity extends AppCompatActivity {

    private DoctorAdapter mDoctorAdapter;
    private PatientAdapter mPatientAdapter;
    private List<Doctor> mListDoctor;
    private List<Patient> mListPatient;
    private List<String> mUsersList;

    FirebaseUser mUser;
    DatabaseReference dbRef;

    ImageView close;
    TextView title_bar;
    FloatingActionButton add_user;
    RecyclerView users_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        close = findViewById(R.id.close);
        title_bar = findViewById(R.id.title_bar);
        users_list = findViewById(R.id.users_list);
        add_user = findViewById(R.id.add_user);

        users_list.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        users_list.setLayoutManager(linearLayoutManager);

        close.setOnClickListener(V -> finish());

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        String role= getIntent().getStringExtra("role");
        String caller = getIntent().getStringExtra("caller");

        if(caller.equals("Doctor") && role.equals("Doctor")){
            add_user.setVisibility(View.GONE);
        } else {
            add_user.setVisibility(View.VISIBLE);
            add_user.setOnClickListener(V -> AddUser(role));
        }

        if(caller.equals("Doctor")) {
            if (role.equals("Doctor")) {
                title_bar.setText("List of Doctors");
                mListDoctor = new ArrayList<>();
                mDoctorAdapter = new DoctorAdapter(getApplicationContext(), mListDoctor, "Doctor");
                users_list.setAdapter(mDoctorAdapter);
                dbRef = FirebaseDatabase.getInstance().getReference("users").child("Doctor");
                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        mListDoctor.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Doctor doctor = dataSnapshot.getValue(Doctor.class);
                            if (!doctor.getId().equals(mUser.getUid()) && doctor.getStatus().equals("Active"))
                                mListDoctor.add(doctor);
                        }
                        mDoctorAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

            } else if (role.equals("Patient")) {
                title_bar.setText("My Patients List");
                mUsersList = new ArrayList<>();
                DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Connection").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                mRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        mUsersList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            mUsersList.add(dataSnapshot.getKey());
                        }
                        readPatients();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        } else if(caller.equals("Receptionist")){
            if (role.equals("Doctor")) {
                title_bar.setText("List of Doctors");
                mListDoctor = new ArrayList<>();
                mDoctorAdapter = new DoctorAdapter(getApplicationContext(), mListDoctor, "Receptionist");
                users_list.setAdapter(mDoctorAdapter);
                dbRef = FirebaseDatabase.getInstance().getReference("users").child("Doctor");
                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        mListDoctor.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Doctor doctor = dataSnapshot.getValue(Doctor.class);
                            if (doctor.getStatus().equals("Active"))
                                mListDoctor.add(doctor);
                        }
                        mDoctorAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

            } else if (role.equals("Patient")) {
                title_bar.setText("List of Patients");
                mListPatient = new ArrayList<>();
                mPatientAdapter = new PatientAdapter(getApplicationContext(), mListPatient, "Receptionist");
                users_list.setAdapter(mPatientAdapter);
                dbRef = FirebaseDatabase.getInstance().getReference("users").child("Patient");
                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        mListPatient.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Patient patient = dataSnapshot.getValue(Patient.class);
                            if (patient.getStatus().equals("Active"))
                                mListPatient.add(patient);
                        }
                        mPatientAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });


            }
        }
    }

    private void readPatients(){
        mListPatient = new ArrayList<>();
        mPatientAdapter = new PatientAdapter(getApplicationContext(), mListPatient, "Doctor");
        users_list.setAdapter(mPatientAdapter);
        DatabaseReference patRef = FirebaseDatabase.getInstance().getReference("users").child("Patient");
        patRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                mListPatient.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Patient patient = dataSnapshot.getValue(Patient.class);
                    for (String id : mUsersList) {
                        if (patient.getId().equals(id)) {
                            mListPatient.add(patient);
                        }
                    }
                }
                mPatientAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void AddUser(String role){
        Intent addUserIntent = new Intent(UserListActivity.this, AddUserActivity.class);
        addUserIntent.putExtra("role", role);
        startActivity(addUserIntent);
    }
}