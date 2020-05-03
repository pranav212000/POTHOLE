package com.example.pothole;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyPhoneNumber extends AppCompatActivity {


    private EditText code_tv;
    private String verificationID;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_number);
        firebaseAuth = FirebaseAuth.getInstance();
        String number = getIntent().getStringExtra("number");
        code_tv = findViewById(R.id.OTP);

        Button button = findViewById(R.id.Verify);
        progressBar = findViewById(R.id.progressBar);
        sendVerificationCode(number);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = code_tv.getText().toString();
                if(otp.isEmpty() || otp.length()<6){
                    code_tv.setError("INVAID OTP");
                    code_tv.requestFocus();
                    return;
                }

                verifyCode(otp);
            }
        });


    }




    private void verifyCode(String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential){
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(VerifyPhoneNumber.this, HomepageActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    progressBar.setVisibility(View.GONE);
                    startActivity(intent);
                    finish();

                }else{
                    Toast.makeText(VerifyPhoneNumber.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }





    private  void sendVerificationCode(String number){
        PhoneAuthProvider.getInstance().verifyPhoneNumber( number, 60, TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD, mCallBack);
        progressBar.setVisibility(View.VISIBLE);

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Toast.makeText(VerifyPhoneNumber.this, "OTP SENT !", Toast.LENGTH_SHORT).show();
            verificationID = s;

        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if(code != null){
                code_tv.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(VerifyPhoneNumber.this, e.getMessage(), Toast.LENGTH_LONG ).show();
        }
    };
}
