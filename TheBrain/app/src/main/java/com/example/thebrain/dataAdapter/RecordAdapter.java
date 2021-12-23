package com.example.thebrain.dataAdapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.thebrain.R;
import com.example.thebrain.SingleSessionActivity;
import com.example.thebrain.datamodels.Doctor;
import com.example.thebrain.datamodels.Session;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder>{
    Context mContext;
    List<Session> mSessionList;
    String patient_id;

    FirebaseUser mUser;

    private RecordAdapter.OnItemsClickListener listener;

    public RecordAdapter(Context mContext, List<Session> mSessionList, String patient_id) {
        this.mContext = mContext;
        this.mSessionList = mSessionList;
        this.patient_id = patient_id;
    }

    public void setWhenClickListener(RecordAdapter.OnItemsClickListener listener){
        this.listener = listener;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        Session record = mSessionList.get(position);
        holder.session_date.setText(record.getDate());
        holder.session_name.setText(record.getName());
        if (!record.getTag().equals("") || record.getTag() != null) {
            holder.prediction_tag.setVisibility(View.VISIBLE);
            holder.prediction_tag.setText(record.getTag());
        }else {
            holder.prediction_tag.setVisibility(View.GONE);
        }

        holder.session_name.setOnClickListener(V-> callUploadSession(patient_id, record.getKey()));

        holder.delete_session.setOnClickListener(V->{
            if(listener!=null){
                listener.onItemClick(record);
            }
//            deleteSession(record.getKey(), patient_id);
        });
    }

    @Override
    public int getItemCount() {
        return mSessionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView session_name, prediction_tag, session_date;
        ImageView delete_session;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            session_name = itemView.findViewById(R.id.session_name);
            prediction_tag = itemView.findViewById(R.id.prediction_tag);
            session_date= itemView.findViewById(R.id.session_date);
            delete_session = itemView.findViewById(R.id.delete_session);
        }
    }

    private void callUploadSession(String patient_id, String session_key){
        DatabaseReference sessionRef = FirebaseDatabase.getInstance().getReference("users").child("Doctor").child(mUser.getUid());
        sessionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Doctor doctor = snapshot.getValue(Doctor.class);
                    String username = doctor.getFirstname()+" "+doctor.getLastname();
                    String userImage = doctor.getImage();
                    Intent DoctorIntent = new Intent(mContext, SingleSessionActivity.class);
                    DoctorIntent.putExtra("patient_id", patient_id);
                    DoctorIntent.putExtra("session_id", session_key);
                    DoctorIntent.putExtra("userImage", userImage);
                    DoctorIntent.putExtra("username", username);
                    DoctorIntent.putExtra("caller", "Doctor");
                    mContext.startActivity(DoctorIntent);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

//    private void deleteSession(String session_id, String patient_id){
//        FirebaseDatabase.getInstance().getReference("users").child("Patient").child(patient_id).child("sessions").child(session_id).removeValue();
//        notifyDataSetChanged();
//    }

    public interface OnItemsClickListener {
        void onItemClick(Session record);
    }
}
