package com.s_maruks.tutinava.eventgallery;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import Entities.Event;
import Entities.User;

public class CreateEvent extends AppCompatActivity  implements View.OnClickListener{
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase, user_events;

    JSONObject object,upcoming_event;
    JSONArray upcoming_events;
    Date currentTime;
    String strDt;
    ImageView iw;

    EditText event_name_input;
    String creator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        event_name_input = (EditText) findViewById(R.id.txt_name);
        findViewById(R.id.btn_create).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        creator = mAuth.getCurrentUser().getUid().toString();
        user_events= mDatabase.child("users").child(creator).child("Created events");
        currentTime  = Calendar.getInstance().getTime();
        iw = (ImageView)findViewById(R.id.imageView2);

        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd.MM.yyyy");
        strDt = simpleDate.format(currentTime);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d("user",mAuth.getCurrentUser().toString());
                    get_upcoming_events();
                    display_picture();
                } else {
                    open_login_screen();
                }

            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_create:
                create_event();
        }
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

    public void get_upcoming_events(){

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/events?since="+strDt,
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        object = response.getJSONObject();
                        try {
                            upcoming_events = object.getJSONArray("data");
                            for (int i = 0; i < upcoming_events.length(); i++) {
                                try {
                                    upcoming_event = upcoming_events.getJSONObject(i);
                                    String name = upcoming_event.getString("name");
                                    Log.d("name",name);
                                } catch (Exception e) {

                                }
                            }
                        }
                    catch(Exception e){

                    }
                    }
                }
        ).executeAsync();
    }

    private void create_event(){
        String event_name = event_name_input.getText().toString();

        String event_id = generate_event_id();

        List<User> participants = new ArrayList<>();
        Event event = new Event(event_name,creator,event_id,participants);
        user_events.child(event_id).setValue(event_id);
        mDatabase.child("events").child(event_id).setValue(event);
    }

    private String generate_event_id(){
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String generated_nr = salt.toString();
        return generated_nr;
    }

    private void open_login_screen(){
        Intent new_activity = new Intent(CreateEvent.this, LoginActivity.class);
        new_activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        new_activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        new_activity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(new_activity);
        finish();
    }

    private void display_picture(){
        Glide.with(this)
                .load("https://scontent.xx.fbcdn.net/v/t1.0-0/c19.0.50.50/p50x50/22449785_1635992856444894_2693274557934630191_n.jpg?oh=d7d86a1a4644daaec3ae8233e4f6b995&oe=5A84C6B8")
                .centerCrop()
                .into(iw);
    }
}
