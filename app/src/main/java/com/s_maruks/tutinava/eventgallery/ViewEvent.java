package com.s_maruks.tutinava.eventgallery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import Adapters.GalleryAdapter;

import Entities.Photo;

import static android.R.attr.bitmap;
import static android.media.CamcorderProfile.get;
import static com.s_maruks.tutinava.eventgallery.R.id.imageView;


public class ViewEvent extends AppCompatActivity implements View.OnClickListener {

    private FirebaseStorage mStorage;
    private StorageReference mStorageRef, eventRef, camera_img;
    private DatabaseReference mDatabase, user_events;

    private GalleryAdapter adapter;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String user,event_id, photo_id;
    private Intent intent;

    List<Photo> all_photos = new ArrayList<>();

    private final int PICK_Camera_IMAGE = 2;
    private final int SELECT_FILE1 = 1;
    public static String imagePath;
    private File destination;
    private Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);

        intent=getIntent();
        event_id=intent.getStringExtra("event_id");

        mRecyclerView= (RecyclerView)findViewById(R.id.rw_gallery);
        mGridLayoutManager = new GridLayoutManager(ViewEvent.this,3);



        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser().getUid().toString();
        user_events = mDatabase.child("users").child(user).child("Created events");
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();
        eventRef = mStorageRef.child("events").child(event_id);

        findViewById(R.id.camera).setOnClickListener(this);
        findViewById(R.id.gallery).setOnClickListener(this);


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    display_photos();
                } else {
                    open_login_screen();
                }
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera:
                openCamera();
                break;
            case R.id.gallery:
                openGallery(SELECT_FILE1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        InputStream stream = null;
        UploadTask uploadTask=null;
        switch (requestCode) {
            case SELECT_FILE1:
                if (resultCode == Activity.RESULT_OK) {
                    selectedImage = imageReturnedIntent.getData();
                    photo_id = generate_photo_id();
                    camera_img = eventRef.child(photo_id);

                    uploadTask = camera_img.putFile(selectedImage);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Photo photo = new Photo(photo_id, event_id);
                            mDatabase.child("events").child(event_id).child("photos").child(photo_id).setValue(photo);
                        }
                    });
                    break;
                }
            case PICK_Camera_IMAGE:
                if (resultCode == RESULT_OK) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;
                    imagePath = destination.getAbsolutePath();
                    photo_id = generate_photo_id();
                    camera_img = eventRef.child(photo_id);

                    try {
                        stream = new FileInputStream(imagePath);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    uploadTask = camera_img.putStream(stream);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Photo photo = new Photo(photo_id, event_id);
                            mDatabase.child("events").child(event_id).child("photos").child(photo_id).setValue(photo);
                        }
                    });
                    break;
                }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If there's a download in progress, save the reference so you can query it later
        if (mStorageRef != null) {
            outState.putString("reference", mStorageRef.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // If there was a download in progress, get its reference and create a new StorageReference
        final String stringRef = savedInstanceState.getString("reference");
        if (stringRef == null) {
            return;
        }
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef);

        // Find all DownloadTasks under this StorageReference (in this example, there should be one)
        List<FileDownloadTask> tasks = mStorageRef.getActiveDownloadTasks();
        if (tasks.size() > 0) {
            // Get the task monitoring the download
            FileDownloadTask task = tasks.get(0);

            // Add new listeners to the task using an Activity scope
            task.addOnSuccessListener(this, new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot state) {
                }
            });
        }
    }

    private void download_photos_to_memory(){

        for (int i=0;i<all_photos.size();i++){
            String photo_id =all_photos.get(i).photo_id;
            download_photo(eventRef.child(photo_id),photo_id);
        }
    }

    private void download_photo(final StorageReference photo, final String photo_id) {

        try {
            final File localFile = File.createTempFile(photo_id, "jpg");
            photo.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap bmp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                FileOutputStream outStream = null;
                File memory = Environment.getExternalStorageDirectory();
                File dir = new File(memory.getAbsolutePath() + "/Event-Gallery/events/"+event_id);
                dir.mkdirs();
                String fileName = photo_id+".jpg";
                File outFile = new File(dir, fileName);
                if(outFile.exists()){
                    Log.d("message","exists");
                }
                else{
                    try {
                        outStream = new FileOutputStream(outFile);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                        outStream.flush();
                        outStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    MediaScannerConnection.scanFile(ViewEvent.this, new String[] { outFile.getAbsolutePath()},
                            null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.d("complete","scan complete");
                                }
                            });
                }
                localFile.deleteOnExit();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("exception", exception.toString());
            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d("exception", taskSnapshot.toString());
            }
        });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void display_photos(){
        adapter = new GalleryAdapter(ViewEvent.this, get_photos());

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                adapter.setOnRecyclerViewItemClickListener(new GalleryAdapter.OnRecyclerViewItemClickListener() {
                    @Override
                    public void onItemClicked(CharSequence text) {
                        Log.d("message","gg");
                    }
                });
                mRecyclerView.setLayoutManager(mGridLayoutManager);
                mRecyclerView.setAdapter(adapter);
                download_photos_to_memory();
            }
        }, 2500);   //1.5 seconds
    }

    private List<Photo> get_photos(){
        final List<Photo> public_photos = new ArrayList<>();
        DatabaseReference event_photos = mDatabase.child("events").child(event_id).child("photos");
        try {
            event_photos.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                        Photo photo = messageSnapshot.getValue(Photo.class);
                        public_photos.add(photo);
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w("", "Failed to read value.", error.toException());
                }

            });
        }
        catch (Exception e){

        }
        all_photos=public_photos;
        return public_photos;
    }

    public void openCamera(){
        String name = dateToString(new Date(), "yyyy-MM-dd-hh-mm-ss");
        destination = new File(Environment
                .getExternalStorageDirectory(), name + ".jpg");

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(destination));
        startActivityForResult(intent, PICK_Camera_IMAGE);
    }

    public void openGallery(int SELECT_FILE1) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Select file to upload "),
                SELECT_FILE1);
    }

    private void open_login_screen() {
        Intent new_activity = new Intent(ViewEvent.this, LoginActivity.class);
        new_activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        new_activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        new_activity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(new_activity);
        finish();
    }

    private String dateToString(Date date, String s) {

        DateFormat df = new SimpleDateFormat(s);
        String reportDate = df.format(date);
        return reportDate;
    }

    private String generate_photo_id(){
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String generated_nr = salt.toString();
        return generated_nr;
    }

}