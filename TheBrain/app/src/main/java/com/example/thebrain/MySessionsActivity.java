package com.example.thebrain;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;

import com.example.thebrain.dataAdapter.MySessionAdapter;
import com.example.thebrain.datamodels.Session;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MySessionsActivity extends AppCompatActivity {

    RecyclerView my_session_rv;
    FirebaseUser mUser;
    DatabaseReference sessionReference;

    List<Session> mSessionList;
    MySessionAdapter mySessionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_sessions);

        my_session_rv = findViewById(R.id.my_session_rv);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        sessionReference = FirebaseDatabase.getInstance().getReference("users").child("Patient").child(mUser.getUid()).child("sessions");

        mSessionList = new ArrayList<>();
        mySessionAdapter = new MySessionAdapter(mSessionList, MySessionsActivity.this);

        my_session_rv.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MySessionsActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        my_session_rv.setLayoutManager(linearLayoutManager);
        my_session_rv.addItemDecoration(new DividerItemDecoration(MySessionsActivity.this, DividerItemDecoration.VERTICAL));
        my_session_rv.setAdapter(mySessionAdapter);

        readSessions();
    }

    private void readSessions(){
        ProgressDialog pd = new ProgressDialog(MySessionsActivity.this);
        pd.setMessage("Loading Sessions...");
        pd.show();
        sessionReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    mSessionList.clear();
                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                        Session mySession = dataSnapshot.getValue(Session.class);
                        mSessionList.add(mySession);
                    }
                    mySessionAdapter.notifyDataSetChanged();
                }
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pd.dismiss();
            }
        });

    }

    public void CloseSessions(View view){
        finish();
    }
}