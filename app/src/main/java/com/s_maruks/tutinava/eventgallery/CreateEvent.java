package com.s_maruks.tutinava.eventgallery;

import android.content.Intent;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
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

import Adapters.UpcomingEventsAdapter;
import Entities.Event;
import Entities.FBEvent;
import Entities.User;
import Helpers.RandomStringGenerator;

public class CreateEvent extends AppCompatActivity  implements View.OnClickListener{
    //Firebase references
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private DatabaseReference user_events;
    private DatabaseReference all_events;

    //Helpers
    private static RandomStringGenerator stringGenerator;

    //JSON data variables
    private JSONObject object;
    private JSONObject upcoming_event;
    private JSONArray upcoming_events;

    JSONObject object1;
    JSONObject data_object;

    //RecyclerView - related
    private UpcomingEventsAdapter adapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;


    //Other data types
    private Date currentTime;
    private String strDt;
    private String creator;
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

        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd.MM.yyyy");
        strDt = simpleDate.format(currentTime);

        mRecyclerView = (RecyclerView) findViewById(R.id.rw_fb);
        mLinearLayoutManager = new LinearLayoutManager(this);

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
                            upcoming_events = object.getJSONArray("data");
                            for (int i = 0; i < upcoming_events.length(); i++) {
                                FBEvent fbEvent= new FBEvent();
                                upcoming_event = upcoming_events.getJSONObject(i);
                                fbEvent.start_time = upcoming_event.getString("start_time");
                                fbEvent.description = upcoming_event.getString("description");
                                fbEvent.Name = upcoming_event.getString("name");
                                fbEvent.event_id = upcoming_event.getLong("id");
                                fb_events.add(fbEvent);
                                display_data();
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

    private void display_data(){
        adapter = new UpcomingEventsAdapter(this, fb_events);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(adapter);
    }

    private void display_picture(){

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
