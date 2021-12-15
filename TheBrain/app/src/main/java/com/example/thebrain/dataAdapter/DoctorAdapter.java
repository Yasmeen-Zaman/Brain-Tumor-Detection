package com.example.thebrain.dataAdapter;

import android.content.Context;
import android.content.Intent;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.thebrain.R;
import com.example.thebrain.UserDataActivity;
import com.example.thebrain.datamodels.Doctor;
import com.example.thebrain.datamodels.Patient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder>{

    Context mContext;
    List<Doctor> mDoctorList;
    String caller;

    public DoctorAdapter(Context mContext, List<Doctor> mDoctorList, String caller) {
        this.mContext = mContext;
        this.mDoctorList = mDoctorList;
        this.caller = caller;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.doctor_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Doctor doctor = mDoctorList.get(position);
        if(!doctor.getImage().equals("") && !doctor.getImage().isEmpty() && doctor.getImage()!=null){
            Picasso.get().load(doctor.getImage()).into(holder.prof_img);
        } else {
            holder.prof_img.setImageResource(R.drawable.ic_person);
        }
        holder.full_name.setText(doctor.getFirstname()+" "+doctor.getLastname());
        holder.specialization.setText(doctor.getSpecialization());

        if(caller.equals("Doctor")){
            holder.delete.setVisibility(View.GONE);

        } else if(caller.equals("Receptionist")){
            holder.delete.setVisibility(View.VISIBLE);
            holder.full_name.setOnClickListener(V->{
                Intent intentUser = new Intent(mContext, UserDataActivity.class);
                intentUser.putExtra("id", doctor.getId());
                intentUser.putExtra("role", "Doctor");
                mContext.startActivity(intentUser);
            });
        }

        holder.delete.setOnClickListener(V->{
            deleteDoctor(doctor.getId(), doctor.getDoctor_reference_id());
        });

    }

    @Override
    public int getItemCount() {
        return mDoctorList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView full_name, specialization, select;
        CircleImageView prof_img;
        ImageView delete;

        public ViewHolder(@NonNull @NotNull View itemView) {

            super(itemView);

            full_name = itemView.findViewById(R.id.full_name);
            specialization = itemView.findViewById(R.id.specialization);
            prof_img = itemView.findViewById(R.id.prof_img);
            select = itemView.findViewById(R.id.select);
            delete = itemView.findViewById(R.id.delete);
        }
    }

    private void deleteDoctor(String doc_id, String ref_id){
        Query mDbmsRef = FirebaseDatabase.getInstance().getReference("users").child("Patient").orderByChild(ref_id);
        mDbmsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Patient patient = dataSnapshot.getValue(Patient.class);
                    if(dataSnapshot.child("doctor_reference_id").getValue(String.class).equals(ref_id)){
                        FirebaseDatabase.getInstance().getReference("users").child("Patient").child(patient.getId()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        FirebaseDatabase.getInstance().getReference("role-user").child(doc_id).removeValue();
        FirebaseDatabase.getInstance().getReference("users").child("Doctor").child(doc_id).removeValue();
    }
}
