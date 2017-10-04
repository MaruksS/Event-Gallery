package com.s_maruks.tutinava.eventgallery;

/**
 * Created by Tutinava on 29.09.2017.
 */

public class User {

    public String username;
    public String email;
    public String id;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email,String id) {
        this.username = username;
        this.email = email;
        this.id= id;
    }

}
