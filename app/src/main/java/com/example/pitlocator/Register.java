package com.example.pitlocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pitlocator.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class Register extends AppCompatActivity {

    private Button registerbtn;
    FirebaseAuth mAuth;
    EditText emailId, pass,mob,otpBox;
    TextView loginPage;
    ProgressBar progressBar;
    Button submitDetails, verifyOtp,btnLogin;
    String verificationID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        progressBar =  findViewById(R.id.progressBar);
        loginPage = findViewById(R.id.loginpage);
        pass = findViewById(R.id.password);
        emailId  = findViewById(R.id.emailId);
        submitDetails = findViewById(R.id.submitdetails);
        verifyOtp = findViewById(R.id.verifyotp);
        btnLogin = findViewById(R.id.btn_login);
        otpBox = findViewById(R.id.otpbox);

        mob = findViewById(R.id.number);

        mAuth = FirebaseAuth.getInstance();

        registerbtn = findViewById(R.id.btn_login);

        loginPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this,Login.class);
                startActivity(intent);
                finish();
            }
        });

        submitDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(emailId.getText().toString().isEmpty() || pass.getText().toString().isEmpty() || mob.getText().toString().isEmpty())
                {
                    Toast.makeText(Register.this, "Field cannot be empty", Toast.LENGTH_SHORT).show();
                }
                else
                {

                    String number = mob.getText().toString();
                    sendVerificationCode(number);
                    submitDetails.setVisibility(v.GONE);
                    otpBox.setVisibility(v.VISIBLE);
                    verifyOtp.setVisibility(v.VISIBLE);
                }

            }
        });

        verifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(otpBox.getText().toString().isEmpty())
                {
                    Toast.makeText(Register.this, "Enter Otp", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    otpBox.setVisibility(View.GONE);
                    verifyOtp.setVisibility(View.GONE);
                    submitDetails.setVisibility(View.GONE);
                    btnLogin.setVisibility(View.VISIBLE);
                    mob.setEnabled(false);
                }
            }
        });



        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(v.VISIBLE);
                String email,password;
                email = String.valueOf(emailId.getText());
                password = String.valueOf(pass.getText());

                if(emailId.length()==0)
                {
                    Toast.makeText(Register.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(pass.length()==0)
                {
                    Toast.makeText(Register.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(v.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(Register.this, "Authentication Done", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Register.this,Login.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(Register.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }

    private void sendVerificationCode(String phoneNumber) {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91"+phoneNumber)       // Phone number to verify
                        .setTimeout(120L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {

            final String code = credential.getSmsCode();
            if(code!=null)
            {
                otpBox.setVisibility(View.GONE);
                verifyOtp.setVisibility(View.GONE);
                submitDetails.setVisibility(View.GONE);
                btnLogin.setVisibility(View.VISIBLE);
                mob.setEnabled(false);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(Register.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();

        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
           super.onCodeSent(s,token);
           verificationID = s;
        }
    };

}