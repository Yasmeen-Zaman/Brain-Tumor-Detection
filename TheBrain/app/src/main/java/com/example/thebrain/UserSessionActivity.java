package com.example.thebrain;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thebrain.dataAdapter.CommentAdapter;
import com.example.thebrain.datamodels.Comment;
import com.example.thebrain.datamodels.Session;
import com.example.thebrain.remote.APIUtils;
import com.example.thebrain.remote.FileService;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;

public class UserSessionActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 100;

    ImageView add_mri, close, comment_icon, mriImage;
    TextView tag, result, upload, total_comments;
    TextInputLayout comment_body;
    Button post_comment;
    RelativeLayout comment_rl;

    Uri imgUri;
    String mriUri="";
    String imagePath;
    StorageTask uploadTask;
    StorageReference mRef;
    FirebaseUser mUser;

    ProgressDialog progressDialog;
    FileService fileService;
    List<Comment> mMyCommentList;
    CommentAdapter mMyCommentsAdapter;
    RecyclerView comment_rv;

    String patient_id, session_id, username, userImage, last_key="0", parent_id;
    int comment_id = -1, ttl_cmt=0, pit_counter, man_counter, gli_counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_session);
        ttl_cmt=0;

// firebase authentication user and firebase storage
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mRef = FirebaseStorage.getInstance().getReference("Brain");

        DatabaseReference mGliRef = FirebaseDatabase.getInstance().getReference("prediction-counters").child("glioma");

        mGliRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    gli_counter = snapshot.getValue(Integer.class);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        DatabaseReference mPitRef = FirebaseDatabase.getInstance().getReference("prediction-counters").child("pitutary");

        mPitRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    pit_counter = snapshot.getValue(Integer.class);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        DatabaseReference mMenRef = FirebaseDatabase.getInstance().getReference("prediction-counters").child("meningioma");

        mMenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    man_counter = snapshot.getValue(Integer.class);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        //get intent data
        patient_id = getIntent().getStringExtra("patient_id");
        session_id = getIntent().getStringExtra("session_id");
        username = getIntent().getStringExtra("username");
        userImage = getIntent().getStringExtra("userImage");

        add_mri = findViewById(R.id.add_image);
        close = findViewById(R.id.close);
        comment_icon = findViewById(R.id.comment_icon);
        upload = findViewById(R.id.upload);
        tag = findViewById(R.id.tag);
        result = findViewById(R.id.result);
        mriImage = findViewById(R.id.mri_image);
        total_comments = findViewById(R.id.total_comments);

        post_comment = findViewById(R.id.post_comment);
        comment_body = findViewById(R.id.comment_body);
        comment_rl = findViewById(R.id.comment_rl);

        comment_rv = findViewById(R.id.comments_rv);
        comment_rv.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(UserSessionActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        comment_rv.setLayoutManager(linearLayoutManager);

        mMyCommentList = new ArrayList<>();
        mMyCommentsAdapter = new CommentAdapter(UserSessionActivity.this, mMyCommentList, patient_id, session_id, username, userImage);
        comment_rv.setAdapter(mMyCommentsAdapter);

        ReadComments(patient_id, session_id);

        if(ttl_cmt>0){
            total_comments.setVisibility(View.VISIBLE);
            total_comments.setText(""+ttl_cmt);
        } else{
            total_comments.setVisibility(View.GONE);
        }

        mMyCommentsAdapter.setWhenClickListener(comment -> {
            comment_rl.setVisibility(View.VISIBLE);
            parent_id = comment.getId();
            post_comment.setText("REPLY");
            comment_body.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(comment_body.getEditText(), InputMethodManager.SHOW_IMPLICIT);
        });

        // get service
        fileService = APIUtils.getFileService();

        close.setOnClickListener(V -> finish());

        getImage();

        comment_icon.setOnClickListener(V -> {
            parent_id = "";
            comment_rl.setVisibility(View.VISIBLE);
            post_comment.setText("POST");
            comment_body.getEditText().requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(comment_body.getEditText(), InputMethodManager.SHOW_IMPLICIT);
        });

        add_mri.setOnClickListener(V ->{
            try {
                if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(UserSessionActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                    if ((ActivityCompat.shouldShowRequestPermissionRationale(UserSessionActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(UserSessionActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE))) {
// do nothing
                    } else {
                        ActivityCompat.requestPermissions(UserSessionActivity.this, new String[]{
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_PERMISSION);
                    }
                } else {
                    Log.e("Else", "Else");
                    progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("Please wait...");
                    progressDialog.show();
                    showFileChooser();
                }
            } catch (Exception e){
                Log.e(TAG, "onAddMri"+ Arrays.toString(e.getStackTrace()));
            }
        });

        upload.setOnClickListener(V ->{
            try {
                if(!isInternetAvailable()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("No Internet Connection");
                    builder.setMessage("Please check your connection");
                    builder.setNegativeButton("close", (dialog, which) -> dialog.dismiss());
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                uploadToFirebase();
            } catch (Exception e){
                Log.e(TAG,"onClickUpload: "+ Arrays.toString(e.getStackTrace()));
            }
        });

        post_comment.setOnClickListener(V-> {
            String commented = comment_body.getEditText().getText().toString();
            if(!commented.equals("") && commented!=null) {
                if(post_comment.getText().equals("POST")) {
                    AddComment(commented, patient_id, session_id);
                    comment_body.getEditText().setText("");
                } else if(post_comment.getText().equals("REPLY")){
                    AddReply(commented, patient_id, session_id, parent_id);
                    comment_body.getEditText().setText("");
                }
            } else {
                Toast.makeText(UserSessionActivity.this, "Write a comment", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(getIntent());
            }
        });
    }

    private void AddReply(String replied, String patient_id, String session_id, String parent_id){
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss", Locale.getDefault());
        String date_time = dateFormat.format(date);

        last_key = String.valueOf(comment_id+1);

        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("users").child("Patient").child(patient_id).child("sessions").child(session_id).child("comments");
        HashMap<String, Object> commentHash = new HashMap<>();
        commentHash.put("id", last_key);
        commentHash.put("body", replied);
        commentHash.put("userId", mUser.getUid());
        commentHash.put("userImage", userImage);
        commentHash.put("username", username);
        commentHash.put("parentId", parent_id);
        commentHash.put("createdAt", date_time);

        commentRef.child(last_key).setValue(commentHash).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(UserSessionActivity.this, "Comment added!", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(getIntent());
            }
        });
        comment_rl.setVisibility(View.GONE);
    }

    private void AddComment(String commented, String patient_id, String session_id){
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss", Locale.getDefault());
        String date_time = dateFormat.format(date);

        last_key = String.valueOf(comment_id+1);

        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("users").child("Patient").child(patient_id).child("sessions").child(session_id).child("comments");
        HashMap<String, Object> commentHash = new HashMap<>();
        commentHash.put("id", last_key);
        commentHash.put("body", commented);
        commentHash.put("userId", mUser.getUid());
        commentHash.put("userImage", userImage);
        commentHash.put("username", username);
        commentHash.put("parentId", "");
        commentHash.put("createdAt", date_time);

        commentRef.child(last_key).setValue(commentHash).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(UserSessionActivity.this, "Comment added!", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(getIntent());
            }
        });
        comment_rl.setVisibility(View.GONE);
    }

    private void showFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), 0);
    }

    private String getFileExtension(Uri imgUri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(imgUri));
    }

    private void getImage(){
        DatabaseReference imageRef = FirebaseDatabase.getInstance().getReference("users").child("Patient").child(patient_id).child("sessions").child(session_id);
        imageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Session session = snapshot.getValue(Session.class);
                    if (snapshot.exists() && !session.getMriImage().isEmpty()) {
                        Picasso.get().load(session.getMriImage()).into(mriImage);
                        if (snapshot.child("permission").getValue(String.class).equals("true")) {
                            tag.setText("prediction Result: ");
                            result.setText(snapshot.child("tag").getValue(String.class));
                        } else if (snapshot.child("permission").getValue(String.class).equals("false")) {
                            tag.setText("prediction Result: ");
                            result.setText("Pending");
                        }
                    } else {
                        mriImage.setImageResource(R.drawable.no_image_uploadedpng);
                        tag.setText("No MRI Image Uploaded");
                        result.setText("");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void uploadToFirebase() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        if(imgUri != null){
            StorageReference fileRef = mRef.child(System.currentTimeMillis()+"."+getFileExtension(imgUri));
            uploadTask = fileRef.putFile(imgUri);
            uploadTask.continueWithTask(task -> {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return fileRef.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if(task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    mriUri = downloadUri.toString();

                    DatabaseReference sessionRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child("Patient")
                            .child(patient_id)
                            .child("sessions")
                            .child(session_id);
                    HashMap<String, Object> sessionHash = new HashMap<>();
                    sessionHash.put("mriImage", mriUri);
                    sessionHash.put("tag", result.getText().toString());
                    sessionRef.updateChildren(sessionHash);

                    DatabaseReference counterRef = FirebaseDatabase.getInstance().getReference("prediction-counters");
                    HashMap<String, Object> counterHash = new HashMap<>();
                    counterHash.put("glioma", gli_counter);
                    counterHash.put("meningioma", man_counter);
                    counterHash.put("pitutary", pit_counter);
                    counterRef.updateChildren(counterHash);

                } else {
                    Toast.makeText(getApplicationContext(), "Failed to upload", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), " "+e.getMessage(), Toast.LENGTH_SHORT).show());
        } else{
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK && data != null && data.getData() != null) {

            // Get the Uri of data
            imgUri = data.getData();
            imagePath = getPath(imgUri);

            try {
                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
                uploadToRemoteServer(imagePath);
                mriImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

    private String getPath(Uri uri){
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_idx);
        cursor.close();

        return result;
    }

    private void uploadToRemoteServer(String imagePath){
        if(imagePath!=null) {
            File file = new File(imagePath);
            RequestBody requestBody = RequestBody.create(file, MediaType.parse("multipart/form-data"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
            Call<ResponseBody> call = fileService.uploadImage(body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                    if(response.isSuccessful()){
                        if(response.body()!=null) {
                            try {
                                String tumor= response.body().string().split(":")[1];
                                tag.setText("prediction Result: ");
                                result.setText(tumor.replace("}", "").trim());
                                if(result.getText().equals("\"Glioma\"")){
                                    gli_counter++;
                                } else if(result.getText().equals("\"Pitutary\"")){
                                    pit_counter++;
                                } else if(result.getText().equals("\"Meningioma\"")){
                                    man_counter++;
                                }
                                progressDialog.dismiss();
                            } catch (IOException e) {
                                e.printStackTrace();
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Server not responding", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Server not responding", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                    Toast.makeText(getApplicationContext(), "ERROR: "+t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            progressDialog.dismiss();
        }
    }

    private void ReadComments(String patient_id, String session_id){
        ProgressDialog progressDialog = new ProgressDialog(UserSessionActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        DatabaseReference myCommentRef = FirebaseDatabase.getInstance().getReference("users").child("Patient").child(patient_id).child("sessions").child(session_id).child("comments");
        myCommentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    mMyCommentList.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        Comment myComment = dataSnapshot.getValue(Comment.class);
                        last_key = myComment.getId();
                        comment_id = Integer.parseInt(last_key);
                        if(myComment.getParentId().equals("") || myComment.getParentId()==null){
                            ttl_cmt = ttl_cmt+1;
                            mMyCommentList.add(myComment);
                        }
                    }
                    mMyCommentsAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });
    }

    private boolean isInternetAvailable(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}