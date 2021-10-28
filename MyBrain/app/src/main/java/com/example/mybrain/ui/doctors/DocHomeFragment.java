package com.example.mybrain.ui.doctors;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mybrain.Adapters.SessionsAdapter;
import com.example.mybrain.R;
import com.example.mybrain.model.Doctor;
import com.example.mybrain.model.Sessions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DocHomeFragment extends Fragment {

    private RecyclerView user_uploads;
    private SessionsAdapter mSessionAdapter;
    private List<Sessions> mListSessions;
    private List<String> mConnectedPatient;

    FirebaseUser mUser;
    DatabaseReference mReference;

    CircleImageView user_image;
    TextView name, title;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_doc_home, container, false);
        user_image = view.findViewById(R.id.user_image);
        name = view.findViewById(R.id.name);
        title = view.findViewById(R.id.title);

        user_uploads = view.findViewById(R.id.user_uploads);
        user_uploads.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        user_uploads.setLayoutManager(linearLayoutManager);

        mListSessions = new ArrayList<>();
        mSessionAdapter = new SessionsAdapter(getContext(), mListSessions, "Doctor");
        user_uploads.setAdapter(mSessionAdapter);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = mUser.getUid();
        mReference = FirebaseDatabase.getInstance().getReference("users").child("Doctor").child(userId);

        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(getActivity()==null){
                        return;
                    }
                    Doctor doctor = snapshot.getValue(Doctor.class);
                    Picasso.get().load(doctor.getImage()).into(user_image);
                    name.setText(doctor.getFullname());
                    title.setText(doctor.getRole());
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

//        readPosts();
        checkFollowing();

        return view;
    }

    private void readPosts() {
        DatabaseReference sRef = FirebaseDatabase.getInstance().getReference("Brain");
        sRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                mListSessions.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Sessions session = dataSnapshot.getValue(Sessions.class);
                    for(String id : mConnectedPatient){
                        if (session.getPublisher().equals(id))
                            mListSessions.add(session);
                    }
                }
                mSessionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    // this has to be done for doctor because patients are connected to doctor.
    private void checkFollowing(){
        mConnectedPatient = new ArrayList<>();

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Connection").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                mConnectedPatient.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    mConnectedPatient.add(dataSnapshot.getKey());
                }
                readPosts();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}