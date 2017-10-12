package Entities;

import java.util.List;

/**
 * Created by Sergejs on 12/10/2017.
 */

public class FBEvent {

    public String Name;
    public int  event_id;
    public String description;
    public String start_time;

    public FBEvent() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public FBEvent(String name, String description, String start_time,int event_id) {
        this.Name = name;
        this.event_id= event_id;
        this.description= description;
        this.start_time=start_time;
    }
}
