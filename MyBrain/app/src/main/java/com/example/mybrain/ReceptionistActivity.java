package com.example.mybrain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mybrain.model.Receptionist;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

public class ReceptionistActivity extends AppCompatActivity {

    RelativeLayout doctor_list, patient_list;
    CircleImageView user_img;
    TextView receptionist_name;

    FirebaseUser mUser;
    DatabaseReference mReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receptionist);

        doctor_list = findViewById(R.id.doctor_list);
        patient_list = findViewById(R.id.patient_list);

        user_img = findViewById(R.id.user_img);
        receptionist_name = findViewById(R.id.receptionist_name);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mReference = FirebaseDatabase.getInstance().getReference("users").child("Receptionist").child(mUser.getUid());
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Receptionist recept = snapshot.getValue(Receptionist.class);
                    Picasso.get().load(recept.getImage()).into(user_img);
                    receptionist_name.setText(recept.getFullname());
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
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
    }
}