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
import android.widget.LinearLayout;
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

public class SingleSessionActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS = 100;

    ImageView addMri, permit;
    ImageView mriImage, back, comment_icon;
    TextInputLayout comment_body;
    TextView result, upload, tag, title, total_comments, permission_text;
    Button post_comment;
    RecyclerView comments_rv;
    RelativeLayout title_, comment_rl;
    LinearLayout below_line;

    Uri imgUri;
    String mriUri="";
    String imagePath;
    StorageTask uploadTask;
    StorageReference mRef;
    FirebaseUser mUser;

    ProgressDialog progressDialog;
    FileService fileService;
    List<Comment> mCommentList;
    CommentAdapter mCommentAdapter;

    String patient_id, session_id, caller, username, userImage, last_key="0", parent_id;
    int tot_comments=0, comment_id=-1, pit_counter, man_counter, gli_counter;

    String flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_session);

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

        // get service
        fileService = APIUtils.getFileService();

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mRef = FirebaseStorage.getInstance().getReference("Brain");

        patient_id = getIntent().getStringExtra("patient_id");
        session_id = getIntent().getStringExtra("session_id");
        username = getIntent().getStringExtra("username");
        userImage = getIntent().getStringExtra("userImage");
        caller = getIntent().getStringExtra("caller");

        addMri = findViewById(R.id.addMri);
        permission_text = findViewById(R.id.permission_text);
        permit = findViewById(R.id.permit);
        mriImage = findViewById(R.id.img_mri);
        result = findViewById(R.id.result);
        back = findViewById(R.id.back);
        comment_icon = findViewById(R.id.comment_icon);
        title_ = findViewById(R.id.title_);
        upload = findViewById(R.id.upload);
        tag = findViewById(R.id.tag);
        title = findViewById(R.id.comment_title);
        total_comments = findViewById(R.id.total_comments);
        below_line = findViewById(R.id.below_line);
        comments_rv = findViewById(R.id.comments_rv);

        post_comment = findViewById(R.id.post_comment);
        comment_rl = findViewById(R.id.comment_rl);
        comment_body = findViewById(R.id.comment_body);

        comments_rv.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SingleSessionActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        comments_rv.setLayoutManager(linearLayoutManager);
        mCommentList = new ArrayList<>();
        mCommentAdapter = new CommentAdapter(SingleSessionActivity.this, mCommentList, patient_id, session_id, username, userImage);
        comments_rv.setAdapter(mCommentAdapter);

        readComments(patient_id, session_id);

        mCommentAdapter.setWhenClickListener(comment -> {
            comment_rl.setVisibility(View.VISIBLE);
            parent_id = comment.getId();
            comment_body.requestFocus();
            post_comment.setText("REPLY");
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(comment_body.getEditText(), InputMethodManager.SHOW_IMPLICIT);
        });

        if(tot_comments>0){
            total_comments.setVisibility(View.VISIBLE);
            total_comments.setText(""+tot_comments);
        } else{
            total_comments.setVisibility(View.GONE);
        }

        checkPatientPermission(patient_id, session_id);

        permit.setOnClickListener(V -> {
            if(flag.equals("false")){
                flag = "true";
                permit.setImageResource(R.drawable.ic_toggle_on);
                permit.setContentDescription("allow");
            } else if(flag.equals("true")){
                flag = "false";
                permit.setImageResource(R.drawable.ic_toggle_off);
                permit.setContentDescription("disallow");
            }
            allowPatient(flag);
        });

        back.setOnClickListener(v -> finish());

        getImage();

        addMri.setOnClickListener(V -> {
            try {
                if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                    if ((ActivityCompat.shouldShowRequestPermissionRationale(SingleSessionActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(SingleSessionActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE))) {

                    } else {
                        ActivityCompat.requestPermissions(SingleSessionActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_PERMISSIONS);
                    }
                } else {
                    Log.e("Else", "Else");
                    progressDialog = new ProgressDialog(SingleSessionActivity.this);
                    progressDialog.setMessage("Wait...");
                    progressDialog.show();
                    showFileChooser();
                }
            } catch (Exception e){
                Log.e(TAG, "ADD MRI: "+ Arrays.toString(e.getStackTrace()));
            }
        });

        upload.setOnClickListener(V -> {
            try {
                if (!isInternetAvailable()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("No Internet Connection");
                    builder.setMessage("Please check your connection");
                    builder.setNegativeButton("close", (dialog, which) -> dialog.dismiss());
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                uploadToFirebase();
            } catch (Exception e){
                Log.e(TAG, "Upload: "+ Arrays.toString(e.getStackTrace()));
            }
        });

        comment_icon.setOnClickListener(V->{
            parent_id="";
            comment_rl.setVisibility(View.VISIBLE);
            post_comment.setText("POST");
            comment_body.getEditText().requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(comment_body.getEditText(), InputMethodManager.SHOW_IMPLICIT);
        });

        post_comment.setOnClickListener(V->{
            String commented = comment_body.getEditText().getText().toString();
            if(!commented.equals("") && commented!=null) {
                if(post_comment.getText().equals("POST")){
                    AddComment(commented, patient_id, session_id);
                    comment_body.getEditText().setText("");
                } else if(post_comment.getText().equals("REPLY")){
                    AddReply(commented, patient_id, session_id, parent_id);
                    comment_body.getEditText().setText("");
                }
            } else {
                Toast.makeText(SingleSessionActivity.this, "Write a comment", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkPatientPermission(String patient_id, String session_id){
        DatabaseReference mDbRef = FirebaseDatabase.getInstance().getReference("users").child("Patient").child(patient_id).child("sessions").child(session_id);
        mDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    flag = snapshot.child("permission").getValue(String.class);
                    if(flag.equals("true"))
                        permit.setImageResource(R.drawable.ic_toggle_on);
                    else if(flag.equals("false"))
                        permit.setImageResource(R.drawable.ic_toggle_off);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

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
                Toast.makeText(SingleSessionActivity.this, "Reply added!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SingleSessionActivity.this, "Comment added!", Toast.LENGTH_SHORT).show();
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
                Session session = snapshot.getValue(Session.class);
                if(snapshot.exists() && !session.getMriImage().isEmpty()) {
                    Picasso.get().load(session.getMriImage()).into(mriImage);
                    tag.setText("prediction Result: ");
                    result.setText(session.getTag());
                } else {
                    tag.setText("No MRI Uploaded");
                    result.setText("");
                    mriImage.setImageResource(R.drawable.no_image_uploadedpng);
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

    private void allowPatient(String flag){
        DatabaseReference mDbRef = FirebaseDatabase.getInstance().getReference("users").child("Patient").child(patient_id).child("sessions").child(session_id);
        HashMap<String, Object> permissionHash = new HashMap<>();
        permissionHash.put("permission", flag);
        mDbRef.updateChildren(permissionHash);

    }

    // Override onActivityResult method
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    private void readComments(String patient_id, String session_id){
        tot_comments=0;
        ProgressDialog progressDialog = new ProgressDialog(SingleSessionActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("users").child("Patient").child(patient_id).child("sessions").child(session_id).child("comments");
        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    mCommentList.clear();
                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                        Comment comment = dataSnapshot.getValue(Comment.class);
                        last_key = comment.getId();
                        comment_id = Integer.parseInt(last_key);
                        if(comment.getParentId().equals("")){
                            tot_comments = tot_comments+1;
                            mCommentList.add(comment);
                        }
                    }
                    mCommentAdapter.notifyDataSetChanged();
                    total_comments.setText(""+tot_comments);
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });
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
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Server not responding", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Server not responding", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                    Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                }
            });
            progressDialog.dismiss();
        }
    }

    private boolean isInternetAvailable(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}