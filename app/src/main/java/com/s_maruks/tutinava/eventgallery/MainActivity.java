package com.s_maruks.tutinava.eventgallery;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import Adapters.MainAdapter;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MainAdapter adapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference user_events;
    String creator;
    private static String selected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecyclerView = (RecyclerView) findViewById(R.id.rw_main);
        mLinearLayoutManager = new LinearLayoutManager(this);

        findViewById(R.id.btn_create).setOnClickListener(this);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    creator = mAuth.getCurrentUser().getUid().toString();
                    user_events= mDatabase.child("users").child(creator).child("Created events");
                    display_data();
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
    private void display_data(){
        adapter = new MainAdapter(MainActivity.this, get_events());
        adapter.setOnRecyclerViewItemClickListener(new MainAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClicked(CharSequence text) {
                open_event(text.toString());
            }
        });
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                mRecyclerView.setLayoutManager(mLinearLayoutManager);
                mRecyclerView.setAdapter(adapter);
            }
        }, 1500);   //1.5 seconds
    }
    private List<Event> get_events(){
        final List<Event> events = new ArrayList<>();

        user_events.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    Event current = new Event();
                    current.name= (String) messageSnapshot.child("name").getValue();
                    events.add(current);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("", "Failed to read value.", error.toException());
            }
        });
        return events;
    }

    private void open_event(String event){
        Intent new_activity = new Intent(MainActivity.this, ViewEvent.class);
        new_activity.putExtra("name",event);
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
