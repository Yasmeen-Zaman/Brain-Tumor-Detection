package com.example.thebrain;

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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thebrain.datamodels.Doctor;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class AddUserActivity extends AppCompatActivity {

    TextInputLayout doctor_refer_id, userName, firstName, lastName, cnic, age, city, street, qualification, specialization, phone, email, password, rePassword;
    Button add;
    TextView title_bar;
    ImageView close;

    AutoCompleteTextView gender;

    String role, ref_id, username, firstname, lastname, city_, street_, age_, gender_, cnicNo, qualify, specialize, phone_, email_, password_;
    String caller;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mDbRef, roleRef;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        close = findViewById(R.id.close);
        title_bar = findViewById(R.id.title_bar);
        add = findViewById(R.id.add);

        doctor_refer_id = findViewById(R.id.reference_id);
        userName = findViewById(R.id.username);
        firstName = findViewById(R.id.firstname);
        lastName = findViewById(R.id.lastname);
        cnic = findViewById(R.id.cnic);
        age = findViewById(R.id.age);
        city = findViewById(R.id.city);
        street = findViewById(R.id.street);
        qualification = findViewById(R.id.qualification);
        specialization = findViewById(R.id.specialization);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        rePassword = findViewById(R.id.confirmPass);
        gender = findViewById(R.id.gender);

        progressDialog = new ProgressDialog(this);

        role = getIntent().getStringExtra("role");
        caller = getIntent().getStringExtra("caller");

        if(caller.equals("Receptionist")) {
            if (role.equals("Doctor")) {
                progressDialog.setTitle("Adding Doctor");
                title_bar.setText("Add Doctor");
                add.setText("Add Doctor");
                specialization.setVisibility(View.VISIBLE);
                qualification.setVisibility(View.VISIBLE);
            } else if (role.equals("Patient")) {
                progressDialog.setTitle("Adding Patient");
                title_bar.setText("Add Patient");
                add.setText("Add Patient");
                specialization.setVisibility(View.GONE);
                qualification.setVisibility(View.GONE);
            }
        } else if(caller.equals("Doctor")){
            if (role.equals("Patient")) {
                progressDialog.setTitle("Adding Patient");
                title_bar.setText("Add Patient");
                add.setText("Add Patient");
                specialization.setVisibility(View.GONE);
                qualification.setVisibility(View.GONE);
                fetchReferenceId(mUser.getUid());
            }
            doctor_refer_id.setLongClickable(false);
            doctor_refer_id.setFocusable(false);
            doctor_refer_id.setClickable(false);
            userName.requestFocus();
        }

        ArrayList<String> genderList = getGender();
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genderList);
        //Set adapter
        gender.setAdapter(genderAdapter);

        gender.setOnItemClickListener((parent, view, position, id) -> gender_ = parent.getItemAtPosition(position).toString());

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

            if(!validateEmail() | !validateFirstName() | !validateLastName() | !validateAge() |!validateCity() | !validateStreet() | !validateCNIC() | !validatePassword() | !validatePhone() | !validateReferenceId() | !validateRePassword()){
                return;
            }

            username = userName.getEditText().getText().toString();
            firstname = firstName.getEditText().getText().toString();
            lastname = lastName.getEditText().getText().toString();
            city_ = city.getEditText().getText().toString();
            street_ = street.getEditText().getText().toString();
            age_= age.getEditText().getText().toString();
            cnicNo = cnic.getEditText().getText().toString();
            email_ = email.getEditText().getText().toString();
            phone_ = phone.getEditText().getText().toString();
            password_ = password.getEditText().getText().toString();
            ref_id = doctor_refer_id.getEditText().getText().toString();
            qualify = qualification.getEditText().getText().toString();
            specialize = specialization.getEditText().getText().toString();

            registerUser(ref_id, username, firstname, lastname, city_, street_, age_, gender_ , cnicNo, email_, role, phone_, password_, qualify, specialize);

        });
    }

    private void fetchReferenceId(String user_id){
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users").child("Doctor").child(user_id);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Doctor doctor = snapshot.getValue(Doctor.class);
                    doctor_refer_id.getEditText().setText(doctor.getDoctor_reference_id());
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void registerUser(String ref_id, String username, String firstname, String lastname, String city, String street, String age, String gender, String cnicNo, String email_, String role, String phone_, String password_, String qualify, String specialize){
        mAuth.createUserWithEmailAndPassword(email_, password_).addOnCompleteListener(AddUserActivity.this, task -> {
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
                hashMap.put("firstname", firstname);
                hashMap.put("lastname",lastname);
                hashMap.put("gender",gender);
                hashMap.put("age", age);
                hashMap.put("city",city);
                hashMap.put("street", street);
                hashMap.put("cnic", cnicNo);
                hashMap.put("role", role);
                hashMap.put("password", password_);
                hashMap.put("email", email_);
                hashMap.put("phone", phone_);
                hashMap.put("doctor_reference_id", ref_id);
                if (role.equals("Doctor")) {
                    hashMap.put("qualification", qualify);
                    hashMap.put("specialization", specialize);
                    hashMap.put("image", "https://firebasestorage.googleapis.com/v0/b/mybrain-c35b6.appspot.com/o/images%2Fdoctor.png?alt=media&token=be2e94a8-3668-41f8-b433-0a6c2211e75a");
                } else if (role.equals("Patient")) {
                    hashMap.put("sessions", "");
                    hashMap.put("image", "https://firebasestorage.googleapis.com/v0/b/mybrain-c35b6.appspot.com/o/images%2Fpatient.png?alt=media&token=6a741a79-3fe3-4e06-aaf9-6c1ade98adc2");
                }

                mDbRef.setValue(hashMap);

                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "User successfully added", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    private ArrayList<String> getGender() {
        ArrayList<String> userGender = new ArrayList<>();
        userGender.add("Select Role");
        userGender.add("Male");
        userGender.add("Female");
        userGender.add("Prefer Not to Say");

        return userGender;
    }

    private boolean isInternetAvailable(){
        boolean wifiAvailable = false;
        boolean mobileAvailable = false;
        ConnectivityManager conManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = conManager.getAllNetworkInfo();
        for (NetworkInfo netInfo : networkInfo) {
            if (netInfo.getTypeName().equalsIgnoreCase("WIFI"))
                if (netInfo.isConnected())
                    wifiAvailable = true;
            if (netInfo.getTypeName().equalsIgnoreCase("MOBILE"))
                if (netInfo.isConnected())
                    mobileAvailable = true;
        }
        return wifiAvailable || mobileAvailable;
    }

    private Boolean validateFirstName(){
        String val = firstName.getEditText().getText().toString();
        if(val.isEmpty()){
            firstName.setError("Field cannot be empty");
            return false;
        } else {
            firstName.setError(null);
            firstName.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validateLastName(){
        String val = lastName.getEditText().getText().toString();
        if(val.isEmpty()){
            lastName.setError("Field cannot be empty!");
            return false;
        } else {
            lastName.setError(null);
            lastName.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validateCity(){
        String val = firstName.getEditText().getText().toString();
        if(val.isEmpty()){
            firstName.setError("Field cannot be empty!");
            return false;
        } else {
            firstName.setError(null);
            firstName.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validateStreet(){
        String val = firstName.getEditText().getText().toString();
        if(val.isEmpty()){
            firstName.setError("Field cannot be empty");
            return false;
        } else {
            firstName.setError(null);
            firstName.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validateAge(){
        String val = lastName.getEditText().getText().toString();
        if(val.isEmpty()){
            lastName.setError("Field cannot be empty!");
            return false;
        } else {
            lastName.setError(null);
            lastName.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validateCNIC(){
        String val = cnic.getEditText().getText().toString();
        String cnicPattern = "^[0-9+]{13}";
        if(val.isEmpty()){
            cnic.setError("Field cannot be empty!");
            return false;
        } else if(val.matches(cnicPattern) && val.length()==13){
            email.setError(null);
            email.setErrorEnabled(false);
            return true;
        }else {
            cnic.setError("Invalid NIC Number!");
            return false;
        }

    }

    private Boolean validateReferenceId(){
        String val = doctor_refer_id.getEditText().getText().toString();
        if(val.isEmpty()){
            doctor_refer_id.setError("Field cannot be empty!");
            return false;
        } else {
            doctor_refer_id.setError(null);
            doctor_refer_id.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validateEmail(){
        String val = email.getEditText().getText().toString().trim();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if(val.isEmpty()){
            email.setError("Field cannot be empty!");
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
//        String passPattern = "^(?=.*[0-9])"
//                + "(?=.*[a-z])(?=.*[A-Z])"
//                + "(?=.*[@!#$%^&+=])"
//                + "(?=\\S+$).{6,20}$";

//        Pattern p = Pattern.compile(passPattern);
//        Matcher m = p.matcher(val);

        if(val.isEmpty()){
            password.setError("Field cannot be empty");
            return false;
//        } else if(m.matches()){
//            password.setError(null);
//            password.setErrorEnabled(false);
//            return true;
        } else if(val.length()<6){
            password.setError("Password length should no be less than 6");
            return false;
        } else {
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
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