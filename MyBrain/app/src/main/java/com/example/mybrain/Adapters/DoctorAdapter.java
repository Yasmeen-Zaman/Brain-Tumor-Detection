package com.example.mybrain.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mybrain.R;
import com.example.mybrain.model.Doctor;
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

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

    private Context mContext;
    private List<Doctor> doctorList;
    private String caller;
    private FirebaseUser mUser;

    public DoctorAdapter(Context mContext, List<Doctor> doctorList, String caller) {
        this.mContext = mContext;
        this.doctorList = doctorList;
        this.caller= caller;
    }

    @NonNull
    @NotNull
    @Override
    public DoctorAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);

        return new DoctorAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull DoctorAdapter.ViewHolder holder, int position) {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        final Doctor doc = doctorList.get(position);
        holder.full_name.setText(doc.getFullname());
        holder.title.setText(doc.getRole());
        Picasso.get().load(doc.getImage()).into(holder.prof_img);

        holder.itemView.setOnClickListener(V->{
            SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
            editor.putString("profileId", doc.getImage());
            editor.apply();
        });

        if(caller.equals("Doctor")){
            holder.delete.setVisibility(View.GONE);
        } else if (caller.equals("Receptionist")){
            holder.delete.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return doctorList.size();
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

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView full_name, title;
        public CircleImageView prof_img;
        public ImageView delete;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            full_name = itemView.findViewById(R.id.full_name);
            title = itemView.findViewById(R.id.title);
            prof_img = itemView.findViewById(R.id.prof_img);
            delete = itemView.findViewById(R.id.delete);

        }
    }

}
