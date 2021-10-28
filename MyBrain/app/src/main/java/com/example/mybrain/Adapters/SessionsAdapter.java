package com.example.mybrain.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mybrain.CommentsActivity;
import com.example.mybrain.R;
import com.example.mybrain.model.Doctor;
import com.example.mybrain.model.Patient;
import com.example.mybrain.model.Sessions;
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

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class SessionsAdapter extends RecyclerView.Adapter<SessionsAdapter.ViewHolder>{

    public Context mContext;
    public List<Sessions> mSession;
    private String caller;

    private FirebaseUser firebaseUser;

    public SessionsAdapter(Context mContext, List<Sessions> mSession, String caller) {
        this.mContext = mContext;
        this.mSession = mSession;
        this.caller = caller;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.session_post, parent, false);
        return new SessionsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Sessions session = mSession.get(position);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(caller).child(firebaseUser.getUid());
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(caller.equals("Doctor")){
                        Doctor doctor = snapshot.getValue(Doctor.class);
                        Picasso.get().load(doctor.getImage()).into(holder.profileImg);
                    } else if(caller.equals("Patient")){
                        Patient patient = snapshot.getValue(Patient.class);
                        Picasso.get().load(patient.getImage()).into(holder.profileImg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        Picasso.get().load(session.getMriImage()).into(holder.postImg);
        holder.result.setText(session.getResult());

        if(session.getResult().equals("") || session.getResult()==null){
            holder.result.setVisibility(View.GONE);
//            holder.type_tag.setVisibility(View.GONE);
        } else{
            holder.result.setVisibility(View.VISIBLE);
//            holder.type_tag.setVisibility(View.VISIBLE);
        }

        publisherInfo(holder.profileImg, holder.fullname, holder.result, session.getPublisher(), session.getResult());

        getComments(session.getMriId(), holder.comment_tag);


        holder.comVec.setOnClickListener(V -> {
            Intent intent = new Intent(mContext, CommentsActivity.class);
            intent.putExtra("mriId", session.getMriId());
            intent.putExtra("pubId", session.getPublisher());
            mContext.startActivity(intent);
        });

        holder.comment_tag.setOnClickListener(V -> {
            Intent intent = new Intent(mContext, CommentsActivity.class);
            intent.putExtra("mriId", session.getMriId());
            intent.putExtra("pubId", session.getPublisher());
            mContext.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return mSession.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView postImg, comVec;
        public CircleImageView profileImg;
        public TextView fullname, result, comment_tag, type_tag;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            profileImg = itemView.findViewById(R.id.profileImg);
            postImg = itemView.findViewById(R.id.postImg);
            comVec = itemView.findViewById(R.id.comVec);
            fullname = itemView.findViewById(R.id.uname);
            result = itemView.findViewById(R.id.tumorType);
            comment_tag = itemView.findViewById(R.id.comment_tag);
            type_tag = itemView.findViewWithTag(R.id.typeTag);

        }
    }

    private void getComments(String postId, final TextView comments){
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("comments");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                long viewComment = snapshot.child(postId).getChildrenCount();
                comments.setText("View all "+snapshot.child(postId).getChildrenCount()+" comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getNotifications(String userId, String postId){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Notifications").child(userId);
        HashMap<String, Object> notifyHash = new HashMap<>();
        notifyHash.put("id", userId);
        notifyHash.put("text", "Posted new Comment");
        notifyHash.put("mriId", postId);
        notifyHash.put("inpost", true);

        dbRef.push().setValue(notifyHash);
    }

    private void publisherInfo(ImageView profileImg, TextView publisher, TextView result, String userId, String sRes){
        DatabaseReference mDbRef = FirebaseDatabase.getInstance().getReference("role-user");

        Query check = mDbRef.orderByChild("id").equalTo(userId);
        check.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String uRole = snapshot.child(userId).child("role").getValue(String.class);
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uRole).child(userId);
                    userRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if(uRole.equals("Doctor")){
                                Doctor doctor = snapshot.getValue(Doctor.class);
                                profileImg.setImageURI(firebaseUser.getPhotoUrl());
//                                Picasso.get().load(doctor.getImage()).into(profileImg);
                                result.setText(sRes);
                                publisher.setText(doctor.getFullname());
                            } else if(uRole.equals("Patient")){
                                Patient patient = snapshot.getValue(Patient.class);
                                profileImg.setImageURI(firebaseUser.getPhotoUrl());
//                                Picasso.get().load(patient.getImage()).into(profileImg);
                                result.setText(sRes);
                                publisher.setText(patient.getFullname());
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
