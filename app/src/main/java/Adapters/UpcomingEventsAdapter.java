package Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.s_maruks.tutinava.eventgallery.R;

import java.util.Collections;
import java.util.List;

import Entities.FBEvent;

import static com.s_maruks.tutinava.eventgallery.R.id.iw_image;

/**
 * Created by Sergejs on 17/10/2017.
 */

public class UpcomingEventsAdapter extends RecyclerView.Adapter<UpcomingEventsAdapter.EventsViewHolder>{
    private LayoutInflater inflater;
    List<FBEvent> events = Collections.emptyList();
    String photoRef;

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
        String name = current.name;
        String date = current.start_time;
        photoRef= current.image_url;
        if (photoRef=="placeholder"){
            Glide.with(UpcomingEventsAdapter.EventsViewHolder.display_image.getContext())
                    .load(R.drawable.ic_photo_placeholder)
                    .into(UpcomingEventsAdapter.EventsViewHolder.display_image);
        }
        else{
            Glide.with(UpcomingEventsAdapter.EventsViewHolder.display_image.getContext())
                    .load(photoRef)
                    .asBitmap()
                    .into(UpcomingEventsAdapter.EventsViewHolder.display_image);
        }
        EventsViewHolder.event_date.setText(date);
        EventsViewHolder.event_name.setText(name);
        EventsViewHolder.event_layout.setTag(current.event_id);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class EventsViewHolder extends RecyclerView.ViewHolder{
        private static ImageView display_image;
        private static TextView event_name;
        private static TextView event_date;
        private static RelativeLayout event_layout;
        private EventsViewHolder(View itemView) {
            super(itemView);
            display_image = (ImageView) itemView.findViewById(iw_image);
            event_name = (TextView)itemView.findViewById(R.id.txt_name);
            event_date = (TextView)itemView.findViewById(R.id.txt_date);
            event_layout= (RelativeLayout) itemView.findViewById(R.id.event_layout);
            display_image.setMinimumWidth(50);
            display_image.setMinimumHeight(50);
            event_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // send the text to the listener, i.e Activity.
                    mListener.onItemClicked((CharSequence) v.getTag());
                }
            });
        }
    }
}
