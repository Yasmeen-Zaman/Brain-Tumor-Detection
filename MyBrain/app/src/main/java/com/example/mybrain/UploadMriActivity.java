package com.example.mybrain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mybrain.model.Doctor;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;

public class UploadMriActivity extends AppCompatActivity {

    FloatingActionButton addMri;
    ImageView mriImage, back;
    TextView result, upload;

    Uri imgUri;
    String mriUri="";
    StorageTask uploadTask;
    StorageReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_mri);

        AppBarLayout appbar = findViewById(R.id.mri_appbar);

        addMri = findViewById(R.id.addMri);
        mriImage = findViewById(R.id.img_mri);
        result = findViewById(R.id.result);
        back = findViewById(R.id.back);
        upload = findViewById(R.id.upload);

        back.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), DoctorActivity.class)));

        mRef = FirebaseStorage.getInstance().getReference("Brain");

        addMri.setOnClickListener(V -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), 22);
        });

        upload.setOnClickListener(V -> {
            if(!isInternetAvailable()){
                AlertDialog.Builder builder =new AlertDialog.Builder(this);
                builder.setTitle("No Internet Connection");
                builder.setMessage("Please check your connection");
                builder.setNegativeButton("close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
            uploadToFirebase();
        });

    }

    private String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploadToFirebase() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        if(imgUri != null){
            StorageReference fileRef = mRef.child(System.currentTimeMillis()+"."+getFileExtension(imgUri));
            uploadTask = fileRef.putFile(imgUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        mriUri = downloadUri.toString();

                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Brain");
                        String mriId = dbRef.push().getKey();
                        HashMap<String, Object> mriHashMap = new HashMap<>();
                        mriHashMap.put("mriId", mriId);
                        mriHashMap.put("mriImage", mriUri);
                        mriHashMap.put("result", result.getText().toString());
                        mriHashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());

                        dbRef.child(mriId).setValue(mriHashMap);
                        progressDialog.dismiss();
                        startActivity(new Intent(UploadMriActivity.this, DoctorActivity.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to upload", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Toast.makeText(getApplicationContext(), " "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else{
            Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    // Override onActivityResult method
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 22 && resultCode==RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            imgUri = data.getData();
            try {
                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
                mriImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
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

}