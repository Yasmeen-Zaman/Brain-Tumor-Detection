package com.example.mybrain.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mybrain.PatientActivity;
import com.example.mybrain.R;
import com.example.mybrain.DoctorActivity;
import com.example.mybrain.model.Comments;
import com.example.mybrain.model.Doctor;
import com.example.mybrain.model.Patient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private Context mContext;
    private List<Comments> mComment;

    private FirebaseUser mUser;

    public CommentsAdapter(Context mContext, List<Comments> mComment) {
        this.mContext = mContext;
        this.mComment = mComment;
    }


    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);

        return new CommentsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        Comments comments = mComment.get(position);

        holder.comments.setText(comments.getComment());

        getUserInfo(holder.img_prof, holder.username, comments.getPublisher());

        DatabaseReference roleDbRef = FirebaseDatabase.getInstance().getReference("role-user").child(mUser.getUid());
        roleDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String user = snapshot.child("role").getValue(String.class);
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(user).child(mUser.getUid());
                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if(user.equals("Doctor")){
                            Doctor doc =  snapshot.getValue(Doctor.class);
                            Picasso.get().load(doc.getImage()).into(holder.img_prof);
                        }
                        if(user.equals("Patient")){
                            Patient pat =  snapshot.getValue(Patient.class);
                            Picasso.get().load(pat.getImage()).into(holder.img_prof);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

//        holder.img_prof.setImageURI(mUser.getPhotoUrl());


        holder.comments.setOnClickListener(V->{
            DatabaseReference mDbRef = FirebaseDatabase.getInstance().getReference("role-user");
            Query checkRole = mDbRef.orderByChild("id").equalTo(comments.getPublisher());
            checkRole.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    String role = snapshot.child("role").getValue(String.class);
                    if(role.equals("Doctor")){
                        Intent docInt = new Intent(mContext, DoctorActivity.class);
                        docInt.putExtra("publisherId", comments.getPublisher());
                        mContext.startActivity(docInt);
                    } else if(role.equals("Patient")) {
                        Intent patInt = new Intent(mContext, PatientActivity.class);
                        patInt.putExtra("publisherId", comments.getPublisher());
                        mContext.startActivity(patInt);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

        });


        holder.img_prof.setOnClickListener(V->{
            DatabaseReference mDbRef = FirebaseDatabase.getInstance().getReference("role-user").child(comments.getPublisher());
//            Query checkRole = mDbRef.orderByChild("id").equalTo(comments.getPublisher());
            mDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String role = snapshot.child("role").getValue(String.class);
                    if(role.equals("Doctor")){
                        Intent docInt = new Intent(mContext, DoctorActivity.class);
                        docInt.putExtra("publisherId", comments.getPublisher());
                        mContext.startActivity(docInt);
                    } else if(role.equals("Patient")) {
                        Intent patInt = new Intent(mContext, PatientActivity.class);
                        patInt.putExtra("publisherId", comments.getPublisher());
                        mContext.startActivity(patInt);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

        });

        holder.reply.setOnClickListener(V -> {

        });
    }

    @Override
    public int getItemCount() {
        return mComment.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView img_prof;
        public TextView username, comments, reply;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            img_prof = itemView.findViewById(R.id.img_prof);
            username = itemView.findViewById(R.id.username);
            comments = itemView.findViewById(R.id.comments);
            reply = itemView.findViewById(R.id.reply);
        }
    }

    private void getUserInfo(ImageView img_prof, TextView username, String publisherId){
        DatabaseReference mRoleRef = FirebaseDatabase.getInstance().getReference("role-user").child(publisherId);
//        Query check = mRoleRef.orderByChild("id").equalTo(publisherId);
        mRoleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String uRole = snapshot.child("role").getValue(String.class);
                    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("users").child(uRole).child(publisherId);
                    mRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if(uRole.equals("Doctor")){
                                Doctor doctor = snapshot.getValue(Doctor.class);
                                img_prof.setImageURI(mUser.getPhotoUrl());
//                                Picasso.get().load(doctor.getImage()).into(img_prof);
                                username.setText(doctor.getFullname());
                            } else if(uRole.equals("Patient")){
                                Patient patient = snapshot.getValue(Patient.class);
                                img_prof.setImageURI(mUser.getPhotoUrl());
//                                Picasso.get().load(patient.getImage()).into(img_prof);
                                username.setText(patient.getFullname());
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

}
