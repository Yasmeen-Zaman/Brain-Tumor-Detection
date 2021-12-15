package com.example.thebrain.dataAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.thebrain.R;
import com.example.thebrain.datamodels.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ViewHolder>{

    Context context;
    List<Comment> mListReply;
    FirebaseUser mUser;

    public ReplyAdapter(Context context, List<Comment> mListReply) {
        this.context = context;
        this.mListReply = mListReply;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_reply, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        Comment reply = mListReply.get(position);
        holder.reply_date.setText(reply.getCreatedAt());
        holder.username.setText(reply.getUsername());
        holder.reply_body.setText(reply.getBody());
        if(!reply.getUserImage().isEmpty() && !reply.getUserImage().equals("") && reply.getUserImage()!=null){
            Picasso.get().load(reply.getUserImage()).into(holder.user_img);
        }
//        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mListReply.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView user_img;
        TextView username, reply_body, reply_date;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            user_img = itemView.findViewById(R.id.child_img_prof);
            username = itemView.findViewById(R.id.child_username);
            reply_body = itemView.findViewById(R.id.child_body);
            reply_date = itemView.findViewById(R.id.child_date_time);
        }
    }
}
