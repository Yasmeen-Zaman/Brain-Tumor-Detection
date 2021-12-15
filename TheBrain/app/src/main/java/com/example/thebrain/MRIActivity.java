package com.example.thebrain;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thebrain.remote.APIUtils;
import com.example.thebrain.remote.FileService;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class MRIActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS = 100;

    Uri imgUri;
    String imagePath;
    StorageReference mRef;

    ImageView mri, close;
    TextView result;
    ImageView add_image;

    ProgressDialog progressDialog;
    FileService fileService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mriactivity);

        mri = findViewById(R.id.mri);
        close = findViewById(R.id.close);
        result = findViewById(R.id.result);
        add_image = findViewById(R.id.add);

        // get service
        fileService = APIUtils.getFileService();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Select MRI image");
        progressDialog.setMessage("Please wait...");

        mRef = FirebaseStorage.getInstance().getReference("Brain");

        close.setOnClickListener(V-> finish());

        add_image.setOnClickListener(V -> {
            if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                if ((ActivityCompat.shouldShowRequestPermissionRationale(MRIActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(MRIActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE))) {

                } else {
                    ActivityCompat.requestPermissions(MRIActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_PERMISSIONS);
                }
            } else {
                Log.e("Else", "Else");
                progressDialog.show();
                showFileChooser();
            }
        });
    }

    private void showFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), 0);

    }

    private String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_idx);
        cursor.close();

        return result;
    }

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
                uploadToRemoteServer();
                mri.setImageBitmap(bitmap);
            } catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

    private void uploadToRemoteServer(){
        if(!isInternetAvailable()){
            AlertDialog.Builder builder =new AlertDialog.Builder(this);
            builder.setTitle("No Internet Connection");
            builder.setMessage("Please check your connection");
            builder.setNegativeButton("close", (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
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
                                result.setVisibility(View.VISIBLE);
                                String tumor= response.body().string().split(":")[1];
                                result.setText("Tumor Type: "+tumor.replace("}", "").trim());
                                Toast.makeText(getApplicationContext(), "Tumor Type: "+tumor.replace("}", ""), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
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

    private boolean isInternetAvailable(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}