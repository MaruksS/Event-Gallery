package com.s_maruks.tutinava.eventgallery;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import Adapters.MainAdapter;
import Entities.Event;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Firebase references
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private DatabaseReference user_events;
    private DatabaseReference sinle_event;

    //RecyclerView - related
    private MainAdapter adapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    //Other data types
    private String creator;
    private FloatingActionButton Fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecyclerView = (RecyclerView) findViewById(R.id.rw_main);
        mLinearLayoutManager = new LinearLayoutManager(this);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    creator = mAuth.getCurrentUser().getUid().toString();
                    user_events= mDatabase.child("users").child(creator).child("Attending");
                    get_events();
                } else {
                    open_login_screen();
                }
            }
        };

        Fab = (FloatingActionButton) findViewById(R.id.fab);
        Fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                create_event();
            }
        });
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
            case R.id.Join:
                open_join_event();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_create :
                create_event();
                break;
        }
    }

    private void create_event(){
        Intent new_activity = new Intent(MainActivity.this, CreateEvent.class);
        startActivity(new_activity);
    }

    private void display_data(List<Event> events){
        adapter = new MainAdapter(MainActivity.this, events);
        adapter.setOnRecyclerViewItemClickListener(new MainAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClicked(CharSequence text) {
                open_event(text.toString());
            }
        });
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(adapter);
    }

    private void signOut() {
        LoginManager.getInstance().logOut();
        FirebaseAuth.getInstance().signOut();
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

    private void open_event(String event){
        Intent new_activity = new Intent(MainActivity.this, ViewEvent.class);
        new_activity.putExtra("event_id",event);
        startActivity(new_activity);
    }

    private void open_join_event(){
        Intent new_activity = new Intent(MainActivity.this, JoinEventActivity.class);
        startActivity(new_activity);
    }


    private void get_events(){
        final List<Event> displayed_events= new ArrayList<>();
        try {
            //getting all events id from users.user_id.attending
            user_events.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                        String event_id = messageSnapshot.getKey();
                        sinle_event=mDatabase.child("events").child(event_id);

                        //getting all events 1 by 1
                        sinle_event.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Event event = dataSnapshot.getValue(Event.class);
                                displayed_events.add(event);
                                display_data(displayed_events);
                            }
                            @Override
                            public void onCancelled(DatabaseError error) {
                                // Failed to read value
                                Log.w("", "Failed to read value.", error.toException());
                            }
                        });
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

}
