   package com.example.mybrain;

   import android.app.AlertDialog;
   import android.app.ProgressDialog;
   import android.content.Context;
   import android.content.DialogInterface;
   import android.content.Intent;
   import android.net.ConnectivityManager;
   import android.net.NetworkInfo;
   import android.os.Bundle;
   import android.widget.Button;
   import android.widget.TextView;
   import android.widget.Toast;

   import com.google.firebase.auth.FirebaseAuth;
   import com.google.firebase.auth.FirebaseUser;
   import com.google.firebase.database.DataSnapshot;
   import com.google.firebase.database.DatabaseError;
   import com.google.firebase.database.DatabaseReference;
   import com.google.firebase.database.FirebaseDatabase;
   import com.google.firebase.database.ValueEventListener;
   import com.google.firebase.storage.StorageReference;

   import org.jetbrains.annotations.NotNull;

   import androidx.annotation.NonNull;
   import androidx.appcompat.app.AppCompatActivity;

   public class LoginActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputLayout email, password;
    Button login;
    TextView signup, forgot;
    ProgressDialog progressDialog;

    DatabaseReference reference, roleRef;
    StorageReference storageReference;
    FirebaseAuth mAuth;

    int bool = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(!isInternetAvailable()){
            AlertDialog.Builder builder =new AlertDialog.Builder(this);
            builder.setTitle("No internet Connection");
            builder.setMessage("Please turn on internet connection to continue");
            builder.setNegativeButton("close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        signup = findViewById(R.id.singupText);
        forgot = findViewById(R.id.forgotpass);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(getApplicationContext());

        login.setOnClickListener(v -> {

            if(!validateEmail() || !validatePassword()){
                return;
            }
            else{

                ProgressDialog pd = new ProgressDialog(LoginActivity.this);
                pd.setMessage("Please wait...");
                pd.show();

                String UserEnteredEmail = email.getEditText().getText().toString().trim();
                String UserEnteredPassword = password.getEditText().getText().toString().trim();

                mAuth.signInWithEmailAndPassword(UserEnteredEmail, UserEnteredPassword).addOnCompleteListener(LoginActivity.this, task -> {
                    if(task.isSuccessful()){

                        FirebaseUser mUser = mAuth.getCurrentUser();
                        String userId = mUser.getUid();
                        roleRef = FirebaseDatabase.getInstance().getReference("role-user").child(userId);
                        roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    String uRole = snapshot.child("role").getValue(String.class);
                                    reference = FirebaseDatabase.getInstance().getReference("users").child(uRole).child(userId);
                                    reference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                pd.dismiss();
                                                if(uRole.equals("Doctor")){
                                                    Intent goToDoctor = new Intent(LoginActivity.this, DoctorActivity.class);
                                                    goToDoctor.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    goToDoctor.putExtra("role", uRole);
                                                    goToDoctor.putExtra("id", userId);

                                                    startActivity(goToDoctor);
                                                } else if(uRole.equals("Patient")){
                                                    Intent goToPatient = new Intent(LoginActivity.this, PatientActivity.class);
                                                    goToPatient.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    goToPatient.putExtra("role", uRole);
                                                    goToPatient.putExtra("id", userId);

                                                    startActivity(goToPatient);
                                                } else if(uRole.equals("Receptionist")){
                                                    Intent goToReception = new Intent(LoginActivity.this, ReceptionistActivity.class);
                                                    goToReception.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    goToReception.putExtra("role", uRole);
                                                    goToReception.putExtra("id", userId);

                                                    startActivity(goToReception);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                            pd.dismiss();
                                        }
                                    });
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                pd.dismiss();
                            }
                        });
                    } else {
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(), "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });

        signup.setOnClickListener(v -> {
            Intent register = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(register);
            finish();
        });

        forgot.setOnClickListener(v -> {
            Intent reset = new Intent(LoginActivity.this, ForgotActivity.class);
            startActivity(reset);
            finish();
        });
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
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        if(isConnected) {
            return true;
        } else{
            return false;
        }
    }
}