package com.s_maruks.tutinava.eventgallery;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    JSONObject event;
    JSONObject object;
    JSONArray events;

    TextView tw;
    ImageView iw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tw = (TextView) findViewById(R.id.tw_text);
        iw = (ImageView) findViewById(R.id.iw_image);
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btn_create).setOnClickListener(this);

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
                    Log.d("user",mAuth.getCurrentUser().getUid().toString());
                } else {
                    open_login_screen();
                }

            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Profile:
                open_profile();
                return true;
            case R.id.LogOut:
                signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void setText(String name){
        tw.setText(name);
    }

    private void open_login_screen(){
        Intent new_activity = new Intent(MainActivity.this, LoginActivity.class);
        new_activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        new_activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        new_activity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(new_activity);
        finish();
    }
    private void open_profile(){
        Intent new_activity = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(new_activity);
    }
    private void create_event(){
        Intent new_activity = new Intent(MainActivity.this, CreateEvent.class);
        startActivity(new_activity);
    }

    private void signOut() {
        LoginManager.getInstance().logOut();
        FirebaseAuth.getInstance().signOut();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_create :
                create_event();
        }
    }
}
