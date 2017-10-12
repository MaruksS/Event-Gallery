package com.s_maruks.tutinava.eventgallery;

import android.content.Intent;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import Adapters.GalleryAdapter;
import Entities.Event;
import Entities.FBEvent;
import Entities.User;
import Helpers.FacebookRequest;
import Helpers.RandomStringGenerator;

public class CreateEvent extends AppCompatActivity  implements View.OnClickListener{
    //Firebase references
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private DatabaseReference user_events;
    private DatabaseReference all_events;

    //Helpers
    private static FacebookRequest fbRequest;
    private static RandomStringGenerator stringGenerator;

    //JSON data variables
    private JSONObject object;
    private JSONObject upcoming_event;
    private JSONArray upcoming_events;

    //Other data types
    private Date currentTime;
    private String strDt;
    private String creator;
    private ImageView iw;
    private boolean exists;
    List<FBEvent>fb_events= new ArrayList<>();

    //Visual elements
    private EditText event_name_input;
    private Toast currentToast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        event_name_input = (EditText) findViewById(R.id.txt_name);
        findViewById(R.id.btn_create).setOnClickListener(this);
        findViewById(R.id.fb_events).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        creator = mAuth.getCurrentUser().getUid().toString();
        user_events= mDatabase.child("users").child(creator).child("Created events");
        all_events = mDatabase.child("events");
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
                    display_picture();
                } else {
                    open_login_screen();
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_create:
                create_event();
            case R.id.fb_events:
                get_upcoming_events();
        }
    }

    public void get_upcoming_events(){
        String path =  "/me/events?since="+strDt;
        AccessToken at =AccessToken.getCurrentAccessToken();

            object = fbRequest.getGraphApi(path,at);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    try {
                        upcoming_events = object.getJSONArray("data");
                        for (int i = 0; i < upcoming_events.length(); i++) {
                            upcoming_event = upcoming_events.getJSONObject(i);
                            fb_events.get(i).start_time = upcoming_event.getString("start_time");
                            fb_events.get(i).description = upcoming_event.getString("description");
                            fb_events.get(i).Name = upcoming_event.getString("name");
                            fb_events.get(i).event_id = upcoming_event.getInt("id");
                        }
                    }
                catch(Exception e){
                }
                }
            }, 2500);   //1.5 seconds

    }

    private void create_event(){
        String event_name = event_name_input.getText().toString();
        String event_id = event_name.replace(' ', '-').toLowerCase();
        if (!Exists(event_id)){
            List<User> participants = new ArrayList<>();
            Event event = new Event(event_name,creator,event_id,participants);
            user_events.child(event_id).setValue(event_id);
            mDatabase.child("events").child(event_id).setValue(event);
        }
       else Log.d("message","exists");
        create_toast("Event with this name already exists");
    }

    private void display_picture(){


        Glide.with(this)
                .load("https://scontent.xx.fbcdn.net/v/t1.0-0/c19.0.50.50/p50x50/22449785_1635992856444894_2693274557934630191_n.jpg?oh=d7d86a1a4644daaec3ae8233e4f6b995&oe=5A84C6B8")
                .centerCrop()
                .into(iw);
    }

    private void open_login_screen(){
        Intent new_activity = new Intent(CreateEvent.this, LoginActivity.class);
        new_activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        new_activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        new_activity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(new_activity);
        finish();
    }

    private boolean Exists(String id){
        DatabaseReference event = all_events.child(id);
        event.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    if (messageSnapshot!=null){
                        Log.d("message","exists");
                        exists = true;
                    }
                    else{
                        Log.d("message","OK");
                        exists = false;
                    }

                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("", "Failed to read value.", error.toException());
            }

        });
        return exists;
    }

    public void create_toast(String msg) {
        if (currentToast != null) currentToast.cancel();
        currentToast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        currentToast.show();
    }
}
