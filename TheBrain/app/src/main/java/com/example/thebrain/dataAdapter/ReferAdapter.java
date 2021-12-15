package com.example.thebrain.dataAdapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thebrain.DoctorActivity;
import com.example.thebrain.R;
import com.example.thebrain.datamodels.Doctor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ReferAdapter extends RecyclerView.Adapter<ReferAdapter.ViewHolder> {
    Context mContext;
    List<Doctor> mListDoctor;
    FirebaseUser mUser;
    String patient_id, doctor_id;

    public ReferAdapter(Context mContext, List<Doctor> mListDoctor, String patient_id, String doctor_id) {
        this.mContext = mContext;
        this.mListDoctor = mListDoctor;
        this.patient_id = patient_id;
        this.doctor_id = doctor_id;
    }


    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        Doctor doctor = mListDoctor.get(position);
        if(!doctor.getImage().equals("") && !doctor.getImage().isEmpty() && doctor.getImage()!=null){
            Picasso.get().load(doctor.getImage()).into(holder.prof_img);
        } else {
            holder.prof_img.setImageResource(R.drawable.ic_person);
        }

        holder.specialization.setText(doctor.getSpecialization());
        holder.fullname.setText(doctor.getFirstname()+" "+doctor.getLastname());

        holder.select.setOnClickListener(V-> UpdateConnection(mUser.getUid(), patient_id, doctor.getDoctor_reference_id()));

    }

    @Override
    public int getItemCount() {

        return mListDoctor.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView prof_img;
        TextView fullname, specialization, select;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            prof_img = itemView.findViewById(R.id.prof_img);
            fullname = itemView.findViewById(R.id.full_name);
            specialization = itemView.findViewById(R.id.specialization);
            select = itemView.findViewById(R.id.select);
        }
    }

    private void UpdateConnection(String cur_doctor_id, String patient_id, String refer_to){

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users").child("Patient").child(patient_id);
        HashMap<String, Object> referHash = new HashMap<>();
        referHash.put("doctor_reference_id", refer_to);
        mRef.updateChildren(referHash);
        Toast.makeText(mContext, "Patient Referred Successfully", Toast.LENGTH_SHORT).show();
        mContext.startActivity(new Intent(mContext, DoctorActivity.class));
        notifyDataSetChanged();
    }
}
