package com.s_maruks.tutinava.eventgallery;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "FacebookLogin";
    private FirebaseAuth mAuth;
    EditText email_input;
    EditText password_input;
    FirebaseUser currentUser;
    AuthCredential credential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        email_input = (EditText) findViewById(R.id.ip_email);
        password_input = (EditText) findViewById(R.id.ip_password);
        mAuth = FirebaseAuth.getInstance();
        findViewById(R.id.btn_link).setOnClickListener(this);
        FirebaseUser user = mAuth.getCurrentUser();
        Log.d(TAG,user.toString());
    }


    private void link_account(String email, String password){
        credential = EmailAuthProvider.getCredential(email, password);
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "linkWithCredential:failure", task.getException());
                            Toast.makeText(ProfileActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Authentication successful.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();

    }
    @Override
    public void onClick(View v) {
        link_account(email_input.getText().toString(), password_input.getText().toString());
    }
}
