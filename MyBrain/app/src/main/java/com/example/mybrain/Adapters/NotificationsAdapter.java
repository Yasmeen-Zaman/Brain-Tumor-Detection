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
import com.example.mybrain.model.Notification;
import com.example.mybrain.model.Sessions;
import com.example.mybrain.ui.doctors.DocHomeFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private Context mContext;
    private List<Notification> mNotification;

    public NotificationsAdapter(Context mContext, List<Notification> mNotification) {
        this.mContext = mContext;
        this.mNotification = mNotification;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false);
        return new NotificationsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Notification notification = mNotification.get(position);
        holder.comment.setText(notification.getText());
        getUserInfo(holder.img_profile, holder.name, notification.getUserId());

        if(notification.isInpost()){
            holder.post_image.setVisibility(View.VISIBLE);
            getMriImage(holder.post_image, notification.getPostId());
        } else {
            holder.post_image.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(V->{
            SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
            editor.putString("profileId", notification.getUserId());
            editor.apply();

            ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.doc_fragment_container, new DocHomeFragment());
        });
    }

    @Override
    public int getItemCount() {
        return mNotification.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView post_image;
        CircleImageView img_profile;
        TextView name, comment;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            post_image = itemView.findViewById(R.id.post_image);
            img_profile = itemView.findViewById(R.id.image_profile);
            name = itemView.findViewById(R.id.name);
            comment = itemView.findViewById(R.id.comment);
        }
    }

    private void getUserInfo(ImageView user_image, TextView name, String publisherId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child("Doctor").child(publisherId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Doctor doctor = snapshot.getValue(Doctor.class);
                Picasso.get().load(doctor.getImage()).into(user_image);
                name.setText(doctor.getFullname());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getMriImage(ImageView mri, final String mriId){
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Brain").child(mriId);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Sessions session = snapshot.getValue(Sessions.class);
                Picasso.get().load(session.getMriImage()).into(mri);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

}
