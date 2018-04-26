package com.s_maruks.tutinava.eventgallery;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.json.JSONObject;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    // Firebase references
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private CallbackManager callbackManager;

    // JSON data variables
    private JSONObject object;

    // final variables
    private final String TAG = "FacebookLogin";
    private final int password_length = 6;

    // Other data types
    private String user_id;
    private String name;
    private String email;

    // Visual elements
    private LoginButton loginButton;
    private EditText email_input;
    private EditText password_input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        email_input = (EditText) findViewById(R.id.ip_email);
        password_input = (EditText) findViewById(R.id.ip_password);

        findViewById(R.id.btn_create).setOnClickListener(this);
        findViewById(R.id.btn_login).setOnClickListener(this);

        try{
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("user_events","email"));
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onError(FacebookException error) {
                    Log.d(TAG,error.toString());
                }
            });
        }
        catch (NullPointerException e)
        {

        }
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    String path = "/me?fields=name,email";
                    AccessToken token =AccessToken.getCurrentAccessToken();
                    new GraphRequest(
                            token,
                            path,
                            null,
                            HttpMethod.GET,
                            new GraphRequest.Callback() {
                                public void onCompleted(GraphResponse response) {
                                    try {
                                        object = response.getJSONObject();
                                        name = object.getString("name");
                                        user_id=mAuth.getCurrentUser().getUid().toString();
                                        email = object.getString("email");
                                        writeNewUser(user_id,name,email);
                                        Log.d(TAG,name);
                                        open_main_activity();
                                    }
                                    catch (Exception e) {
                                    }
                                }
                            }
                    ).executeAsync();

                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_create :
                createAccount(email_input.getText().toString(), password_input.getText().toString());
            case R.id.btn_login:
                signIn(email_input.getText().toString(), password_input.getText().toString());
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                        } else {
                            Log.w("message", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void createAccount(final String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            writeNewUser(mAuth.getCurrentUser().getUid().toString(), email.split("@")[0],email);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void writeNewUser(String userId, String name, String email) {
        mDatabase.child("users").child(userId).child("username").setValue(name);
        mDatabase.child("users").child(userId).child("email").setValue(email);
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            open_main_activity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        if (!task.isSuccessful()) {
                            Log.d(TAG,"Unsucsessful");
                        }

                    }
                });
    }

    private void signOut() {
        mAuth.signOut();
        LoginManager.getInstance().logOut();
    }

    private void open_main_activity(){
        Intent new_activity = new Intent(LoginActivity.this, MainActivity.class);
        LoginActivity.this.startActivity(new_activity);
        finish();
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = email_input.getText().toString();
        if (TextUtils.isEmpty(email)) {
            email_input.setError("Required.");
            valid = false;
        }else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            email_input.setError("Email is not valid.");
        }
        else {
            email_input.setError(null);
        }

        String password = password_input.getText().toString();
        if (TextUtils.isEmpty(password)) {
            password_input.setError("Required.");
            valid = false;
        }
        else if(password.length()<password_length){
            password_input.setError("Password must be at least "+password_length+" characters.");
        }
        else {
            password_input.setError(null);
        }

        return valid;
    }

}