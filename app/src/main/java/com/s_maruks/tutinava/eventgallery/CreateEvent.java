package com.s_maruks.tutinava.eventgallery;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import Helpers.DatePickerFragment;
import Helpers.LayoutExpander;

public class CreateEvent extends AppCompatActivity  implements View.OnClickListener{
    //Firebase references
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private DatabaseReference user_events;
    private DatabaseReference all_events;

    //Helpers
    private static LayoutExpander layoutAnimationManager;

    //JSON data variables
    private JSONObject object;
    private JSONObject upcoming_event;
    private JSONArray upcoming_events;

    JSONObject data_object;

    //RecyclerView - related
    private UpcomingEventsAdapter adapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private RelativeLayout animated_layout;


    //Other data types
    private Date currentTime;
    private String strDt;
    private String creator;
    private boolean exists;
    List<FBEvent>fb_events= new ArrayList<>();
    private boolean isVisible;
    private boolean isLoaded;

    //Visual elements
    private EditText event_name_input;
    private Toast currentToast;

    AccessToken token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        event_name_input = (EditText) findViewById(R.id.txt_name);
        findViewById(R.id.btn_create).setOnClickListener(this);
        findViewById(R.id.btn_fb).setOnClickListener(this);
        findViewById(R.id.btn_date).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        creator = mAuth.getCurrentUser().getUid().toString();
        user_events= mDatabase.child("users").child(creator).child("Created events");
        all_events = mDatabase.child("events");
        currentTime  = Calendar.getInstance().getTime();

        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd.MM.yyyy");
        strDt = simpleDate.format(currentTime);

        layoutAnimationManager= new LayoutExpander();
        isVisible = false;
        isLoaded=false;
        mRecyclerView = (RecyclerView) findViewById(R.id.rw_fb);
        mLinearLayoutManager = new LinearLayoutManager(this);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d("user",mAuth.getCurrentUser().toString());

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
            case R.id.btn_date:
                showDatePickerDialog(v);
                break;
            case R.id.btn_create:
                //create_event();
                break;
            case R.id.btn_fb:
                launch_animation();
                break;
        }
    }

    public void get_upcoming_events(){
        String path =  "/me/events?since="+strDt;
        token =AccessToken.getCurrentAccessToken();

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
                                fbEvent.event_id = upcoming_event.getString("id");
                                fb_events.add(fbEvent);
                            }
                            get_display_picture();
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
        adapter.setOnRecyclerViewItemClickListener(new UpcomingEventsAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClicked(CharSequence text) {
                create_from_event(text.toString());
                create_toast(text.toString());
            }
        });
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(adapter);
        isLoaded=true;
    }

    private void get_display_picture(){
        token =AccessToken.getCurrentAccessToken();
        for (int i = 0; i < fb_events.size(); i++) {
            String path =  "/"+fb_events.get(i).event_id+"/picture?redirect=false";
            final int finalI = i;
            new GraphRequest(
                    token,
                    path,
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            try {
                                object = response.getJSONObject();
                                data_object = object.getJSONObject("data");
                                fb_events.get(finalI).image_url=data_object.getString("url");
                                display_data();
                            }
                            catch(Exception e){
                            }
                        }
                    }
            ).executeAsync();
        }

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

    private void create_toast(String msg) {
        if (currentToast != null) currentToast.cancel();
        currentToast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    private void create_from_event(String event_id){

        Event new_event = new Event();

    }

    private void launch_animation(){
        if (isVisible) {
            layoutAnimationManager.expand(mRecyclerView, 1000, -500);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    mRecyclerView.setVisibility(View.GONE);
                }
            }, 200);
            isVisible = false;
        } else if (!isVisible){
            if (!isLoaded) get_upcoming_events();
            layoutAnimationManager.expand(mRecyclerView, 1000, 500);
            mRecyclerView.setVisibility(View.VISIBLE);
            isVisible = true;
        }
    }

    private void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }
}
