package Entities;

import java.util.List;

/**
 * Created by Sergejs on 01/10/2017.
 */

public class Event {

    public String name;
    public String creator_id;
    public String event_id;
    public List<User> participants;
    public String fb_event_id;
    public String description;

    public Event() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Event(String name,String description, String creator_id,String event_id, List<User> participants,String fb_event_id) {
        this.name = name;
        this.description = description;
        this.creator_id=creator_id;
        this.event_id= event_id;
        this.participants=participants;
        this.fb_event_id=fb_event_id;
    }

}
