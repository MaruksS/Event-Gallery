package Entities;

import java.util.List;

/**
 * Created by Sergejs on 12/10/2017.
 */

public class FBEvent {

    public String name;
    public String event_id;
    public String description;
    public String start_time;
    public String image_url;

    public FBEvent() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public FBEvent(String name, String start_time,String event_id, String image_url) {
        this.name = name;
        this.event_id= event_id;
        //this.description= description;
        this.start_time=start_time;
        this.image_url= image_url;
    }
}
