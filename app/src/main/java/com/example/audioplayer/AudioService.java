package com.example.audioplayer;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;

//SONIA MUBASHER
//20129528

public class AudioService extends Service {

    //Declaring variables
    private final IBinder myBinder = new MyLocalBinder();
    public MP3Player audio; //audio object of type MP3Player class
    public String CHANNEL_ID = "Audio Channel";
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

    //Created a Constructor to initialize an object of MP3Player class
    public AudioService() {
        audio = new MP3Player();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return myBinder;
    }

    //Local binder for Audio Service
    public class MyLocalBinder extends Binder {
        AudioService getService() {

            return AudioService.this;
        }
    }

    //To destroy the audio service
    @Override
    public void onDestroy() {
        super.onDestroy();
        removeNotification();
        audio.stop();
    }

    //Initializing Notification manager
    NotificationManager notificationManager;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        notificationManager = getSystemService(NotificationManager.class);
        builder
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Audio Player") //sets the title
                .setContentText("The Song is Now Playing") //sets the text to be displayed on notification
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //When the user clicks on notification this will redirect the user to main activity
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        //Creating a notification channel
        NotificationChannel audioChannel = new NotificationChannel(CHANNEL_ID, "audioPlayer", IMPORTANCE_DEFAULT);
        audioChannel.setDescription("Audio notification");
        notificationManager.createNotificationChannel(audioChannel);

        //To display the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationId = 2;
        notificationManager.notify(notificationId, builder.build());
        return super.onStartCommand(intent, flags, startId);
    }

    //To remove the notification
    public void removeNotification() {
        NotificationManager Manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        Manager.cancel(0);
    }

}
