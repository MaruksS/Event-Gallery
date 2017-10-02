package Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.s_maruks.tutinava.eventgallery.Event;
import com.s_maruks.tutinava.eventgallery.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static android.R.attr.data;

/**
 * Created by uizen on 02/10/2017.
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.myViewHolder> {

    private LayoutInflater inflater;
    List<Event> events = Collections.emptyList();

    public MainAdapter(Context context, List<Event> events){
        inflater = LayoutInflater.from(context);
        this.events=events;
    }

    @Override
    public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.event_display_layout, parent,false);
        myViewHolder holder = new myViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(myViewHolder holder, int position) {
        Event current = events.get(position);
        holder.event_name.setText(current.name);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder{

        TextView event_name;

        public myViewHolder(View itemView) {
            super(itemView);
            event_name= (TextView) itemView.findViewById(R.id.event_name);
        }
    }
}
