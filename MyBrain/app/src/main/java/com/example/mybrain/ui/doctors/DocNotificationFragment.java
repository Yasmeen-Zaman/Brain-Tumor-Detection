package com.example.mybrain.ui.doctors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mybrain.Adapters.NotificationsAdapter;
import com.example.mybrain.R;
import com.example.mybrain.model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DocNotificationFragment extends Fragment {
    private RecyclerView notificationRecycler;
    private NotificationsAdapter mNotificationAdapter;
    private List<Notification> mNotificationList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_doc_notification, container, false);
        notificationRecycler = root.findViewById(R.id.notify_recycler);
        notificationRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        notificationRecycler.setLayoutManager(linearLayoutManager);
        mNotificationList = new ArrayList<>();
        mNotificationAdapter = new NotificationsAdapter(getContext(), mNotificationList);
        notificationRecycler.setAdapter(mNotificationAdapter);

        readPostNotifications();

        return root;
    }

    private void readPostNotifications(){
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(mUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                mNotificationList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Notification notification = snapshot.getValue(Notification.class);
                    mNotificationList.add(notification);
                }

                Collections.reverse(mNotificationList);
                mNotificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}