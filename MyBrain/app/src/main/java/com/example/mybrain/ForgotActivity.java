package com.example.mybrain;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputLayout email;
    Button send;
    TextView showText;
    ImageView back;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        email = findViewById(R.id.reEmail);
        send = findViewById(R.id.forget);
        showText = findViewById(R.id.showText);
        back = findViewById(R.id.back);

        mAuth = FirebaseAuth.getInstance();

        send.setOnClickListener(v -> {
            String userEmail = email.getEditText().getText().toString();
            mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if(task.isComplete()){
                        showText.setText(R.string.alert_user);
                        email.setEnabled(false);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    showText.setText(e.getMessage());
                    email.setEnabled(true);
                }
            });

        });

        back.setOnClickListener(v -> {
            Intent goBack = new Intent(ForgotActivity.this, LoginActivity.class);
            startActivity(goBack);
            finish();
        });
    }
}