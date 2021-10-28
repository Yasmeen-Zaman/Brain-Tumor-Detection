package com.example.mybrain;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mybrain.Adapters.CommentsAdapter;
import com.example.mybrain.model.Comments;
import com.example.mybrain.model.Doctor;
import com.example.mybrain.model.Patient;
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
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CommentsActivity extends AppCompatActivity {

    private CommentsAdapter mCommentAdapter;
    private List<Comments> commentsList;

    EditText add_comment;
    Button post;
    ImageView img_prof;

    String commentId, pub_id;

    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Toolbar tool = findViewById(R.id.tool);
        setSupportActionBar(tool);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tool.setOnClickListener(v -> finish());

        RecyclerView mRV = findViewById(R.id.commentRV);
        mRV.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRV.setLayoutManager(llm);
        commentsList = new ArrayList<>();
        mCommentAdapter = new CommentsAdapter(this, commentsList);
        mRV.setAdapter(mCommentAdapter);

        add_comment = findViewById(R.id.add_comment);
        post = findViewById(R.id.post);
        img_prof = findViewById(R.id.img_prof);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        commentId = getIntent().getStringExtra("mriId");
        pub_id = getIntent().getStringExtra("pubId");

        post.setOnClickListener(V -> {
            if(add_comment.getText().toString().equals("")){
                Toast.makeText(getApplicationContext(), "Enter Comment", Toast.LENGTH_SHORT).show();
            } else {
                addComment();
            }
        });
        getImage();
        readComments();
    }

    private void addComment(){
        DatabaseReference mComRef = FirebaseDatabase.getInstance().getReference("comments").child(commentId);

        HashMap<String, Object> commentHashMap = new HashMap<>();
        commentHashMap.put("comment", add_comment.getText().toString());
        commentHashMap.put("publisher", mUser.getUid());
        commentHashMap.put("image", mUser.getPhotoUrl());

        mComRef.push().setValue(commentHashMap);
        addNotifications();
        add_comment.setText("");
    }

    private void addNotifications(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Notifications").child(pub_id);
        HashMap<String, Object> notifyHash = new HashMap<>();
        notifyHash.put("id", pub_id);
        notifyHash.put("text", "Commented "+add_comment.getText().toString());
        notifyHash.put("mriId", commentId);
        notifyHash.put("inpost", true);

        dbRef.push().setValue(notifyHash);
    }

    private void getImage(){
        DatabaseReference mRoleRef = FirebaseDatabase.getInstance().getReference("role-user").child(mUser.getUid());
//        Query findRole = mRoleRef.orderByChild("id").equalTo(mUser.getUid());

        mRoleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String role = snapshot.child("role").getValue(String.class);
                    DatabaseReference mImgRef = FirebaseDatabase.getInstance().getReference("users").child(role).child(mUser.getUid());
                    mImgRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(role.equals("Patient")){
                                Patient patient = snapshot.getValue(Patient.class);
                                Picasso.get().load(patient.getImage()).into(img_prof);
                            } else if(role.equals("Doctor")){
                                Doctor doctor = snapshot.getValue(Doctor.class);
                                Picasso.get().load(doctor.getImage()).into(img_prof);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void readComments(){
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("comments").child(commentId);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                commentsList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Comments comment = dataSnapshot.getValue(Comments.class);
                    commentsList.add(comment);
                }

                mCommentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

}