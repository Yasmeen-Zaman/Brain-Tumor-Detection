package com.example.thebrain;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.thebrain.dataAdapter.RecordAdapter;
import com.example.thebrain.datamodels.Session;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ViewSessionActivity extends AppCompatActivity {

    Button create;
    ImageView close;

    ProgressDialog progressDialog;

    String patient_id, doctor_id;
    FirebaseUser mUser;

    RecordAdapter recordAdapter;
    List<Session> mRecordList;
    RecyclerView sessionsRv;
    String last_session_key ="0";
    int session_id=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_session);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        patient_id = getIntent().getStringExtra("patient_id");
        doctor_id = getIntent().getStringExtra("doctor_id");

        sessionsRv = findViewById(R.id.sessionRV);
        close= findViewById(R.id.close);
        create = findViewById(R.id.create);
        progressDialog = new ProgressDialog(this);

        sessionsRv.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ViewSessionActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        sessionsRv.setLayoutManager(linearLayoutManager);
        sessionsRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mRecordList = new ArrayList<>();
        recordAdapter = new RecordAdapter(ViewSessionActivity.this, mRecordList, patient_id);
        sessionsRv.setAdapter(recordAdapter);

        close.setOnClickListener(V -> finish());

        create.setOnClickListener(V -> {
            progressDialog.setMessage("Fetching Sessions...");
            progressDialog.show();
            createSession();
        });

        readSessions();

    }

    private void createSession(){
        if(!isInternetAvailable()){
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ViewSessionActivity.this);
            alertBuilder.setTitle("No Internet Connection");
            alertBuilder.setMessage("Please check your connection");
            alertBuilder.setNegativeButton("close", (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = alertBuilder.create();
            alertDialog.show();
        }

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss", Locale.getDefault());
        String session_date = df.format(date);
        last_session_key = String.valueOf(session_id+1);

        DatabaseReference sessionRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child("Patient")
                .child(patient_id)
                .child("sessions")
                .child(last_session_key);

        HashMap<String, Object> sessionHash = new HashMap<>();
        sessionHash.put("key", last_session_key);
        sessionHash.put("name", "Session "+ last_session_key);
        sessionHash.put("date", session_date);
        sessionHash.put("mriImage", "");
        sessionHash.put("permission","false");
        sessionHash.put("tag", "No MRI Uploaded");
        sessionHash.put("comments", "");
        sessionRef.setValue(sessionHash);
        progressDialog.dismiss();
        Toast.makeText(getApplicationContext(), "Session created Successfully", Toast.LENGTH_SHORT).show();
    }

    private void readSessions(){
        DatabaseReference sessionRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child("Patient")
                .child(patient_id)
                .child("sessions");
        sessionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    mRecordList.clear();
                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                        Session session = dataSnapshot.getValue(Session.class);
                        mRecordList.add(session);
                        last_session_key = session.getKey();
                        session_id = Integer.parseInt(last_session_key);
                    }

                    recordAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private boolean isInternetAvailable(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}