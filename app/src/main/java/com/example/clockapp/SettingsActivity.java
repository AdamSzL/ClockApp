package com.example.clockapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.slider.RangeSlider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public class SettingsActivity extends AppCompatActivity {
    private TextView defaultRingtoneTextView;
    private TextView timeZoneTextView;
    private RangeSlider defaultVolumeSlider;
    private RangeSlider ringtonePartSlider;
    private Spinner themeSpinner;
    private LinearLayout ringtoneContainer;
    private LinearLayout timeZoneContainer;
    private LinearLayout ringtonePartContainer;
    private SharedPreferences sharedPreferences;
    final private ArrayList<String> availableThemes = new ArrayList<>(Arrays.asList("light", "dark"));
    private int MAX_FULL_DURATION = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        defaultRingtoneTextView = findViewById(R.id.default_ringtone_text_view);
        defaultVolumeSlider = findViewById(R.id.default_ringtone_volume_slider);
        ringtonePartSlider = findViewById(R.id.ringtone_selected_part_slider);
        themeSpinner = findViewById(R.id.theme_spinner);
        timeZoneTextView = findViewById(R.id.time_zone_text_view);
        timeZoneContainer = findViewById(R.id.time_zone_container);
        ringtoneContainer = findViewById(R.id.ringtone_container);
        ringtonePartContainer = findViewById(R.id.ringtone_part_container);

        setDefaultValues();
        handleSettingsChange();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onResume() {
        setDefaultValues();
        super.onResume();
    }


    private void setDefaultValues() {
        sharedPreferences = getSharedPreferences("Clock", MODE_PRIVATE);
        String timeZone = TimeZone.getDefault().getID();
        String theme = sharedPreferences.getString("theme", "");
        String defaultRingtone = sharedPreferences.getString("ringtone", "");
        float defaultVolume = sharedPreferences.getFloat("volume", 0);

        if (!defaultRingtone.equals("")) {
            Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(defaultRingtone));
            String title = ringtone.getTitle(this);
            defaultRingtoneTextView.setText(title);

            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(this, Uri.parse(defaultRingtone));
                mediaPlayer.prepare();
                int duration = mediaPlayer.getDuration();
                if (duration >= MAX_FULL_DURATION) {
                    ringtonePartContainer.setVisibility(View.VISIBLE);
                    float ringtoneStart = sharedPreferences.getFloat("ringtoneStart", 0f);
                    float ringtoneEnd = sharedPreferences.getFloat("ringtoneEnd", 0f);
                    ringtonePartSlider.setValueFrom(0);
                    ringtonePartSlider.setValueTo(duration);
                    ringtonePartSlider.setValues(ringtoneStart, ringtoneEnd);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        defaultVolumeSlider.setValues(defaultVolume);
        timeZoneTextView.setText(timeZone);

        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, availableThemes);
        themeSpinner.setAdapter(themeAdapter);
        themeSpinner.setSelection(availableThemes.indexOf(theme));
    }

    private void handleSettingsChange() {
        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String theme = availableThemes.get(i);
                if (theme.equals("light")) {
                    editor.putString("theme", "light");
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else if (theme.equals("dark")) {
                    editor.putString("theme", "dark");
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        ringtoneContainer.setOnClickListener(view -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
            defaultRingtoneActivityLauncher.launch(intent);
        });

        defaultVolumeSlider.addOnChangeListener((slider, value, fromUser) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("volume", value);
            editor.commit();
        });

        ringtonePartSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = ringtonePartSlider.getValues();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("ringtoneStart", values.get(0));
            editor.putFloat("ringtoneEnd", values.get(1));
            editor.commit();
        });

        timeZoneContainer.setOnClickListener(view -> {
            startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
        });
    }

    ActivityResultLauncher<Intent> defaultRingtoneActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
                    String title = ringtone.getTitle(this);
                    SharedPreferences sharedPreferences = getSharedPreferences("Clock", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("ringtone", uri.toString());
                    editor.commit();
                    defaultRingtoneTextView.setText(title);

                    MediaPlayer mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(this, uri);
                        mediaPlayer.prepare();
                        int duration = mediaPlayer.getDuration();
                        handleRingtonePartSelection(duration);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

    private void handleRingtonePartSelection(int duration) {
        SharedPreferences sharedPreferences = getSharedPreferences("Clock", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (duration >= MAX_FULL_DURATION) {
            ringtonePartContainer.setVisibility(View.VISIBLE);
            ringtonePartSlider.setValueFrom(0);
            ringtonePartSlider.setValueTo(duration);
            ringtonePartSlider.setValues(0f, (float) duration);
            editor.putFloat("ringtoneStart", 0);
            editor.putFloat("ringtoneEnd", duration);
        } else {
            ringtonePartContainer.setVisibility(View.GONE);
            editor.putFloat("ringtoneStart", 0);
            editor.putFloat("ringtoneEnd", 0);
        }
        editor.commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}