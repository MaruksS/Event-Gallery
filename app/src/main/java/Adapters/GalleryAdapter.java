package Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.Environment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.s_maruks.tutinava.eventgallery.R;

import java.io.File;
import java.util.Collections;
import java.util.List;
import Entities.Photo;

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

        File memory = Environment.getExternalStorageDirectory();
        File dir = new File(memory.getAbsolutePath() + "/Event-Gallery/events/"+current.event_id);
        dir.mkdirs();
        String fileName = current.photo_id+".jpg";
        File outFile = new File(dir, fileName);

        if(outFile.exists()){
            Uri imageUri = Uri.fromFile(outFile);
            Glide.with(context)
                    .load(imageUri)
                    .asBitmap()
                    .centerCrop()
                    .into(GalleryViewHolder.display_image);
        }
        else {
            Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(photoRef)
                    .asBitmap()
                    .centerCrop()
                    .into(GalleryViewHolder.display_image);
        }
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

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
