package com.example.thebrain.dataAdapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thebrain.R;
import com.example.thebrain.ReferCaseActivity;
import com.example.thebrain.UserDataActivity;
import com.example.thebrain.ViewSessionActivity;
import com.example.thebrain.datamodels.Patient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.ViewHolder>{
    Context mContext;
    List<Patient> mPatientList;
    String caller;

    FirebaseUser mUser;

    public PatientAdapter(Context mContext, List<Patient> mPatientList, String caller) {
        this.mContext = mContext;
        this.mPatientList = mPatientList;
        this.caller = caller;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        Patient patient = mPatientList.get(position);
        if(!patient.getImage().isEmpty() && !patient.getImage().equals("") && patient.getImage()!=null){
            Picasso.get().load(patient.getImage()).into(holder.patient_dp);
        } else {
            holder.patient_dp.setImageResource(R.drawable.ic_person);
        }
        holder.Name.setText(patient.getFirstname()+" "+patient.getLastname());

        if(caller.equals("Doctor")){
            holder.refer.setVisibility(View.VISIBLE);
            holder.patient_dp.setOnClickListener(V -> {
                Intent intent = new Intent(mContext, ViewSessionActivity.class);
                intent.putExtra("patient_id", patient.getId());
                intent.putExtra("doctor_id", mUser.getUid());
                mContext.startActivity(intent);
            });

            holder.Name.setOnClickListener(V -> {
                Intent intent = new Intent(mContext, ViewSessionActivity.class);
                intent.putExtra("patient_id", patient.getId());
                intent.putExtra("doctor_id", mUser.getUid());
                mContext.startActivity(intent);
            });
        } else if(caller.equals("Receptionist")){
            holder.refer.setVisibility(View.GONE);
            holder.Name.setOnClickListener(V->{
                Intent intentUser = new Intent(mContext, UserDataActivity.class);
                intentUser.putExtra("id", patient.getId());
                intentUser.putExtra("role", "Patient");
                mContext.startActivity(intentUser);
            });
        }

        holder.refer.setOnClickListener(V->{
            Intent intent = new Intent(mContext, ReferCaseActivity.class);
            intent.putExtra("patient_id", patient.getId());
            intent.putExtra("doctor_id", mUser.getUid());
            mContext.startActivity(intent);
        });

        holder.delete.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("role-user").child(patient.getId()).removeValue();
            FirebaseDatabase.getInstance().getReference("users").child("Patient").child(patient.getId()).removeValue();
            Toast.makeText(mContext, "Patient deleted successfully", Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public int getItemCount() {
        return mPatientList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView Name;
        CircleImageView patient_dp;
        ImageView delete, refer;

        public ViewHolder(@NonNull @NotNull View itemView) {

            super(itemView);
            Name = itemView.findViewById(R.id.Name);
            patient_dp = itemView.findViewById(R.id.patient_img);
            delete = itemView.findViewById(R.id.delete);
            refer = itemView.findViewById(R.id.refer);

        }
    }
}
