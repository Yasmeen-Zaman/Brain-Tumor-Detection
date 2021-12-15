package com.example.thebrain.dataAdapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.thebrain.R;
import com.example.thebrain.UserSessionActivity;
import com.example.thebrain.datamodels.Patient;
import com.example.thebrain.datamodels.Session;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MySessionAdapter extends RecyclerView.Adapter<MySessionAdapter.ViewHolder>{

    List<Session> mySessionList;
    Context mContext;
    String patient_id;

    FirebaseUser mUser;

    public MySessionAdapter(List<Session> mySessionList, Context mContext) {
        this.mySessionList = mySessionList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_session_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MySessionAdapter.ViewHolder holder, int position) {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        patient_id = mUser.getUid();
        Session session = mySessionList.get(position);
        if(!session.getName().equals("") && !session.getDate().isEmpty() && session.getName()!=null && session.getDate()!=null){
            holder.session_date.setText(session.getDate());
            holder.session_name.setText(session.getName());
        }
        if(session.getPermission().equals("true")){
            holder.prediction_tag.setVisibility(View.VISIBLE);
            holder.prediction_tag.setText(session.getTag().trim());
        } else if(session.getPermission().equals("false")){
            holder.prediction_tag.setVisibility(View.GONE);
        }

        holder.session_name.setOnClickListener(V->{
            callUploadSession(patient_id, session.getKey());
        });

    }

    @Override
    public int getItemCount() {
        return mySessionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView session_name;
        TextView prediction_tag;
        TextView session_date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            session_name = itemView.findViewById(R.id.session_name);
            prediction_tag = itemView.findViewById(R.id.prediction_tag);
            session_date = itemView.findViewById(R.id.session_date);
        }
    }

    private void callUploadSession(String patient_id, String session_id){
        DatabaseReference patientRef = FirebaseDatabase.getInstance().getReference("users").child("Patient").child(patient_id);
        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Patient patient = snapshot.getValue(Patient.class);
                    String username = patient.getFirstname()+" "+patient.getLastname();
                    String userImage = patient.getImage();

                    Intent myIntent = new Intent(mContext, UserSessionActivity.class);
                    myIntent.putExtra("patient_id", patient_id);
                    myIntent.putExtra("session_id", session_id);
                    myIntent.putExtra("username", username);
                    myIntent.putExtra("userImage", userImage);
                    myIntent.putExtra("caller", "Patient");
                    mContext.startActivity(myIntent);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}
