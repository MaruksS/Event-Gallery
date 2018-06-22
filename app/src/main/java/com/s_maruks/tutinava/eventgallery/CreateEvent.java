package com.s_maruks.tutinava.eventgallery;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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

import Adapters.MainAdapter;
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

    //RecyclerView - related
    private UpcomingEventsAdapter adapter;
    private MainAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private LinearLayoutManager mLinearLayoutManager_FB;

    //Other data types
    private String stringDate;
    private String dateToShow;
    private String creator;
    private FBEvent selected_event;
    private List<FBEvent>fb_events= new ArrayList<>();
    private boolean isVisible;
    private boolean isLoaded;
    private SimpleDateFormat displayDate;
    private SimpleDateFormat simpleDate;
    private AccessToken token;
    private int fb_event_count;
    private int height;

    //final variables
    private final String FB_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss+SSSS";
    private final String DISPLAY_DATE_FORMAT = "dd.MMMM yyyy";
    private final String SIMPLE_DATE_FORMAT = "dd.MM.yyyy";

    //Visual elements
    private EditText event_name_input;
    private EditText event_description_input;
    private TextView event_date_field;
    private TextView event_indicator;
    private TextView event_alert;
    private ImageView disable_event;
    private ImageView fb_event_image;
    private RecyclerView rec_view;
    private Toast currentToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        event_name_input = (EditText) findViewById(R.id.txt_name);
        event_description_input= (EditText) findViewById(R.id.txt_desc);
        event_date_field= (TextView)findViewById(R.id.tw_date);
        event_indicator = (TextView)findViewById(R.id.fb_event_tfield);
        disable_event = (ImageView)findViewById(R.id.fb_event_close);
        disable_event.setOnClickListener(this);
        fb_event_image = (ImageView)findViewById(R.id.fb_event_image);
        rec_view=(RecyclerView) findViewById(R.id.rec_view);

        findViewById(R.id.btn_create).setOnClickListener(this);
        findViewById(R.id.btn_fb).setOnClickListener(this);
        findViewById(R.id.btn_date).setOnClickListener(this);

        Date currentTime  = Calendar.getInstance().getTime();

        simpleDate =  new SimpleDateFormat(SIMPLE_DATE_FORMAT);
        displayDate =  new SimpleDateFormat(DISPLAY_DATE_FORMAT);

        dateToShow = displayDate.format(currentTime);
        stringDate = simpleDate.format(currentTime);
        event_date_field.setText(dateToShow);

        isVisible = false;
        isLoaded = false;

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        creator = mAuth.getCurrentUser().getUid().toString();
        user_events= mDatabase.child("users").child(creator).child("Attending");
        all_events = mDatabase.child("events");

        layoutAnimationManager= new LayoutExpander();
        mRecyclerView = (RecyclerView) findViewById(R.id.rw_fb);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager_FB = new LinearLayoutManager(this);
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
                create_event();
                break;
            case R.id.btn_fb:
                launch_animation();
                break;
            case R.id.fb_event_close:
                disable_fb_event();
                break;
        }
    }

    public void get_upcoming_events(){
        height=0;
        fb_event_count=0;
        String path =  "/me/events";
        token =AccessToken.getCurrentAccessToken();
        new GraphRequest(token, path, null, HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            object = response.getJSONObject();
                            JSONArray upcoming_events = object.getJSONArray("data");
                            for (int i = 0; i < upcoming_events.length(); i++) {
                                FBEvent fbEvent= new FBEvent();
                                JSONObject upcoming_event = upcoming_events.getJSONObject(i);
                                fbEvent.start_time = upcoming_event.getString("start_time");
                                //fbEvent.description = upcoming_event.getString("description");
                                fbEvent.name = upcoming_event.getString("name");
                                fbEvent.event_id = upcoming_event.getString("id");
                                fb_events.add(fbEvent);
                                fb_event_count++;
                                if (fb_event_count<=3){
                                    height+=150;
                                }
                            }
                            get_display_picture();
                            open_animation();
                        }
                        catch(Exception e){
                        }
                    }
                }
        ).executeAsync();
    }

    private void create_event(){
        final String event_name = event_name_input.getText().toString();

        final String event_id = event_name.replace(' ', '-').toLowerCase();
        final String event_description = event_description_input.getText().toString();
        final String fb_event_id;
        final String cover_photo = "not_set";
        if (selected_event != null){
            fb_event_id=selected_event.event_id;
        }
        else fb_event_id=null;

        if(event_name.length()<4){
            create_toast("Event name is too short");
        }else {
            DatabaseReference event = all_events.child(event_id);
            event.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()==true){
                        Log.d("message","exists");
                        create_toast("Event with this name already exists");
                    }
                    else{
                        Log.d("message","OK");
                        List<User> participants = new ArrayList<>();
                        Event event = new Event(event_name,event_description,creator,event_id,participants,fb_event_id, cover_photo);
                        user_events.child(event_id).setValue(true);
                        mDatabase.child("events").child(event_id).setValue(event);
                        open_invite_activity(event_id,event_name);
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w("", "Failed to read value.", error.toException());
                }

            });
        }
    }

    private void display_data(){
        adapter = new UpcomingEventsAdapter(this, fb_events);
        adapter.setOnRecyclerViewItemClickListener(new UpcomingEventsAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClicked(CharSequence text) {
                create_from_event(text.toString());
            }
        });
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(adapter);
        isLoaded=true;
    }

    private void get_display_picture(){
        token = AccessToken.getCurrentAccessToken();
        for (int i = 0; i < fb_events.size(); i++) {
            final int finalI = i;
            //fb_events.get(finalI).image_url = "http://snappyhouse.com.sg/templates/bootstrap2-responsive/assets/images/v2/featured_placeholder-400x300.png";
            //display_data();
            String path = "/" + fb_events.get(i).event_id + "?fields=cover";
            new GraphRequest(
                    token,
                    path,
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            try {
                                object = response.getJSONObject();
                                if (!object.has("cover")){
                                    fb_events.get(finalI).image_url = "placeholder";
                                }
                                else{
                                    JSONObject data_object = object.getJSONObject("cover");
                                    fb_events.get(finalI).image_url = data_object.getString("source");
                                }
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

    private void create_toast(String msg) {
        if (currentToast != null) currentToast.cancel();
        currentToast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    private void create_from_event(String event_id){
        rec_view.setVisibility(View.INVISIBLE);

        for (int i=0;i<fb_events.size();i++){
            if (fb_events.get(i).event_id==event_id){
                selected_event=fb_events.get(i);

                event_name_input.setText(selected_event.name);
                event_name_input.setEnabled(false);

                event_description_input.setText(selected_event.description);
                Date event_date = parseDate(selected_event.start_time);
                String display_date = displayDate.format(event_date);
                event_date_field.setText(display_date);
                findViewById(R.id.btn_date).setEnabled(false);
                disable_event.setVisibility(View.VISIBLE);
                event_indicator.setText(selected_event.name);
                get_event();

                String photoRef= selected_event.image_url;
                if (photoRef=="placeholder"){
                    Glide.with(this)
                            .load(R.drawable.ic_photo_placeholder)
                            .into(fb_event_image);
                }
                else{
                    Glide.with(this)
                            .load(photoRef)
                            .asBitmap()
                            .into(fb_event_image);
                }
                launch_animation();
            }
        }

    }

    private void disable_fb_event(){
        rec_view.setVisibility(View.INVISIBLE);
        selected_event=null;
        event_name_input.setText("");
        event_name_input.setEnabled(true);

        findViewById(R.id.btn_date).setEnabled(true);
        Glide.with(this)
                .load(R.drawable.ic_photo_placeholder)
                .into(fb_event_image);
        event_indicator.setText("No Facebook Event selected");
        disable_event.setVisibility(View.INVISIBLE);
    }

    private void launch_animation(){
        if (isVisible) {
            layoutAnimationManager.expand(mRecyclerView, 1000, -height);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    mRecyclerView.setVisibility(View.GONE);
                }
            }, 200);
            isVisible = false;
        } else if (!isVisible){
            if (!isLoaded) get_upcoming_events();
            else open_animation();
        }
    }

    private void open_animation(){
        layoutAnimationManager.expand(mRecyclerView, 1000, height);
        mRecyclerView.setVisibility(View.VISIBLE);
        isVisible = true;
    }

    private void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    private Date parseDate(String str) {
        SimpleDateFormat sdf = new SimpleDateFormat(FB_DATE_FORMAT);
        Date date = null;
        try {
             date = sdf.parse(str);
        } catch(Exception e) {
        }
        return date;
    }

    private void open_invite_activity(String event_id,String event_name){
        Intent new_activity = new Intent(CreateEvent.this, InviteActivity.class);
        new_activity.putExtra("event_id",event_id);
        new_activity.putExtra("event_name",event_name);
        startActivity(new_activity);
    }

    private void open_event(String event){
        Intent new_activity = new Intent(CreateEvent.this, ViewEvent.class);
        new_activity.putExtra("event_id",event);
        startActivity(new_activity);
    }

    private void get_event(){
        try {
            all_events.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                        Event event = messageSnapshot.getValue(Event.class);
                        if(event.fb_event_id!=null){
                            if (event.fb_event_id.trim().equals(selected_event.event_id.trim())){
                                existing_event_display(event);
                                break;
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w("", "Failed to read value.", error.toException());
                }
            });
        }
        catch (Exception e){
        }
    }

    private void existing_event_display(Event event){
        List<Event> list= new ArrayList<>();
        list.add(event);

        mAdapter = new MainAdapter(CreateEvent.this, list);
        mAdapter.setOnRecyclerViewItemClickListener(new MainAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClicked(CharSequence text) {
                open_event(text.toString());
            }
        });
        rec_view.setLayoutManager(mLinearLayoutManager_FB);
        rec_view.setAdapter(mAdapter);

        rec_view.setVisibility(View.VISIBLE);
    }
}
