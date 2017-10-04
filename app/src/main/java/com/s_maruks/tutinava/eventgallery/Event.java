package com.s_maruks.tutinava.eventgallery;

import java.util.List;

/**
 * Created by uizen on 01/10/2017.
 */

public class Event {

    public String Name;
    public String creator_id;
    public String event_id;
    public List<User> participants;

    public Event() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Event(String name, String creator_id,String event_id, List<User> participants) {
        this.Name = name;
        this.creator_id=creator_id;
        this.event_id= event_id;
        this.participants=participants;
    }

}
