package com.example.mybrain;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mybrain.model.Doctor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputLayout doctor_refer_id, userName, fullName, qualification, specialization, phone, email, password, rePassword;
    Button signup;
    TextView signin;
    ImageView back;

    AutoCompleteTextView roles;

    FirebaseDatabase rootNode;
    DatabaseReference reference, roleRef;
    StorageReference storageRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    ProgressDialog progressDialog;

    String full_name, qualif, specialize, email_ad, phone_no, password_user, role_user, username, doctor_ref_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        fullName = findViewById(R.id.fullname);
        userName = findViewById(R.id.username);
        doctor_refer_id = findViewById(R.id.reference_id);
        specialization = findViewById(R.id.specialization);
        qualification = findViewById(R.id.qualification);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        rePassword = findViewById(R.id.confirmPass);
        back = findViewById(R.id.back);
        roles = findViewById(R.id.role);

        signup = findViewById(R.id.signup);
        signin = findViewById(R.id.signinText);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        ArrayList<String> roleList = getRoles();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, roleList);

        //Set adapter
        roles.setAdapter(adapter);

        roles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                role_user = parent.getItemAtPosition(position).toString();
                if(role_user.equals("Doctor")){
                    qualification.setVisibility(View.VISIBLE);
                    specialization.setVisibility(View.VISIBLE);
                } else {
                    qualification.setVisibility(View.GONE);
                    specialization.setVisibility(View.GONE);
                }
            }
        });

        signup.setOnClickListener(v -> {

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

            if(!validateReferenceId() |!validateFullName() | !validateEmail() | !validatePassword() | !validatePhone() | !validateRePassword()){
                return;
            }

            progressDialog.show();

            rootNode = FirebaseDatabase.getInstance();
            reference = rootNode.getReference("users");
            roleRef = rootNode.getReference("role-user");
            storageRef = FirebaseStorage.getInstance().getReference("images");
            mAuth = FirebaseAuth.getInstance();

            username = userName.getEditText().getText().toString();
            doctor_ref_id = doctor_refer_id.getEditText().getText().toString();
            full_name = fullName.getEditText().getText().toString();
            qualif = qualification.getEditText().getText().toString();
            specialize = specialization.getEditText().getText().toString();
            email_ad = email.getEditText().getText().toString().trim();
            phone_no = phone.getEditText().getText().toString().trim();
            password_user = password.getEditText().getText().toString().trim();

            if(!role_user.isEmpty() && !role_user.equals("Select Role") && role_user != null){
                Register(doctor_ref_id, username, full_name, qualif, specialize, email_ad, phone_no, password_user, role_user);

            } else {
                roles.setError("Select role!");
                return;
            }
        });

        back.setOnClickListener(V ->  {
            Intent goBack = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(goBack);
            finish();
        });

        signin.setOnClickListener(v -> {
            Intent loginPage = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(loginPage);
            finish();
        });

    }

    private void Register(String doctor_ref_id, String username, String full_name, String qualif, String specialize, String email_ad, String phone_no, String password_user, String role_user){
        mAuth.createUserWithEmailAndPassword(email_ad, password_user).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if(task.isComplete()){
                    mUser = mAuth.getCurrentUser();
                    String userId = mUser.getUid();

                    roleRef = FirebaseDatabase.getInstance().getReference("role-user").child(userId);

                    HashMap<String, Object> roleHash = new HashMap<>();
                    roleHash.put("id", userId);
                    roleHash.put("role", role_user);
//                    roleHash.put("reference-id", doctor_ref_id);

                    roleRef.setValue(roleHash);

                    if(role_user.equals("Doctor")){

                        reference = FirebaseDatabase.getInstance().getReference().child("users").child(role_user).child(userId);

                        HashMap<String, Object> docHashMap = new HashMap<>();
                        docHashMap.put("id", userId);
                        docHashMap.put("reference_id", doctor_ref_id);
                        docHashMap.put("username", username);
                        docHashMap.put("fullname", full_name);
                        docHashMap.put("email", email_ad);
                        docHashMap.put("qualification", qualif);
                        docHashMap.put("specialization", specialize);
                        docHashMap.put("phone", phone_no);
                        docHashMap.put("role", role_user);
                        docHashMap.put("status", "Active");
                        docHashMap.put("password", password_user);
                        docHashMap.put("image", "https://firebasestorage.googleapis.com/v0/b/mybrain-c35b6.appspot.com/o/images%2Fdoctor.png?alt=media&token=be2e94a8-3668-41f8-b433-0a6c2211e75a");

                        reference.setValue(docHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if(task.isComplete()){
                                    Intent goToProfile = new Intent(SignupActivity.this, DoctorActivity.class);
                                    goToProfile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                                    goToProfile.putExtra("role", role_user);
                                    goToProfile.putExtra("id", userId);

                                    startActivity(goToProfile);
                                }
                            }
                        });

                    } else if(role_user.equals("Patient")) {
//                        DatabaseReference all_doctors
                        Query checkReference= FirebaseDatabase.getInstance().getReference("users").child("Doctor").orderByChild("reference_id");

                        checkReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                    Doctor doctor = dataSnapshot.getValue(Doctor.class);
                                    Toast.makeText(getApplicationContext(), ""+doctor.getReference_id(), Toast.LENGTH_SHORT).show();
                                    if(doctor.getReference_id().equals(doctor_ref_id)){
                                       FirebaseDatabase.getInstance().getReference("Connection").child(doctor.getId()).child(userId).setValue(true);
                                       break;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });

                        reference = FirebaseDatabase.getInstance().getReference().child("users").child(role_user).child(userId);

                        HashMap<String, Object> patHashMap = new HashMap<>();
                        patHashMap.put("id", userId);
                        patHashMap.put("username", username);
                        patHashMap.put("fullname", full_name);
                        patHashMap.put("email", email_ad);
                        patHashMap.put("phone", phone_no);
                        patHashMap.put("role", role_user);
                        patHashMap.put("status", "Active");
                        patHashMap.put("password", password_user);
                        patHashMap.put("image", "https://firebasestorage.googleapis.com/v0/b/mybrain-c35b6.appspot.com/o/images%2Fuser.png?alt=media&token=0b203504-79f3-425f-868f-643546b1439c");

                        reference.setValue(patHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if(task.isComplete()){
                                    Intent goToProfile = new Intent(SignupActivity.this, PatientActivity.class);
                                    goToProfile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    goToProfile.putExtra("role", role_user);
                                    goToProfile.putExtra("id", userId);
                                    startActivity(goToProfile);
                                }
                            }
                        });
                    }
                    progressDialog.dismiss();
                }
            }
        });
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

    private ArrayList<String> getRoles() {
        ArrayList<String> userRole = new ArrayList<>();
        userRole.add("Select Role");
        userRole.add("Doctor");
        userRole.add("Patient");

        return userRole;
    }

    private Boolean validateFullName(){
        String val = fullName.getEditText().getText().toString();
        if(val.isEmpty()){
            fullName.setError("Field cannot be empty");
            return false;
        } else {
            fullName.setError(null);
            fullName.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validateReferenceId(){
        String val = doctor_refer_id.getEditText().getText().toString();
        if(val.isEmpty()){
            doctor_refer_id.setError("Field cannot be empty");
            return false;
        } else {
            fullName.setError(null);
            fullName.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validateEmail(){
        String val = email.getEditText().getText().toString().trim();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if(val.isEmpty()){
            email.setError("Field cannot be empty");
            return false;
        } else if(val.matches(emailPattern) || Patterns.EMAIL_ADDRESS.matcher(val).matches()){
            email.setError(null);
            email.setErrorEnabled(false);
            return true;
        } else {
            email.setError("Invalid email address!");
            email.setErrorEnabled(false);
            return false;
        }

    }

    private Boolean validatePhone(){
        String val = phone.getEditText().getText().toString().trim();


        if(val.isEmpty()){
            phone.setError("Field cannot be empty");
            return false;
        } else {
            phone.setError(null);
            phone.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validatePassword(){
        String val = password.getEditText().getText().toString();
        String passPattern = "^(?=.*[0-9])"
                + "(?=.*[a-z])(?=.*[A-Z])"
                + "(?=.*[@#$%^&+=])"
                + "(?=\\S+$).{8,20}$";

        Pattern p = Pattern.compile(passPattern);
        Matcher m = p.matcher(val);

        if(val.isEmpty()){
            password.setError("Field cannot be empty");
            return false;
        } else if(m.matches()){
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        } else {
            password.setError("Password too weak!");
            return false;
        }

    }

    private Boolean validateRePassword(){
        String val = rePassword.getEditText().getText().toString();
        password_user = password.getEditText().getText().toString().trim();

        Toast.makeText(getApplicationContext(),""+val + " "+password_user, Toast.LENGTH_SHORT).show();

        if(val.isEmpty()){
            rePassword.setError("Field cannot be empty!");
            return false;
        } else if(val.equals(password_user)){
            rePassword.setError(null);
            rePassword.setErrorEnabled(false);
            return true;
        } else{
            rePassword.setError("Password did not match!");
            return false;
        }
    }

}