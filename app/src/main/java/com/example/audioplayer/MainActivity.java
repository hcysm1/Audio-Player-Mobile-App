package com.example.audioplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.File;

//SONIA MUBASHER
//20129528
public class MainActivity extends AppCompatActivity {

    //Declaring variables
    AudioService audioService;
    boolean isBound = false;
    private Button Pause; //to pause the songs
    private Button Play; //to play the songs
    private Button Stop; //to stop the songs
    private SeekBar seekBar; //to show the progress of the songs
    private Handler handler = new Handler();


    //Using CustomAdapter to fill the list view with songs
    private class CustomAdapter extends ArrayAdapter<File> {
        File[] songs;

        public CustomAdapter(@NonNull Context context, int resource, @NonNull File[] objects) {
            super(context, resource, objects);
            songs = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                textView = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
                String text = songs[position].getName();
                textView.setText(text);
                return textView;
            } else
                return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //to start audio service
        Intent intent = new Intent(MainActivity.this, AudioService.class);
        bindService(intent, myConnection, 0);
        startService(intent);

        final ListView listView = findViewById(R.id.music_List); //TO DISPLAY MUSIC
        seekBar = findViewById(R.id.seekbar); //to display progress
        Play = findViewById(R.id.button_play); //to play
        Pause = findViewById(R.id.button_pause); //to pause
        Stop = findViewById(R.id.button_Stop); //to stop

        //Searching Songs from the Music Directory
        File musicDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Music/");
        File[] list = musicDir.listFiles(); //to list the songs
        if (list != null) {
            listView.setAdapter(new CustomAdapter(this, android.R.layout.simple_list_item_1, list));

            //onClick Listener for the list when user clicks on any song from the list
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                    File selectedFromList = (File) (listView.getItemAtPosition(myItemInt));
                    audioService.audio.stop();
                    audioService.audio.load(selectedFromList.getAbsolutePath());
                    audioService.audio.play();
                    Play.setText("Playing");
                    Pause.setText("Pause");
                    Stop.setText("Stop");
                    seek();
                }
            });
        }
        //OnClick listener for Pause Button
        Pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioService.audio.mediaPlayer != null) {
                    audioService.audio.pause(); //audio service will call audio object of MP3Player class to pause song
                    Pause.setText("Paused");
                    Play.setText("Play");
                    Stop.setText("Stop");
                    seek();
                }
            }
        });

        //OnClick Listener to play the songs
        Play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioService.audio.mediaPlayer != null) {
                    audioService.audio.play();  //audio service will call audio object of MP3Player class to play song
                    Pause.setText("Pause");
                    Play.setText("Playing");
                    Stop.setText("Stop");
                    seek();
                }
            }
        });
        //onClick Listener to stop the songs
        Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioService.audio.mediaPlayer != null) {
                    audioService.audio.stop(); //audio service will call audio object of MP3Player class to stop song
                    Pause.setText("Pause");
                    Play.setText("Play");
                    Stop.setText("Stopped");
                    seek();
                }
            }
        });
        //To set the progress of the song
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (audioService.audio.mediaPlayer != null) {
                        audioService.audio.setProgress(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }
    //Connecting audioService with main activity
    private ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.MyLocalBinder binder = (AudioService.MyLocalBinder) service;
            audioService = binder.getService();
            isBound = true;
            if (audioService != null && audioService.audio.getState() == MP3Player.MP3PlayerState.PLAYING) {
                Play.setText("Playing");
                Pause.setText("Pause");
                Stop.setText("Stop");
                seek();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    //To update the seek bar when the song is playing
    private void seek() {
        seekBar.setMax(audioService.audio.getDuration());
        if (audioService != null) {
            if (audioService.audio.getState() == MP3Player.MP3PlayerState.PLAYING) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        seekBar.setProgress(audioService.audio.getProgress());
                        seek();
                    }
                };
                handler.postDelayed(runnable, 1000);
            }
        }
    }



}
