package com.example.thebrain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.thebrain.datamodels.Doctor;
import com.example.thebrain.datamodels.Patient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

public class UserDataActivity extends AppCompatActivity {

    CircleImageView user_dp;
    TextView name, email, phone;

    DatabaseReference mReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);

        String user_id = getIntent().getStringExtra("id");
        String user_role = getIntent().getStringExtra("role");

        mReference = FirebaseDatabase.getInstance().getReference("users").child(user_role).child(user_id);

        user_dp = findViewById(R.id.user_image);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);

        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(user_role.equals("Patient")){
                        Patient patient = snapshot.getValue(Patient.class);
                        if(!patient.getImage().equals("") && !patient.getImage().isEmpty() && patient.getImage()!=null){
                            Picasso.get().load(patient.getImage()).into(user_dp);
                        }
                        name.setText(patient.getFirstname()+" "+patient.getLastname());
                        email.setText(patient.getEmail());
                        phone.setText(patient.getPhone());
                    } else if(user_role.equals("Doctor")){
                        Doctor doctor = snapshot.getValue(Doctor.class);
                        if(!doctor.getImage().equals("") && !doctor.getImage().isEmpty() && doctor.getImage()!=null){
                            Picasso.get().load(doctor.getImage()).into(user_dp);
                        }
                        name.setText(doctor.getFirstname()+" "+doctor.getLastname());
                        email.setText(doctor.getEmail());
                        phone.setText(doctor.getPhone());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void CloseWind(View view){
        finish();
    }
}