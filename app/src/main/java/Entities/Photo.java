package Entities;

import java.util.List;

/**
 * Created by uizen on 08/10/2017.
 */

public class Photo {

    public String photo_id;
    public String event_id;

    public Photo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Photo( String photo_id,String event_id) {
        this.photo_id= photo_id;
        this.event_id = event_id;
    }
}
