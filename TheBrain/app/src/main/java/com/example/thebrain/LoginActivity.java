package com.example.thebrain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    TextInputLayout email, password;
    Button login;
    TextView signup, forgot;
    ProgressDialog progressDialog;

    DatabaseReference roleRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        signup = findViewById(R.id.singupText);
        forgot = findViewById(R.id.forgotpass);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(LoginActivity.this);

    }

    public void Login(View view){
        if(!isInternetAvailable()){
            AlertDialog.Builder builder =new AlertDialog.Builder(this);
            builder.setTitle("No internet Connection");
            builder.setMessage("Please turn on internet connection to continue");
            builder.setNegativeButton("close", (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        if(!validateEmail() || !validatePassword()){
            return;
        } else {
            String userEmail = email.getEditText().getText().toString();
            String userPassword = password.getEditText().getText().toString();
            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            mAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    FirebaseUser mUser = mAuth.getCurrentUser();
                    roleRef = FirebaseDatabase.getInstance().getReference("role-user").child(mUser.getUid());
                    roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String role = snapshot.child("role").getValue(String.class);
                                switch (role) {
                                    case "Patient":
                                        Intent PatientIntent = new Intent(LoginActivity.this, PatientActivity.class);
                                        PatientIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        PatientIntent.putExtra("id", mUser.getUid());
                                        PatientIntent.putExtra("role", "Patient");
                                        startActivity(PatientIntent);
                                        finish();
                                        progressDialog.dismiss();
                                        break;
                                    case "Doctor":
                                        Intent DoctorIntent = new Intent(LoginActivity.this, DoctorActivity.class);
                                        DoctorIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        DoctorIntent.putExtra("id", mUser.getUid());
                                        DoctorIntent.putExtra("role", "Patient");
                                        startActivity(DoctorIntent);
                                        finish();
                                        progressDialog.dismiss();
                                        break;
                                    case "Receptionist":
                                        Intent ReceptionistIntent = new Intent(LoginActivity.this, ReceptionistActivity.class);
                                        ReceptionistIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        ReceptionistIntent.putExtra("id", mUser.getUid());
                                        ReceptionistIntent.putExtra("role", "Patient");
                                        startActivity(ReceptionistIntent);
                                        finish();
                                        progressDialog.dismiss();
                                        break;
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            progressDialog.dismiss();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Incorrect username or password!", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    public void SignUp(View view){
        startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        finish();
    }

    public void ForgotPassword(View view){
        startActivity(new Intent(LoginActivity.this, ForgotActivity.class));
    }

    private Boolean validateEmail() {
        String userEmail = email.getEditText().getText().toString().trim();
//        String noWhiteSpace = "\\A\\w{4,20}\\z";

        if(userEmail.isEmpty()){
            email.setError("Field cannot be empty");
            return false;
        } else{
            email.setError(null);
            email.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword(){
        String userPassword = password.getEditText().getText().toString();

        if(userPassword.isEmpty()){
            password.setError("Field cannot be empty");
            return false;
        } else {
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }

    public boolean isInternetAvailable(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}