package com.example.mybrain;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

import com.example.mybrain.model.Doctor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class AddUserActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputLayout doctor_refer_id, userName, fullName, qualification, specialization, phone, email, password, rePassword;
    Button add;
    TextView title_bar;
    ImageView close;

    String role, ref_id, username, fullname, qualify, specialize, phone_, email_, password_;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mDbRef, roleRef;
    StorageReference storageReference;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        mAuth = FirebaseAuth.getInstance();

        close = findViewById(R.id.close);
        title_bar = findViewById(R.id.title_bar);
        add = findViewById(R.id.add);

        doctor_refer_id = findViewById(R.id.reference_id);
        userName = findViewById(R.id.username);
        fullName = findViewById(R.id.fullname);
        qualification = findViewById(R.id.qualification);
        specialization = findViewById(R.id.specialization);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        rePassword = findViewById(R.id.confirmPass);

        progressDialog = new ProgressDialog(this);

        role = getIntent().getStringExtra("role");
        if(role.equals("Doctor")){
            progressDialog.setTitle("Adding Doctor");
            title_bar.setText("Add Doctor");
            add.setText("Add Doctor");
            specialization.setVisibility(View.VISIBLE);
            qualification.setVisibility(View.VISIBLE);
        } else if(role.equals("Patient")){
            progressDialog.setTitle("Adding Patient");
            title_bar.setText("Add Patient");
            add.setText("Add Patient");
            specialization.setVisibility(View.GONE);
            qualification.setVisibility(View.GONE);
        }

        progressDialog.setMessage("Please wait...");

        close.setOnClickListener(V -> {
            finish();
        });

        add.setOnClickListener(V -> {
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

            progressDialog.show();

            if(!validateEmail() | !validateFullName() | !validatePassword() | !validatePhone() | !validateReferenceId() | !validateRePassword()){
                return;
            }

            username = userName.getEditText().getText().toString();
            fullname = fullName.getEditText().getText().toString();
            email_ = email.getEditText().getText().toString();
            phone_ = phone.getEditText().getText().toString();
            password_ = password.getEditText().getText().toString();
            ref_id = doctor_refer_id.getEditText().getText().toString();
            qualify = qualification.getEditText().getText().toString();
            specialize = specialization.getEditText().getText().toString();

            registerUser(ref_id, username, fullname, email_, role, phone_, password_, qualify, specialize);

        });

//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);
    }

    private void registerUser(String ref_id, String username, String fullname, String email_, String role, String phone_, String password_, String qualify, String specialize){
        mAuth.createUserWithEmailAndPassword(email_, password_).addOnCompleteListener(AddUserActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mUser = mAuth.getCurrentUser();
                    roleRef = FirebaseDatabase.getInstance().getReference("role-user").child(mUser.getUid());
                    HashMap<String, Object> roleHash = new HashMap<>();
                    roleHash.put("id", mUser.getUid());
                    roleHash.put("role", role);
                    roleRef.setValue(roleHash);

                    mDbRef = FirebaseDatabase.getInstance().getReference("users").child(role).child(mUser.getUid());
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("id", mUser.getUid());
                    hashMap.put("username", username);
                    hashMap.put("fullname", fullname);
                    hashMap.put("role", role);
                    hashMap.put("password", password_);
                    hashMap.put("email", email_);
                    hashMap.put("phone", phone_);
                    hashMap.put("status", "Active");

                    if (role.equals("Doctor")) {
                        hashMap.put("reference_id", ref_id);
                        hashMap.put("qualification", qualify);
                        hashMap.put("specialization", specialize);
                        hashMap.put("image", "https://firebasestorage.googleapis.com/v0/b/mybrain-c35b6.appspot.com/o/images%2Fdoctor.png?alt=media&token=be2e94a8-3668-41f8-b433-0a6c2211e75a");
                    } else if (role.equals("Patient")) {
                        Query checkReference= FirebaseDatabase.getInstance().getReference("users").child("Doctor").orderByChild("reference_id");

                        checkReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                    Doctor doctor = dataSnapshot.getValue(Doctor.class);
                                    Toast.makeText(getApplicationContext(), ""+doctor.getReference_id(), Toast.LENGTH_SHORT).show();
                                    if(doctor.getReference_id().equals(ref_id)){
                                        FirebaseDatabase.getInstance().getReference("Connection").child(doctor.getId()).child(mUser.getUid()).setValue(true);
                                        break;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                        hashMap.put("image", "https://firebasestorage.googleapis.com/v0/b/mybrain-c35b6.appspot.com/o/images%2Fuser.png?alt=media&token=0b203504-79f3-425f-868f-643546b1439c");
                    }

                    mDbRef.setValue(hashMap);

                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "User successfully added", Toast.LENGTH_SHORT).show();

                    sendRegistrationEmail(email_, fullname, password_);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    private void sendRegistrationEmail(String email_, String fullname, String password_){
        String userEmail = "yasmeenzaman.cs17@iba-suk.edu.pk";
        String password = "yasmeenzaman12";
        String messageToSend = "Wel Come "+ fullname +" to Brain Tumor Detection System.\nYou have been successfully registered. Your login details are:\nEmail: "+email_+"\nPassword: "+password_;
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable","true");
        props.put("mail.smtp.host","smtp.gmail.com");
        props.put("mail.smtp.port","587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator(){
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(userEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email_));
            message.setSubject("Registration Successful");
            message.setText(messageToSend);
            Transport.send(message);
            Toast.makeText(getApplicationContext(), "Email sent Successfully!", Toast.LENGTH_SHORT).show();
        } catch (MessagingException e){
            throw new RuntimeException(e);
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
                + "(?=.*[@!#$%^&+=])"
                + "(?=\\S+$).{6,20}$";

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
            password.setError("Password must contain at least: \n1 Capital Character (A-Z), \n1 Special Character (@!#$%^&+=) and \nNumber (0-9)!\nLength should no be less than 6");
            return false;
        }

    }

    private Boolean validateRePassword(){
        String val = rePassword.getEditText().getText().toString();
        password_ = password.getEditText().getText().toString().trim();

        if(val.isEmpty()){
            rePassword.setError("Field cannot be empty!");
            return false;
        } else if(val.equals(password_)){
            rePassword.setError(null);
            rePassword.setErrorEnabled(false);
            return true;
        } else{
            rePassword.setError("Password did not match!");
            return false;
        }
    }

}