package com.example.mybrain.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mybrain.R;
import com.example.mybrain.model.Patient;
import com.example.mybrain.ui.patients.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.ViewHolder> {

    private Context mContext;
    private List<Patient> patientList;
    private String caller;
    private FirebaseUser mUser;

    public PatientAdapter(Context mContext, List<Patient> patientList, String caller) {
        this.mContext = mContext;
        this.patientList = patientList;
        this.caller = caller;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.patient_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        final Patient ptn = patientList.get(position);
        holder.name.setText(ptn.getFullname());
        holder.delete.setVisibility(View.VISIBLE);
        if(caller.equals("Receptionist")){
            holder.refer.setVisibility(View.GONE);
        } else if(caller.equals("Doctor")){
            holder.refer.setVisibility(View.VISIBLE);
        }
        Picasso.get().load(ptn.getImage()).into(holder.patient_img);

        holder.itemView.setOnClickListener(V->{
            SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
            editor.putString("profileId", ptn.getImage());
            editor.apply();

            ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();

        });

        holder.delete.setOnClickListener(V -> {

        });

    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    private void getNotifications(String userId){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Notifications").child(userId);
        HashMap<String, Object> notifyHash = new HashMap<>();
        notifyHash.put("id", userId);
        notifyHash.put("text", "New Patient");
        notifyHash.put("mriId", "");
        notifyHash.put("inpost", false);

        dbRef.push().setValue(notifyHash);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView name;
        public CircleImageView patient_img;
        public ImageView delete, refer;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.Name);
            patient_img = itemView.findViewById(R.id.patient_img);
            delete = itemView.findViewById(R.id.delete);
            refer = itemView.findViewById(R.id.refer);
        }
    }


}
