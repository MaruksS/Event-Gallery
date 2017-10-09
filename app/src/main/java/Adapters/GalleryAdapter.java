package Adapters;

import android.content.Context;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.s_maruks.tutinava.eventgallery.R;
import com.s_maruks.tutinava.eventgallery.ViewEvent;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import Entities.Event;
import Entities.Photo;

import static Adapters.GalleryAdapter.GalleryViewHolder.display_image;
import static com.s_maruks.tutinava.eventgallery.R.id.imageView;



public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>{
    private LayoutInflater inflater;
    List<Photo> photos = Collections.emptyList();

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


    public GalleryAdapter(Context context, List<Photo> photo){
        inflater = LayoutInflater.from(context);
        this.photos=photo;
    }

    @Override
    public GalleryAdapter.GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.gallery_display_layout, parent,false);
        GalleryViewHolder holder = new GalleryViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(GalleryAdapter.GalleryViewHolder holder, int position) {
        Photo current = photos.get(position);
        Context context = GalleryViewHolder.display_image.getContext();

        FirebaseStorage mStorage = FirebaseStorage.getInstance();
        StorageReference mStorageRef = mStorage.getReference();
        StorageReference photoRef = mStorageRef.child("events").child(current.event_id).child(current.photo_id);

        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(photoRef)
                .into(GalleryViewHolder.display_image);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class GalleryViewHolder extends RecyclerView.ViewHolder{
        static ImageView display_image;
        public GalleryViewHolder(View itemView) {
            super(itemView);
            display_image = (ImageView) itemView.findViewById(imageView);
            display_image.setMinimumWidth(50);
            display_image.setMinimumHeight(50);
            display_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // send the text to the listener, i.e Activity.
                    mListener.onItemClicked((CharSequence) v.getTag());
                }
            });
        }
    }
}
