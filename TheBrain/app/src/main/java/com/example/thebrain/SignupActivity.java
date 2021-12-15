package com.example.thebrain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {
    TextInputLayout doctor_refer_id, userName, firstName, lastName, cnic, age, city, street, qualification, specialization, phone, email, password, rePassword;
    AutoCompleteTextView roles, gender;
    DatabaseReference reference, roleRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    ProgressDialog progressDialog;
    String firstname, lastname, city_, street_, age_, gender_, cnicNo, qualif, specialize, email_ad, phone_no, password_user, role_user, username, doctor_ref_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firstName = findViewById(R.id.firstname);
        lastName = findViewById(R.id.lastname);
        cnic = findViewById(R.id.cnic);
        userName = findViewById(R.id.username);
        age = findViewById(R.id.age);
        city = findViewById(R.id.city);
        street = findViewById(R.id.street);
        doctor_refer_id = findViewById(R.id.reference_id);
        specialization = findViewById(R.id.specialization);
        qualification = findViewById(R.id.qualification);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        rePassword = findViewById(R.id.confirmPass);
        roles = findViewById(R.id.role);
        gender = findViewById(R.id.gender);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        reference = FirebaseDatabase.getInstance().getReference("users");
        roleRef = FirebaseDatabase.getInstance().getReference("role-user");
        mAuth = FirebaseAuth.getInstance();

        ArrayList<String> roleList = getRoles();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roleList);

        //Set adapter
        roles.setAdapter(adapter);

        ArrayList<String> genderList = getGender();

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genderList);

        //Set adapter
        gender.setAdapter(genderAdapter);

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

        gender.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gender_ = parent.getItemAtPosition(position).toString();
            }
        });
    }

    public void SignUpUser(View view){
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

        if(!validateReferenceId() | !validateFirstName() | !validateLastName() | !validateAge() |!validateCity() | !validateStreet() | !validateCNIC() | !validateEmail() | !validatePassword() | !validatePhone() | !validateRePassword()){
            return;
        }

        progressDialog.show();

        username = userName.getEditText().getText().toString();
        doctor_ref_id = doctor_refer_id.getEditText().getText().toString();
        firstname = firstName.getEditText().getText().toString();
        lastname = lastName.getEditText().getText().toString();
        city_ = city.getEditText().getText().toString();
        street_ = street.getEditText().getText().toString();
        age_= age.getEditText().getText().toString();
        cnicNo = cnic.getEditText().getText().toString();
        qualif = qualification.getEditText().getText().toString();
        specialize = specialization.getEditText().getText().toString();
        email_ad = email.getEditText().getText().toString().trim();
        phone_no = phone.getEditText().getText().toString().trim();
        password_user = password.getEditText().getText().toString().trim();

        if(role_user.equals("Doctor")){
            RegisterDoctor(doctor_ref_id, username, firstname, lastname, city_, street_, age_, gender_ , cnicNo, qualif, specialize, email_ad, phone_no, password_user, role_user);
        } else if(role_user.equals("Patient")){
            RegisterPatient(doctor_ref_id, username, firstname, lastname, city_, street_, age_, gender_ , cnicNo, email_ad, phone_no, password_user, role_user);;
        } else {
            Toast.makeText(SignupActivity.this, "Please select valid role!", Toast.LENGTH_SHORT).show();
        }
    }

    private void RegisterDoctor(String doctor_ref_id, String username, String firstname, String lastname, String city_, String street_, String age_, String gender_, String cnicNo, String qualif, String specialize, String email_ad, String phone_no, String password_user, String role_user) {
        mAuth.createUserWithEmailAndPassword(email_ad, password_user).addOnCompleteListener(SignupActivity.this, task -> {
            if (task.isComplete()){
                mUser = mAuth.getCurrentUser();
                HashMap<String, Object> roleHash = new HashMap<>();
                roleHash.put("id", mUser.getUid());
                roleHash.put("role", role_user);
                roleRef.child(mUser.getUid()).setValue(roleHash);

                HashMap<String, Object> docHashMap = new HashMap<>();
                docHashMap.put("id", mUser.getUid());
                docHashMap.put("doctor_reference_id", doctor_ref_id);
                docHashMap.put("username", username);
                docHashMap.put("firstname", firstname);
                docHashMap.put("lastname", lastname);
                docHashMap.put("age", age_);
                docHashMap.put("city", city_);
                docHashMap.put("street", street_);
                docHashMap.put("gender", gender_);
                docHashMap.put("cnic", cnicNo);
                docHashMap.put("email", email_ad);
                docHashMap.put("qualification", qualif);
                docHashMap.put("specialization", specialize);
                docHashMap.put("phone", phone_no);
                docHashMap.put("role", role_user);
                docHashMap.put("password", password_user);
                docHashMap.put("image", "https://firebasestorage.googleapis.com/v0/b/mybrain-c35b6.appspot.com/o/images%2Fdoctor.png?alt=media&token=be2e94a8-3668-41f8-b433-0a6c2211e75a");

                reference.setValue(docHashMap).addOnCompleteListener(task1 -> {
                    if(task1.isComplete()){
                        Intent goToProfile = new Intent(SignupActivity.this, DoctorActivity.class);
                        goToProfile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        goToProfile.putExtra("role", role_user);
                        goToProfile.putExtra("id", mUser.getUid());
                        startActivity(goToProfile);
                    }
                });
                progressDialog.dismiss();
            }
        });

    }

    private void RegisterPatient(String doctor_ref_id, String username, String firstname, String lastname, String city_, String street_, String age_, String gender_, String cnicNo, String email_ad, String phone_no, String password_user, String role_user) {
        mAuth.createUserWithEmailAndPassword(email_ad, password_user).addOnCompleteListener(SignupActivity.this, task -> {
            if (task.isComplete()){
                mUser = mAuth.getCurrentUser();
                HashMap<String, Object> roleHash = new HashMap<>();
                roleHash.put("id", mUser.getUid());
                roleHash.put("role", role_user);
                roleRef.child(mUser.getUid()).setValue(roleHash);

                HashMap<String, Object> docHashMap = new HashMap<>();
                docHashMap.put("id", mUser.getUid());
                docHashMap.put("doctor_reference_id", doctor_ref_id);
                docHashMap.put("username", username);
                docHashMap.put("firstname", firstname);
                docHashMap.put("lastname", lastname);
                docHashMap.put("age", age_);
                docHashMap.put("city", city_);
                docHashMap.put("street", street_);
                docHashMap.put("gender", gender_);
                docHashMap.put("cnic", cnicNo);
                docHashMap.put("email", email_ad);
                docHashMap.put("phone", phone_no);
                docHashMap.put("role", role_user);
                docHashMap.put("password", password_user);
                docHashMap.put("image", "https://firebasestorage.googleapis.com/v0/b/mybrain-c35b6.appspot.com/o/images%2Fdoctor.png?alt=media&token=be2e94a8-3668-41f8-b433-0a6c2211e75a");

                reference.setValue(docHashMap).addOnCompleteListener(task1 -> {
                    if(task1.isComplete()){
                        Intent goToProfile = new Intent(SignupActivity.this, DoctorActivity.class);
                        goToProfile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        goToProfile.putExtra("role", role_user);
                        goToProfile.putExtra("id", mUser.getUid());
                        startActivity(goToProfile);
                    }
                });
                progressDialog.dismiss();
            }
        });

    }

    public void backToLogin(View view){
        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        finish();
    }

    private boolean isInternetAvailable(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private ArrayList<String> getRoles() {
        ArrayList<String> userRole = new ArrayList<>();
        userRole.add("Select Role");
        userRole.add("Doctor");
        userRole.add("Patient");

        return userRole;
    }

    private ArrayList<String> getGender() {
        ArrayList<String> userGender = new ArrayList<>();
        userGender.add("Male");
        userGender.add("Female");
        userGender.add("Prefer Not to Say");

        return userGender;
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
            lastName.setError("Field cannot be empty");
            return false;
        } else {
            lastName.setError(null);
            lastName.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validateCity(){
        String val = city.getEditText().getText().toString();
        if(val.isEmpty()){
            city.setError("Field cannot be empty");
            return false;
        } else {
            city.setError(null);
            city.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validateStreet(){
        String val = street.getEditText().getText().toString();
        if(val.isEmpty()){
            street.setError("Field cannot be empty");
            return false;
        } else {
            street.setError(null);
            street.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validateAge(){
        String val = age.getEditText().getText().toString();
        if(val.isEmpty()){
            age.setError("Field cannot be empty");
            return false;
        } else {
            age.setError(null);
            age.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validateCNIC(){
        String val = cnic.getEditText().getText().toString();
        String nicPattern = "^[0-9+]{13}";
        if(val.isEmpty()){
            cnic.setError("Field cannot be empty");
            return false;
        } else if(val.matches(nicPattern)){
            email.setError(null);
            email.setErrorEnabled(false);
            return true;
        }else {
            cnic.setError("Invalid NIC pattern");
            cnic.setErrorEnabled(false);
            return true;
        }

    }

    private Boolean validateReferenceId(){
        String val = doctor_refer_id.getEditText().getText().toString();
        if(val.isEmpty()){
            doctor_refer_id.setError("Field cannot be empty");
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
//        String passPattern = "^(?=.*[0-9])"
//                + "(?=.*[a-z])(?=.*[A-Z])"
//                + "(?=.*[@#$%^&+=])"
//                + "(?=\\S+$).{8,20}$";
//
//        Pattern p = Pattern.compile(passPattern);
//        Matcher m = p.matcher(val);

        if(val.isEmpty()){
            password.setError("Field cannot be empty");
            return false;
        } else if(val.length()>=6){
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