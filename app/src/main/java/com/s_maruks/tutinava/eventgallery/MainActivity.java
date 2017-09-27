package com.s_maruks.tutinava.eventgallery;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import static android.R.attr.name;
import static android.support.v7.widget.AppCompatDrawableManager.get;
import static android.view.View.Y;

public class MainActivity extends AppCompatActivity {
    JSONObject event;
    JSONObject object;
    JSONArray events;
    TextView tw;
    ImageView iw;
    String name="nothing";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tw = (TextView) findViewById(R.id.tw_text);
        iw = (ImageView) findViewById(R.id.iw_image);
        mAuth = FirebaseAuth.getInstance();

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/events",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        object = response.getJSONObject();
                        try {
                            events=object.getJSONArray("data");
                            for (int i = 0; i< events.length();i++) {
                                try {
                                    event = events.getJSONObject(i);
                                    String name = event.getString("name");
                                    tw.setText(name);
                                } catch (Exception e) {

                                }
                            }
                        }
                        catch (Exception e){

                        }
                    }
                }).executeAsync();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                } else {
                    Intent new_activity = new Intent(MainActivity.this, LoginActivity.class);
                    //new_activity.putExtra("key", value);
                    MainActivity.this.startActivity(new_activity);
                    finish();
                }

            }
        };
    }
    public void setText(String name){
        tw.setText(name);
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
}
