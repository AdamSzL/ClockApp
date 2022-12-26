package com.example.clockapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.room.Room;

import com.example.clockapp.db.AppDatabase;
import com.example.clockapp.db.daos.AlarmDao;
import com.example.clockapp.db.entities.Alarm;

import java.io.IOException;

public class AlarmService extends Service {
    private final int MAX_FULL_DURATION = 10000;
    private MediaPlayer mediaPlayer;
    private final int interval = 50;
    private static BroadcastReceiver br_ScreenOffReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        AppDatabase db = Room.databaseBuilder(this.getApplicationContext(),
                AppDatabase.class, "clock").fallbackToDestructiveMigration().allowMainThreadQueries().build();
        AlarmDao alarmDao = db.alarmDao();
        String action = intent.getAction();
        if (action.equals("alarm")) {
            Bundle bundle = intent.getBundleExtra("alarm_bundle");
            Alarm alarm = (Alarm) bundle.getSerializable("alarm");
            Toast.makeText(this, alarm.getLabel(), Toast.LENGTH_LONG).show();
            if (alarm.repeatMode.equals("")) {
                alarm.setEnabled(false);
                alarmDao.updateAlarm(alarm);
            }
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                return START_STICKY;
            }
            mediaPlayer = new MediaPlayer();
            resetIfPlaying();
            try {
                mediaPlayer.setDataSource(this, Uri.parse(alarm.getRingtoneUri()));
                mediaPlayer.setVolume(alarm.getVolume(), alarm.getVolume());
                mediaPlayer.prepare();
                int duration = mediaPlayer.getDuration();
                if (duration >= MAX_FULL_DURATION) {
                    handlePartialRingtone(alarm.getRingtoneStart(), alarm.getRingtoneEnd());
                } else {
                    mediaPlayer.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return START_STICKY;
    }

    private void resetIfPlaying() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }

    private void handlePartialRingtone(float start, float end) {
        mediaPlayer.seekTo((int) start);
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayer.getCurrentPosition() < end) {
                            handler.postDelayed(this, interval);
                        } else {
                            mediaPlayer.stop();
                            mediaPlayer.reset();
                            mediaPlayer.release();
                        }
                    }
                }, interval);
            }
        });
    }

    @Override
    public void onCreate()
    {
        registerScreenOffReceiver();
    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(br_ScreenOffReceiver);
        br_ScreenOffReceiver = null;
    }

    private void registerScreenOffReceiver()
    {
        br_ScreenOffReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                // do something, e.g. send Intent to main app
            }
        };

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(br_ScreenOffReceiver, filter);
    }
}
