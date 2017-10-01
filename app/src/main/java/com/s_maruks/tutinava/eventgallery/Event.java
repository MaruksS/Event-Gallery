package com.s_maruks.tutinava.eventgallery;

/**
 * Created by uizen on 01/10/2017.
 */

public class Event {

    public String name;

    public Event() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Event(String name) {
        this.name = name;
    }

}
