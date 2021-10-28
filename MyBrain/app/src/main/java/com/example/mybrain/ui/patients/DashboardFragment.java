package com.example.mybrain.ui.patients;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mybrain.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import static android.app.Activity.RESULT_OK;

public class DashboardFragment extends Fragment {

    Uri imageUri;
    // request code
    private final int PICK_IMAGE_REQUEST = 22;

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser mUser;
    StorageTask uploadTask;

    ImageView brainImg, send_comment, userDp;
    TextView brainTag;
    Button uploadBtn;
    TextView result;
    com.google.android.material.textfield.TextInputLayout current_comment;
    RecyclerView all_comments;


    String postId, pubId, userRole, myUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        uploadBtn = root.findViewById(R.id.uploadBtn);
        brainImg = root.findViewById(R.id.brainImg);
        brainTag = root.findViewById(R.id.brainTag);
        result = root.findViewById(R.id.result);

        storage = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Brain");

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });


        return root;
    }

    private void uploadImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), PICK_IMAGE_REQUEST);

        uploadToFirebase();

    }

    private String getFileExtension(Uri imageUri){
        ContentResolver cr = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getFileExtensionFromUrl(cr.getType(imageUri));
    }

    private void uploadToFirebase() {
        if (imageUri != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));

            uploadTask = ref.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull @NotNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Uri> task) {
                    if(task.isComplete()){
                        Uri uri = task.getResult();
                        myUri = uri.toString();
                        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Brain");
                        postId = mDatabaseRef.push().getKey();

                        HashMap<String, Object> sessionPost = new HashMap<>();
                        sessionPost.put("postId", postId);
                        sessionPost.put("postImage", myUri);
                        sessionPost.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        sessionPost.put("result", "");

                        mDatabaseRef.child(postId).setValue(sessionPost);

                        progressDialog.dismiss();
                    } else {
                        Toast.makeText(getContext(),"Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(e -> {
                // Error, Image not uploaded
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

//            ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
//                // Image uploaded successfully
//                // Dismiss dialog
//                progressDialog.dismiss();
//                Toast.makeText(getContext(), "Image Uploaded!!", Toast.LENGTH_SHORT).show();
//
//                ImageModel model = new ImageModel(taskSnapshot.getStorage().getDownloadUrl().toString());
//                String imageId = mDatabaseRef.push().getKey();
//                mDatabaseRef.child(imageId).setValue(model);
//
//            }).addOnFailureListener(e -> {
//                // Error, Image not uploaded
//                progressDialog.dismiss();
//                Toast.makeText(getContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
//            }).addOnProgressListener(taskSnapshot -> {
//                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
//                progressDialog.setMessage("Uploaded " + (int)progress + "%"); });
        }
    }

    // Override onActivityResult method
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode==RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            imageUri = data.getData();
            try {
                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                brainImg.setImageBitmap(bitmap);
            } catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

}
