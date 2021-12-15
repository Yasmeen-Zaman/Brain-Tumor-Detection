package com.example.thebrain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.thebrain.datamodels.Receptionist;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;

public class ReceptionistActivity extends AppCompatActivity {
    RelativeLayout doctor_list, patient_list, doctor_add, patient_add;
    CircleImageView user_img;
    TextView receptionist_name, logout;

    FirebaseUser mUser;
    DatabaseReference mReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receptionist);

        doctor_list = findViewById(R.id.doctor_list);
        patient_list = findViewById(R.id.patient_list);
        doctor_add = findViewById(R.id.doctor_add);
        patient_add = findViewById(R.id.patient_add);

        user_img = findViewById(R.id.user_img);
        receptionist_name = findViewById(R.id.receptionist_name);
        logout = findViewById(R.id.logout);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mReference = FirebaseDatabase.getInstance().getReference("users").child("Receptionist").child(mUser.getUid());
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Receptionist recept = snapshot.getValue(Receptionist.class);
                    if(recept.getImage()!=null && !recept.getImage().isEmpty())
                        Picasso.get().load(recept.getImage()).into(user_img);
                    receptionist_name.setText(recept.getFirstname()+" "+recept.getLastname());
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        logout.setOnClickListener(V ->{
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });

        doctor_list.setOnClickListener(V -> {
            Intent gotoUserList = new Intent(getApplicationContext(), UserListActivity.class);
            gotoUserList.putExtra("role", "Doctor");
            gotoUserList.putExtra("caller", "Receptionist");
            startActivity(gotoUserList);
        });

        patient_list.setOnClickListener(V -> {
            Intent gotoUserList = new Intent(getApplicationContext(), UserListActivity.class);
            gotoUserList.putExtra("role", "Patient");
            gotoUserList.putExtra("caller", "Receptionist");
            startActivity(gotoUserList);
        });

        doctor_add.setOnClickListener(V -> {
            Intent gotoUserList = new Intent(getApplicationContext(), AddUserActivity.class);
            gotoUserList.putExtra("role", "Doctor");
            gotoUserList.putExtra("caller", "Receptionist");
            startActivity(gotoUserList);
        });

        patient_add.setOnClickListener(V -> {
            Intent gotoUserList = new Intent(getApplicationContext(), AddUserActivity.class);
            gotoUserList.putExtra("role", "Patient");
            gotoUserList.putExtra("caller", "Receptionist");
            startActivity(gotoUserList);
        });
    }
}