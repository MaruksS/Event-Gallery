package com.s_maruks.tutinava.eventgallery;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Arrays;

import static android.R.attr.value;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    LoginButton loginButton;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        boolean fbLogout = true;
        try{
            fbLogout = getIntent().getExtras().getBoolean("FBlogout");
        }catch (Exception e){

        }

        loginButton = (LoginButton)findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();
        //LoginManager.getInstance().logOut();
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
            }
        };
        // If the access token is available already assign it.
        AccessToken  accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken!=null){
            Intent new_activity = new Intent(LoginActivity.this, MainActivity.class);
            //new_activity.putExtra("key", value);
            LoginActivity.this.startActivity(new_activity);
        }
        try{
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("user_events"));
                    Intent new_activity = new Intent(LoginActivity.this, MainActivity.class);
                    //new_activity.putExtra("key", value);
                    LoginActivity.this.startActivity(new_activity);
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onError(FacebookException error) {

                }
            });
        }
        catch (NullPointerException e)
        {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}