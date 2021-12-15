package com.example.thebrain.dataAdapter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thebrain.R;
import com.example.thebrain.datamodels.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{
    Context mContext;
    List<Comment> mCommentList;
    String patient_id, session_id, username, userImage;
    FirebaseUser mUser;

    List<Comment> mReplyList;
    ReplyAdapter mReplyAdapter;

    String last_reply_key="0";
    int reply_key=-1;

    private OnItemsClickListener listener;

    public CommentAdapter(Context mContext, List<Comment> mCommentList, String patient_id, String session_id, String username, String userImage) {
        this.mContext = mContext;
        this.mCommentList = mCommentList;
        this.patient_id = patient_id;
        this.session_id = session_id;
        this.username = username;
        this.userImage = userImage;
    }

    public void setWhenClickListener(OnItemsClickListener listener){
        this.listener = listener;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_comment, parent,false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        Comment comment = mCommentList.get(position);
        if(!comment.getUserImage().isEmpty() && !comment.getUserImage().equals("") && comment.getUserImage()!=null){
            Picasso.get().load(comment.getUserImage()).into(holder.user_image);
        }
        holder.fullname.setText(comment.getUsername());
        holder.date.setText(comment.getCreatedAt());
        holder.body.setText(comment.getBody());

        holder.reply_rv.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setReverseLayout(true);
//        linearLayoutManager.setStackFromEnd(true);
        holder.reply_rv.setLayoutManager(linearLayoutManager);

        mReplyList = new ArrayList<>();
        mReplyAdapter = new ReplyAdapter(mContext, mReplyList);
        holder.reply_rv.setAdapter(mReplyAdapter);

        holder.reply.setOnClickListener(V->{
            if(listener!=null){
                listener.onItemClick(comment);
            }
        });
//        mReplyAdapter.notifyDataSetChanged();


        if(!isInternetAvailable()){
            Toast.makeText(mContext, "No internet connection", Toast.LENGTH_SHORT).show();
        } else {
            readReply(comment.getId(), patient_id, session_id);
        }

    }

    @Override
    public int getItemCount() {
        return mCommentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView user_image;
        TextView fullname, body, date, reply, total_replies;
        RecyclerView reply_rv;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            user_image = itemView.findViewById(R.id.img_prof);
            fullname = itemView.findViewById(R.id.username);
            body = itemView.findViewById(R.id.body);
            date = itemView.findViewById(R.id.date_time);
            reply = itemView.findViewById(R.id.reply);
            total_replies = itemView.findViewById(R.id.view_reply);
            reply_rv = itemView.findViewById(R.id.reply_rv);
        }
    }

    public interface OnItemsClickListener {
        void onItemClick(Comment comment);
    }

    private void readReply(String parent_id, String patient_id, String session_id){

        DatabaseReference refReply = FirebaseDatabase.getInstance().getReference("users").child("Patient").child(patient_id).child("sessions").child(session_id).child("comments");
        refReply.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    mReplyList.clear();
                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                        Comment comment_reply = dataSnapshot.getValue(Comment.class);
                        last_reply_key = comment_reply.getId();
                        reply_key = Integer.parseInt(last_reply_key);
                        if(comment_reply.getParentId().equals(parent_id)){
                            mReplyList.add(comment_reply);
                        }
                    }
                    mReplyAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private boolean isInternetAvailable(){
        ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
