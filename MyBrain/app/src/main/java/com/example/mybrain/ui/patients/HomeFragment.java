package com.example.mybrain.ui.patients;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mybrain.Adapters.SessionsAdapter;
import com.example.mybrain.R;
import com.example.mybrain.model.Patient;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {

    private RecyclerView all_sessions;
    private SessionsAdapter mSessionAdapter;
    private List<Sessions> mListSessions;
    private List<String> mFollowing;

    FirebaseUser mUser;
    DatabaseReference mRef;

    TextView name, title;
    CircleImageView dp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        dp = root.findViewById(R.id.user_image);
        name = root.findViewById(R.id.name);
        title = root.findViewById(R.id.title);

        all_sessions = root.findViewById(R.id.user_uploads);
        all_sessions.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        all_sessions.setLayoutManager(linearLayoutManager);

        mListSessions = new ArrayList<>();
        mSessionAdapter = new SessionsAdapter(getContext(), mListSessions, "Patient");

        all_sessions.setAdapter(mSessionAdapter);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = mUser.getUid();
        mRef = FirebaseDatabase.getInstance().getReference("users").child("Patient").child(userId);

        // home activity username, profile, title
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(getActivity()==null){
                        return;
                    }
                    Patient patient = snapshot.getValue(Patient.class);
                    Picasso.get().load(patient.getImage()).into(dp);
                    name.setText(patient.getFullname());
                    title.setText(patient.getRole());
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        readSession();
        return root;
    }

    private void readSession(){
        DatabaseReference sRef = FirebaseDatabase.getInstance().getReference("Brain");
        sRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                mListSessions.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Sessions session = dataSnapshot.getValue(Sessions.class);
//                    for(String id : mFollowing){
                        if(session.getPublisher().equals(mUser.getUid()))
                            mListSessions.add(session);
//                    }
                }
                mSessionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }


}