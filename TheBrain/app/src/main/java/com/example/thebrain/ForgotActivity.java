package com.example.thebrain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.NotNull;

public class ForgotActivity extends AppCompatActivity {
    TextInputLayout email;
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
    }

    public void SendResetPasswordEmail(View view){
        String userEmail = email.getEditText().getText().toString();
        mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(task -> {
            if(task.isComplete()){
                showText.setText(R.string.alert_user);
                email.setEnabled(false);
            }
        }).addOnFailureListener(e -> {
            showText.setText(e.getMessage());
            email.setEnabled(true);
        });
    }

    public void BackToLogin(View view){
        startActivity(new Intent(ForgotActivity.this, LoginActivity.class));
        finish();
    }
}