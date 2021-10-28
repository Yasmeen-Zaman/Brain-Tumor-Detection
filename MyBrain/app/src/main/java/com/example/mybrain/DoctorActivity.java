package com.example.mybrain;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowInsetsController;

import com.example.mybrain.ui.doctors.DocDashboardFragment;
import com.example.mybrain.ui.doctors.DocHomeFragment;
import com.example.mybrain.ui.doctors.DocNotificationFragment;
import com.example.mybrain.ui.doctors.DocSearchFragment;
import com.example.mybrain.ui.doctors.DocSettingsFragment;
import com.example.mybrain.ui.patients.HomeFragment;
import com.example.mybrain.ui.patients.NotificationsFragment;
import com.example.mybrain.ui.patients.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class DoctorActivity extends AppCompatActivity {

    BottomNavigationView navigationView;
    Fragment fragment = null;


    private String publisherId;

    private Uri imageUri;
    private StorageReference storageRef;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);


        //this line hide statusbar
        getWindow().addFlags(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);// WindowManager.LayoutParams.FLAG_FULLSCREEN);

        navigationView = findViewById(R.id.bottom_navigation);

        navigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.doc_fragment_container, new DocDashboardFragment()).commit();
        navigationView.setSelectedItemId(R.id.doc_dashboard);

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = item -> {
        switch (item.getItemId()){
            case R.id.doc_home:
                fragment = new DocHomeFragment();
                break;
            case R.id.doc_dashboard:
                SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                editor.apply();
                fragment = new DocDashboardFragment();
                break;
            case R.id.doc_search:
                fragment = null;
                fragment = new DocSearchFragment();
//                startActivity(new Intent(DoctorActivity.this, SessionsActivity.class));
                break;
            case R.id.doc_notification:
                fragment = new DocNotificationFragment();
                break;
            case R.id.doc_setting:
                fragment = new DocSettingsFragment();
                break;
        }

        if(fragment!=null){
            getSupportFragmentManager().beginTransaction().replace(R.id.doc_fragment_container, fragment).commit();
        }

        return true;
    };
}