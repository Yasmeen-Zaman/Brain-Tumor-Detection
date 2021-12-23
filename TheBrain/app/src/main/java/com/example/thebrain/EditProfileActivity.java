package com.example.thebrain;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thebrain.datamodels.Doctor;
import com.example.thebrain.datamodels.Patient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    ImageView close;
    CircleImageView user_image;
    TextView save, change_photo;
    MaterialEditText reference_id, firstname, lastname, cnic, age, gender, city, street, username, email, phone, qualification, specialization, role, status;

    FirebaseUser mUser;
    StorageReference storageRef;
    private StorageTask uploadTask;
    private Uri mImageUri;

    String dbEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        firstname = findViewById(R.id.fname);
        lastname = findViewById(R.id.last_name);
        cnic = findViewById(R.id.cnic);
        city = findViewById(R.id.city);
        street = findViewById(R.id.street);
        age = findViewById(R.id.age);
        gender = findViewById(R.id.gender);
        reference_id = findViewById(R.id.reference_id);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        qualification = findViewById(R.id.qualification);
        specialization = findViewById(R.id.specialization);
        role = findViewById(R.id.role);
        close = findViewById(R.id.close);
        change_photo = findViewById(R.id.change_pic);
        save = findViewById(R.id.save);
        user_image = findViewById(R.id.user_image);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("images");

        String role_ = getIntent().getStringExtra("role");

        if(role_.equals("Doctor")){
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child("Doctor").child(mUser.getUid());
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Doctor doctor = snapshot.getValue(Doctor.class);

                    dbEmail = doctor.getEmail();

                    firstname.setText(doctor.getFirstname());
                    lastname.setText(doctor.getLastname());
                    cnic.setText(doctor.getCnic());
                    age.setText(doctor.getAge());
                    gender.setText(doctor.getGender());
                    city.setText(doctor.getCity());
                    street.setText(doctor.getStreet());
                    username.setText(doctor.getUsername());
                    email.setText(doctor.getEmail());
                    phone.setText(doctor.getPhone());
                    qualification.setText(doctor.getQualification());
                    specialization.setText(doctor.getSpecialization());
                    role.setText(doctor.getRole());
                    Picasso.get().load(doctor.getImage()).into(user_image);

                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } else if(role_.equals("Patient")){
            qualification.setVisibility(View.GONE);
            specialization.setVisibility(View.GONE);
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child("Patient").child(mUser.getUid());
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    Patient patient = snapshot.getValue(Patient.class);

                    dbEmail = patient.getEmail();

                    firstname.setText(patient.getFirstname());
                    lastname.setText(patient.getLastname());
                    cnic.setText(patient.getCnic());
                    age.setText(patient.getAge());
                    gender.setText(patient.getGender());
                    city.setText(patient.getCity());
                    street.setText(patient.getStreet());
                    username.setText(patient.getUsername());
                    email.setText(patient.getEmail());
                    phone.setText(patient.getPhone());
                    role.setText(patient.getRole());
                    Picasso.get().load(patient.getImage()).into(user_image);
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }

        close.setOnClickListener(V -> {
            if(role_.equals("Patient")) {
                startActivity(new Intent(EditProfileActivity.this, PatientActivity.class));
                finish();
            }else if(role_.equals("Doctor")) {
                startActivity(new Intent(EditProfileActivity.this, DoctorActivity.class));
                finish();
            }

        });

        change_photo.setOnClickListener(V -> CropImage.activity().setAspectRatio(1, 1)
                .start(EditProfileActivity.this));

        user_image.setOnClickListener(V -> CropImage.activity()
                .setAspectRatio(1, 1)
                .start(EditProfileActivity.this));

        save.setOnClickListener(V -> {
            if(role_.equals("Doctor"))
                updateDoctorProfile(firstname.getText().toString(), lastname.getText().toString(), username.getText().toString(), gender.getText().toString(), city.getText().toString(), age.getText().toString(), street.getText().toString(), email.getText().toString(), phone.getText().toString(), qualification.getText().toString(), specialization.getText().toString(), role.getText().toString());
            else if(role_.equals("Patient"))
                updatePatientProfile(firstname.getText().toString(), lastname.getText().toString(), username.getText().toString(), gender.getText().toString(), city.getText().toString(), age.getText().toString(), street.getText().toString(), email.getText().toString(), phone.getText().toString(), role.getText().toString());
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            // Get the Uri of data
            mImageUri = result.getUri();
            user_image.setImageURI(mImageUri);
            uploadImage();
        } else {
            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void updateDoctorProfile(String firstname, String lastname, String username, String gender, String city, String age, String street, String email, String phone, String qualify, String specialize, String role){
        ProgressDialog pd = new ProgressDialog(EditProfileActivity.this);
        pd.setMessage("Updating...");
        pd.show();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child("Doctor").child(mUser.getUid());

        HashMap<String, Object> updatedUser = new HashMap<>();
        updatedUser.put("firstname", firstname);
        updatedUser.put("lastname", lastname);
        updatedUser.put("city", city);
        updatedUser.put("street", street);
        updatedUser.put("age", age);
        updatedUser.put("gender", gender);
        updatedUser.put("username", username);
        updatedUser.put("email", email);
        updatedUser.put("phone", phone);
        updatedUser.put("qualification", qualify);
        updatedUser.put("specialization", specialize);
        updatedUser.put("role", role);

        ref.updateChildren(updatedUser);

        if(!dbEmail.equals(email)){
            mUser.updateEmail(email);
        }
        Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        pd.dismiss();
    }

    private void updatePatientProfile(String firstname, String lastname, String username, String gender, String city, String age, String street, String email, String phone, String role){
        ProgressDialog pd = new ProgressDialog(EditProfileActivity.this);
        pd.setMessage("Updating...");
        pd.show();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child("Patient").child(mUser.getUid());

        HashMap<String, Object> updatedUser = new HashMap<>();
        updatedUser.put("firstname", firstname);
        updatedUser.put("lastname", lastname);
        updatedUser.put("city", city);
        updatedUser.put("street", street);
        updatedUser.put("age", age);
        updatedUser.put("gender", gender);
        updatedUser.put("username", username);
        updatedUser.put("email", email);
        updatedUser.put("phone", phone);
        updatedUser.put("role", role);

        ref.updateChildren(updatedUser);

        if(!dbEmail.equals(email)){
            mUser.updateEmail(email);
        }
        Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        pd.dismiss();
    }

    private void uploadImage(){
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading...");
        pd.show();

        if(mImageUri!=null){
            StorageReference imgRef = storageRef.child(System.currentTimeMillis()+"."+getFileExtension(mImageUri));
            uploadTask = imgRef.putFile(mImageUri);
            uploadTask.continueWithTask(task -> {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return imgRef.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if(task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    String myUri = downloadUri.toString();
                    String role_u = getIntent().getStringExtra("role");
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(role_u).child(mUser.getUid());
                    HashMap<String, Object> imgHash = new HashMap<>();
                    imgHash.put("image", myUri);

                    reference.updateChildren(imgHash);
                    pd.dismiss();
                }else {
                    Toast.makeText(getApplicationContext(), "Failed to Update", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            });
        } else {
            Toast.makeText(getApplicationContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }

}