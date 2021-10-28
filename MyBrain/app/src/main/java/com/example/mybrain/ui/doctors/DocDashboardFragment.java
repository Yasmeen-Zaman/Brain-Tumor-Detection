package com.example.mybrain.ui.doctors;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mybrain.EditProfileActivity;
import com.example.mybrain.R;
import com.example.mybrain.UploadMriActivity;
import com.example.mybrain.UserListActivity;
import com.example.mybrain.model.Doctor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

public class DocDashboardFragment extends Fragment {

    FirebaseUser mUser;
    DatabaseReference doctorRef;

    CircleImageView doc_dp, status;
    TextView name;
    RelativeLayout doctor_list, patient_list, profile, upload_mri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_doc_dashboard, container, false);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        doc_dp = view.findViewById(R.id.doc_dp);
        status = view.findViewById(R.id.status);
        name = view.findViewById(R.id.name);
        doctor_list = view.findViewById(R.id.doctor_list);
        patient_list = view.findViewById(R.id.patient_list);
        profile = view.findViewById(R.id.profile);
        upload_mri = view.findViewById(R.id.upload_mri);

        doctorRef = FirebaseDatabase.getInstance().getReference("users").child("Doctor").child(mUser.getUid());
        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Doctor doctor = snapshot.getValue(Doctor.class);
                    Picasso.get().load(doctor.getImage()).into(doc_dp);
                    name.setText(doctor.getFullname());
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        upload_mri.setOnClickListener(v -> startActivity(new Intent(getContext(), UploadMriActivity.class)));

        profile.setOnClickListener(V -> {
            Intent doctorIntent = new Intent(getContext(), EditProfileActivity.class);
            doctorIntent.putExtra("role", "Doctor");
            startActivity(doctorIntent);

        });

        doctor_list.setOnClickListener(V -> {
            Intent doctorIntent = new Intent(getContext(), UserListActivity.class);
            doctorIntent.putExtra("role", "Doctor");
            doctorIntent.putExtra("caller", "Doctor");
            startActivity(doctorIntent);
        });

        patient_list.setOnClickListener(V -> {
            Intent doctorIntent = new Intent(getContext(), UserListActivity.class);
            doctorIntent.putExtra("role", "Patient");
            doctorIntent.putExtra("caller", "Doctor");
            startActivity(doctorIntent);
        });

        return view;
    }

    private void getNotifications(String userId, String postId){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Notifications").child(userId);
        HashMap<String, Object> notifyHash = new HashMap<>();
        notifyHash.put("id", userId);
        notifyHash.put("text", "New Connection");
        notifyHash.put("mriId", "");
        notifyHash.put("inpost", false);

        dbRef.push().setValue(notifyHash);
    }
}