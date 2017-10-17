package Adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.s_maruks.tutinava.eventgallery.CreateEvent;
import com.s_maruks.tutinava.eventgallery.R;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import Entities.FBEvent;

import static com.s_maruks.tutinava.eventgallery.R.id.event_name;
import static com.s_maruks.tutinava.eventgallery.R.id.iw_image;

/**
 * Created by Sergejs on 17/10/2017.
 */

public class UpcomingEventsAdapter extends RecyclerView.Adapter<UpcomingEventsAdapter.EventsViewHolder>{
    private LayoutInflater inflater;
    List<FBEvent> events = Collections.emptyList();
    JSONObject object;
    JSONObject data_object;
    String photoRef;
    String name;
    String date;
    int id;

    // Define listener member variable
    private static UpcomingEventsAdapter.OnRecyclerViewItemClickListener mListener;

    // Define the listener interface
    public interface OnRecyclerViewItemClickListener {
        void onItemClicked(CharSequence text);
    }

    // Define the method that allows the parent activity or fragment to define the listener.
    public void setOnRecyclerViewItemClickListener(UpcomingEventsAdapter.OnRecyclerViewItemClickListener listener) {
        this.mListener = listener;
    }


    public UpcomingEventsAdapter(Context context, List<FBEvent> events){
        inflater = LayoutInflater.from(context);
        this.events=events;
    }

    @Override
    public UpcomingEventsAdapter.EventsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.upcoming_fb_event_display_layout, parent,false);
        UpcomingEventsAdapter.EventsViewHolder holder = new UpcomingEventsAdapter.EventsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(UpcomingEventsAdapter.EventsViewHolder holder, int position) {
        FBEvent current = events.get(position);
        String name = current.Name;
        String date = current.start_time;
        long id = current.event_id;


        Context context = UpcomingEventsAdapter.EventsViewHolder.display_image.getContext();

        String path =  "/"+id+"/picture";
        AccessToken token =AccessToken.getCurrentAccessToken();

        new GraphRequest(
                token,
                path,
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            object = response.getJSONObject();
                            data_object = object.getJSONObject("data");
                            String photoRef=data_object.getString("url");
                            Glide.with(UpcomingEventsAdapter.EventsViewHolder.display_image.getContext())
                                    .load(photoRef)
                                    .asBitmap()
                                    .into(UpcomingEventsAdapter.EventsViewHolder.display_image);

                        }
                        catch(Exception e){
                        }
                    }
                }
        ).executeAsync();


        EventsViewHolder.event_date.setText(date);
        EventsViewHolder.event_name.setText(name);

    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class EventsViewHolder extends RecyclerView.ViewHolder{
        static ImageView display_image;
        static TextView event_name;
        static TextView event_date;
        public EventsViewHolder(View itemView) {
            super(itemView);
            display_image = (ImageView) itemView.findViewById(iw_image);
            display_image.setMinimumWidth(50);
            display_image.setMinimumHeight(50);
            display_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // send the text to the listener, i.e Activity.
                    mListener.onItemClicked((CharSequence) v.getTag());
                }
            });
            event_name = (TextView)itemView.findViewById(R.id.txt_name);
            event_date = (TextView)itemView.findViewById(R.id.txt_date);
        }
    }
}
