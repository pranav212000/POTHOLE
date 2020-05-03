package com.example.pothole;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StarterActivity extends AppCompatActivity {

    FirebaseAuth mFirebaseAuth;
    GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 9001;
    private static final String TAG = "StarterActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);

        }


        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(StarterActivity.this, gso);
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(StarterActivity.this);


        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if(user == null && account == null){

            Toast.makeText(this, "Choose Login Mode", Toast.LENGTH_SHORT).show();

            Button phoneSignin = findViewById(R.id.login_by_phone);

            phoneSignin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(StarterActivity.this, PhoneActivity.class));

                }
            });

            SignInButton google_signin = findViewById(R.id.sign_in_button);
            google_signin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    signIn();

                }
            });
        } else{
          //  Toast.makeText(this, "Loading homepage", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(StarterActivity.this, HomepageActivity.class));
            finish();
        }



    }

    private void signIn() {
        Intent signInintent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInintent, RC_SIGN_IN);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            Intent intent = new Intent(StarterActivity.this, HomepageActivity.class);
            intent.putExtra("login_by", "google");
            startActivity(intent);

        } catch (ApiException e) {

            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }
}
