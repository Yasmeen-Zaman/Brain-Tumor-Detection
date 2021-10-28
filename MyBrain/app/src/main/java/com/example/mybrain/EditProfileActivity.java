package com.example.mybrain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mybrain.model.Doctor;
import com.example.mybrain.model.Patient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    ImageView close;
    CircleImageView user_image;
    TextView save, change_photo;
    MaterialEditText reference_id, full_name, username, email, phone, qualification, specialization, role;
    AutoCompleteTextView status;

    FirebaseUser mUser;
    StorageReference storageRef;
    private StorageTask uploadTask;
    private Uri mImageUri;

    String userStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        close = findViewById(R.id.close);
        user_image = findViewById(R.id.user_image);
        save = findViewById(R.id.save);
        change_photo = findViewById(R.id.change_pic);

        full_name = findViewById(R.id.full_name);
        reference_id = findViewById(R.id.reference_id);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        qualification = findViewById(R.id.qualification);
        specialization = findViewById(R.id.specialization);
        role = findViewById(R.id.role);
        status = findViewById(R.id.status);

        ArrayList<String> activeStatus = getStatus();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, activeStatus);

        //Set adapter
        status.setAdapter(adapter);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("images");

        String role_ = getIntent().getStringExtra("role");

        if(role_.equals("Doctor")){
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child("Doctor").child(mUser.getUid());
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Doctor doctor = snapshot.getValue(Doctor.class);
                    full_name.setText(doctor.getFullname());
                    reference_id.setText(doctor.getReference_id());
                    username.setText(doctor.getUsername());
                    email.setText(doctor.getEmail());
                    phone.setText(doctor.getPhone());
                    qualification.setText(doctor.getQualification());
                    specialization.setText(doctor.getSpecialization());
                    role.setText(doctor.getRole());
                    status.setText(doctor.getStatus());
                    Picasso.get().load(doctor.getImage()).into(user_image);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } else if(role_.equals("Patient")){
            reference_id.setVisibility(View.GONE);
            qualification.setVisibility(View.GONE);
            specialization.setVisibility(View.GONE);
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child("Patient").child(mUser.getUid());
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Patient patient = snapshot.getValue(Patient.class);
                    full_name.setText(patient.getFullname());
                    username.setText(patient.getUsername());
                    email.setText(patient.getEmail());
                    phone.setText(patient.getPhone());
                    role.setText(patient.getRole());
                    status.setText(patient.getStatus());
                    Picasso.get().load(patient.getImage()).into(user_image);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }

        status.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                userStatus = parent.getItemAtPosition(position).toString();
            }
        });

        close.setOnClickListener(V -> {
            if(role_.equals("Patient"))
                startActivity(new Intent(EditProfileActivity.this, PatientActivity.class));
            else if(role_.equals("Doctor"))
                startActivity(new Intent(EditProfileActivity.this, DoctorActivity.class));
            finish();
        });

        change_photo.setOnClickListener(V -> CropImage.activity()
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(EditProfileActivity.this));

        user_image.setOnClickListener(V -> CropImage.activity()
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(EditProfileActivity.this));

        save.setOnClickListener(V -> {
            updateProfile(full_name.getText().toString(), username.getText().toString(), email.getText().toString(), phone.getText().toString(), qualification.getText().toString(), specialization.getText().toString(), role.getText().toString(), userStatus);
        });

    }

    // Override onActivityResult method
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            // Get the Uri of data
            mImageUri = result.getUri();
            uploadImage();
        } else {
            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<String> getStatus() {
        ArrayList<String> userRole = new ArrayList<>();
        userRole.add("Active");
        userRole.add("Inactive");

        return userRole;
    }

    private String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void updateProfile(String fullname, String username, String email, String phone, String qualification, String specialization, String role, String userStatus){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child("Doctor").child(mUser.getUid());

        HashMap<String, Object> updatedUser = new HashMap<>();
        updatedUser.put("fullname", fullname);
        updatedUser.put("username", username);
        updatedUser.put("email", email);
        updatedUser.put("phone", phone);
        updatedUser.put("qualification", qualification);
        updatedUser.put("role", role);
        updatedUser.put("status", userStatus);

        ref.updateChildren(updatedUser);
    }

    private boolean isInternetAvailable(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        if(isConnected) {
            return true;
        } else{
            return false;
        }
    }

    private void uploadImage(){
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading...");
        pd.show();

        if(mImageUri!=null){
            StorageReference imgRef = storageRef.child(System.currentTimeMillis()+"."+getFileExtension(mImageUri));
            uploadTask = imgRef.putFile(mImageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull @NotNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return imgRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String myUri = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child("Doctor").child(mUser.getUid());
                        HashMap<String, Object> imgHash = new HashMap<>();
                        imgHash.put("image", myUri);

                        reference.updateChildren(imgHash);
                        pd.dismiss();
                    }else {
                        Toast.makeText(getApplicationContext(), "Failed to Update", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }
}