package com.example.mybrain;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowInsetsController;
import android.widget.ProgressBar;

import com.example.mybrain.ui.patients.HomeFragment;
import com.example.mybrain.ui.patients.NotificationsFragment;
import com.example.mybrain.ui.patients.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class PatientActivity extends AppCompatActivity {

    BottomNavigationView navigationView;
    Fragment fragment = null;


    private String publisherId;

    private Uri imageUri;
    private StorageReference storageRef;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        dbRef = FirebaseDatabase.getInstance().getReference("users");
        storageRef = FirebaseStorage.getInstance().getReference("images");

        publisherId = getIntent().getStringExtra("publisherId");

        //this line hide statusbar
        getWindow().addFlags(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);// WindowManager.LayoutParams.FLAG_FULLSCREEN);

        navigationView = findViewById(R.id.bottom_navigation);

        navigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            String publisher = bundle.getString("publisherId");
            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileId", publisher);
            editor.apply();

            Intent patientIntent = new Intent(PatientActivity.this, EditProfileActivity.class);
            patientIntent.putExtra("role", "Patient");
            startActivity(patientIntent);
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setSelectedItemId(R.id.home);
        }

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = item -> {
        switch (item.getItemId()){
            case R.id.home:
                fragment = new HomeFragment();
                break;
            case R.id.person:
                SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                editor.apply();
//                fragment = new ProfileFragment();
                fragment = null;
                Intent patientIntent = new Intent(PatientActivity.this, EditProfileActivity.class);
                patientIntent.putExtra("role", "Patient");
                startActivity(patientIntent);
                break;
            case R.id.dashboard:
                fragment = null; // new DashboardFragment();
                startActivity(new Intent(PatientActivity.this, SessionsActivity.class));
                break;
            case R.id.notifications:
                fragment = new NotificationsFragment();
                break;
        }

        if(fragment!=null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }

        return true;
    };

}