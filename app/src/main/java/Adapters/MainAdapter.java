package Adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import Entities.Event;

import com.s_maruks.tutinava.eventgallery.R;

import java.util.Collections;
import java.util.List;


/**
 * Created by Sergejs on 02/10/2017.
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.myViewHolder> {
    private LayoutInflater inflater;
    List<Event> events = Collections.emptyList();

    // Define listener member variable
    private static OnRecyclerViewItemClickListener mListener;

    // Define the listener interface
    public interface OnRecyclerViewItemClickListener {
        void onItemClicked(CharSequence text);
    }

    // Define the method that allows the parent activity or fragment to define the listener.
    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mListener = listener;
    }

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
        holder.event_card.setTag(current.event_id);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {

        TextView event_name;
        CardView event_card;

        public myViewHolder(View itemView) {
            super(itemView);
            event_name= (TextView) itemView.findViewById(R.id.event_name);
            event_card= (CardView) itemView.findViewById(R.id.event_card);
            event_card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // send the text to the listener, i.e Activity.
                    mListener.onItemClicked((CharSequence) v.getTag());
                }
            });

        }
    }
}
