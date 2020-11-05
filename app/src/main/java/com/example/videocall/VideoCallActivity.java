package com.example.videocall;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoCallActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

    private static String API_KEY = "46977164";
    private static String SESSION_ID = "1_MX40Njk3NzE2NH5-MTYwNDU2OTUwNDE5OX5ybjNlVEovRmVzRFlyQ3RDNU5ncXhDeHl-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00Njk3NzE2NCZzaWc9MmIyYWQxYmE3NWVhY2UwZmM0MDI1YzkwNzNkMDM0YjIzZWRjOWIyZDpzZXNzaW9uX2lkPTFfTVg0ME5qazNOekUyTkg1LU1UWXdORFUyT1RVd05ERTVPWDV5YmpObFZFb3ZSbVZ6UkZseVEzUkROVTVuY1hoRGVIbC1mZyZjcmVhdGVfdGltZT0xNjA0NTY5NTY5Jm5vbmNlPTAuNDcwMTkyNTA3NDAwOTkxMTcmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTYwNzE2MTU2OSZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";

    private static final String LOG_TAG = VideoCallActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 1;

    private ImageView closeVideoCallBtn;

    private FrameLayout publisherViewController;
    private FrameLayout subscriberViewController;

    private Publisher publisher;
    private Subscriber subscriber;

    private DatabaseReference usersRef;

    private String userId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        closeVideoCallBtn = findViewById(R.id.close_call_btn);
        closeVideoCallBtn.setOnClickListener(view -> usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(userId).hasChild("Ringing")) {
                    usersRef.child(userId).child("Ringing").removeValue();

                    destroyPublisherAndSubscriber();

                    startActivity(new Intent(VideoCallActivity.this, RegistrationActivity.class));
                    finish();
                } else if (snapshot.child(userId).hasChild("Calling")) {
                    usersRef.child(userId).child("Calling").removeValue();

                    destroyPublisherAndSubscriber();

                    startActivity(new Intent(VideoCallActivity.this, RegistrationActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(VideoCallActivity.this, RegistrationActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoCallActivity.this);

    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

        if (EasyPermissions.hasPermissions(this, perms)) {
            publisherViewController = findViewById(R.id.publisher_container);
            subscriberViewController = findViewById(R.id.subscriber_container);

            // Initialize and Connect the session
            Session session = new Session.Builder(this, API_KEY, SESSION_ID).build();
            session.setSessionListener(VideoCallActivity.this);
            session.connect(TOKEN);
        } else {
            EasyPermissions.requestPermissions(this, "The application requires Camera and Microphone permissions", RC_VIDEO_APP_PERM);
        }
    }

    // Publish the stream to the session
    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");

        publisher = new Publisher.Builder(this).build();
        publisher.setPublisherListener(VideoCallActivity.this);

        publisherViewController.addView(publisher.getView());
        if (publisher.getView() instanceof GLSurfaceView) {
            ((GLSurfaceView) publisher.getView()).setZOrderOnTop(true);
        }

        session.publish(publisher);
    }

    // Subscribing to the published stream
    @Override
    public void onStreamReceived(Session session, Stream stream) {

        Log.i(LOG_TAG, "Stream Received");

        if (subscriber == null) {
            subscriber = new Subscriber.Builder(this, stream).build();
            session.subscribe(subscriber);
            subscriberViewController.addView(subscriber.getView());
        }

    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Stream Disconnected");
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");

        if (subscriber != null) {
            subscriber = null;
            subscriberViewController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG, "Stream Error");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void destroyPublisherAndSubscriber() {
        if (publisher != null) {
            publisher.destroy();
            ;
        }
        if (subscriber != null) {
            subscriber.destroy();
            ;
        }
    }
}