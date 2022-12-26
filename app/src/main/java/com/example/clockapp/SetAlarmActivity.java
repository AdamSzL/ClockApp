package com.example.clockapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.clockapp.db.daos.AlarmDao;
import com.example.clockapp.db.entities.Alarm;
import com.example.clockapp.helpers.AlarmSetter;
import com.example.clockapp.helpers.Helpers;
import com.example.clockapp.ui.alarm.AlarmFragment;
import com.google.android.material.slider.RangeSlider;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class SetAlarmActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private LinearLayout alarmLabelContainer;
    private LinearLayout alarmRingtoneContainer;
    private LinearLayout ringtonePartContainer;
    private Button setAlarmBtn;
    private TextView repeatDailyButton;
    private TextView repeatWeeklyButton;
    private TextView repeatMonthlyButton;
    private TextView labelTextView;
    private TextView alarmRingtoneTitle;
    private RangeSlider volumeSlider;
    private RangeSlider ringtonePartSlider;
    private String alarmLabel = "Alarm";
    private String alarmRingtone = "None";
    private String selectedRepeatMode = "";
    private float selectedVolume = 0.5f;
    private int selectedHour = 0;
    private int selectedMinute = 0;
    private String mode = "";
    private Alarm updatedAlarm = null;
    private int MAX_FULL_DURATION = 10000;
    private AlarmDao alarmDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);

        alarmDao = AlarmFragment.db.alarmDao();

        alarmLabelContainer = findViewById(R.id.alarm_label_container);
        alarmRingtoneContainer = findViewById(R.id.alarm_ringtone_container);
        setAlarmBtn = findViewById(R.id.set_alarm_btn);
        volumeSlider = findViewById(R.id.volume_slider);
        ringtonePartSlider = findViewById(R.id.ringtone_part_slider);
        labelTextView = findViewById(R.id.label_text_view);
        alarmRingtoneTitle = findViewById(R.id.alarm_ringtone_title);
        ringtonePartContainer = findViewById(R.id.ringtone_part_container);
        volumeSlider.addOnChangeListener((slider, value, fromUser) -> {
            selectedVolume = value;
        });

        handleRepeatModeSelection();

        alarmLabelContainer.setOnClickListener(view -> {
            showAlarmLabelDialog();
        });

        alarmRingtoneContainer.setOnClickListener(view -> {
            showAlarmRingtoneDialog();
        });

        setAlarmBtn.setOnClickListener(view -> {
            setAlarm();
        });

        handleTimePicker();

        timePicker.setOnTimeChangedListener((timePicker, hour, minute) -> {
            selectedHour = hour;
            selectedMinute = minute;
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Set an alarm");
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        mode = bundle.getString("mode");
        if (mode.equals("update")) {
            setBaseValues(bundle);
        } else if (mode.equals("insert")) {
            setDefaultValues();
        }
    }

    private void setBaseValues(Bundle bundle) {
        updatedAlarm = (Alarm) bundle.getSerializable("alarm");
        String time = updatedAlarm.getTime();
        String repeat = updatedAlarm.getRepeatMode();
        String label = updatedAlarm.getLabel();
        String ringtoneUri = updatedAlarm.getRingtoneUri();
        float volume = updatedAlarm.getVolume();
        float ringtoneStart = updatedAlarm.getRingtoneStart();
        float ringtoneEnd = updatedAlarm.getRingtoneEnd();
        int hour = Integer.parseInt(time.charAt(0) + Character.toString(time.charAt(1)));
        int minute = Integer.parseInt(time.charAt(3) + Character.toString(time.charAt(4)));
        timePicker.setHour(hour);
        timePicker.setMinute(minute);
        selectedRepeatMode = repeat;
        labelTextView.setText(label);
        alarmLabel = label;
        if (!ringtoneUri.equals("None")) {
            Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(ringtoneUri));
            String title = ringtone.getTitle(this);
            alarmRingtoneTitle.setText(title);
        }
        alarmRingtone = ringtoneUri;
        volumeSlider.setValues(volume);
        handleRingtonePartSlider(ringtoneUri, ringtoneStart, ringtoneEnd);
        handleDefaultRepeatMode();
    }

    private void setDefaultValues() {
        SharedPreferences sharedPreferences = getSharedPreferences("Clock", MODE_PRIVATE);
        String defaultRingtone = sharedPreferences.getString("ringtone", "");
        float defaultVolume = sharedPreferences.getFloat("volume", 0);
        float ringtoneStart = sharedPreferences.getFloat("ringtoneStart", 0f);
        float ringtoneEnd = sharedPreferences.getFloat("ringtoneEnd", 0f);
        selectedVolume = defaultVolume;
        if (!defaultRingtone.equals("")) {
            Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(defaultRingtone));
            String title = ringtone.getTitle(this);
            alarmRingtoneTitle.setText(title);
            alarmRingtone = defaultRingtone;

            handleRingtonePartSlider(defaultRingtone, ringtoneStart, ringtoneEnd);
        }
        volumeSlider.setValues(defaultVolume);
    }

    private void handleRingtonePartSlider(String ringtone, float ringtoneStart, float ringtoneEnd) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, Uri.parse(ringtone));
            mediaPlayer.prepare();
            int duration = mediaPlayer.getDuration();
            if (duration >= MAX_FULL_DURATION) {
                ringtonePartContainer.setVisibility(View.VISIBLE);
                ringtonePartSlider.setValueFrom(0);
                ringtonePartSlider.setValueTo(duration);
                ringtonePartSlider.setValues(ringtoneStart, ringtoneEnd);
            } else {
                ringtonePartContainer.setVisibility(View.GONE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDefaultRepeatMode() {
        if (selectedRepeatMode.equals("daily")) {
            markOptionSelected(repeatDailyButton, selectedRepeatMode);
        } else if (selectedRepeatMode.equals("weekly")) {
            markOptionSelected(repeatWeeklyButton, selectedRepeatMode);
        } else if (selectedRepeatMode.equals("monthly")) {
            markOptionSelected(repeatMonthlyButton, selectedRepeatMode);
        }
    }

    private void handleTimePicker() {
        timePicker = findViewById(R.id.time_picker);
        timePicker.setIs24HourView(DateFormat.is24HourFormat(this));
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        selectedHour = calendar.get(Calendar.HOUR_OF_DAY);
        selectedMinute = calendar.get(Calendar.MINUTE);
    }

    private void handleRepeatModeSelection() {
        repeatDailyButton = findViewById(R.id.repeat_daily_button);
        repeatWeeklyButton = findViewById(R.id.repeat_weekly_button);
        repeatMonthlyButton = findViewById(R.id.repeat_monthly_button);

        handleOptionSelection(repeatDailyButton, "daily");
        handleOptionSelection(repeatWeeklyButton, "weekly");
        handleOptionSelection(repeatMonthlyButton, "monthly");
    }

    private void handleOptionSelection(TextView option, String mode) {
        option.setOnClickListener(view -> {
            if (selectedRepeatMode.equals(mode)) {
                resetOption(option);
            } else {
                markOptionSelected(option, mode);
            }
        });
    }

    private String getSelectedTime() {
        return Helpers.formatIntegerToTwoDigits(selectedHour) + ":" + Helpers.formatIntegerToTwoDigits(selectedMinute);
    }

    private void markOptionSelected(TextView option, String mode) {
        resetOption(repeatDailyButton);
        resetOption(repeatWeeklyButton);
        resetOption(repeatMonthlyButton);
        option.setBackgroundColor(ContextCompat.getColor(this, com.google.android.material.R.color.design_dark_default_color_primary));
        selectedRepeatMode = mode;
    }

    private void resetOption(TextView option) {
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                option.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_gray));
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                option.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
                break;
        }
    }

    private void showAlarmLabelDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Set alarm");
        alert.setMessage("Enter a label");
        final EditText labelInput = new EditText(this);
        labelInput.setSingleLine();
        labelInput.setText(alarmLabel);
        labelInput.setInputType(InputType.TYPE_CLASS_TEXT);
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_input_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_input_margin);
        container.addView(labelInput);
        labelInput.setLayoutParams(params);
        alert.setView(container);
        alert.setPositiveButton("Set", (dialogInterface, i) -> {
            String label = labelInput.getText().toString();
            if (!label.equals("")) {
                alarmLabel = label;
                labelTextView.setText(label);
            } else {
                Toast.makeText(this, "Alarm label must not be empty", Toast.LENGTH_LONG).show();
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }

    ActivityResultLauncher<Intent> ringtoneActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
                    String title = ringtone.getTitle(this);
                    alarmRingtoneTitle.setText(title);
                    alarmRingtone = uri.toString();

                    MediaPlayer mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(this, uri);
                        mediaPlayer.prepare();
                        int duration = mediaPlayer.getDuration();
                        if (duration >= MAX_FULL_DURATION) {
                            ringtonePartContainer.setVisibility(View.VISIBLE);
                            ringtonePartSlider.setValueFrom(0);
                            ringtonePartSlider.setValueTo(duration);
                            ringtonePartSlider.setValues(0f, (float) duration);
                        } else {
                            ringtonePartContainer.setVisibility(View.GONE);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

    private void showAlarmRingtoneDialog() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        ringtoneActivityLauncher.launch(intent);
    }

    private void setAlarm() {
        if (mode.equals("insert")) {
            Toast.makeText(this, "Alarm has been set", Toast.LENGTH_LONG).show();
            MediaPlayer mediaPlayer = new MediaPlayer();
            float ringtoneStart = 0f;
            float ringtoneEnd = 0f;
            try {
                mediaPlayer.setDataSource(this, Uri.parse(alarmRingtone));
                mediaPlayer.prepare();
                int duration = mediaPlayer.getDuration();
                if (duration >= MAX_FULL_DURATION) {
                    List<Float> values = ringtonePartSlider.getValues();
                    ringtoneStart = values.get(0);
                    ringtoneEnd = values.get(1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Alarm alarm = new Alarm(
                    true,
                    false,
                    getSelectedTime(),
                    alarmLabel,
                    selectedRepeatMode,
                    alarmRingtone,
                    ringtoneStart,
                    ringtoneEnd,
                    selectedVolume);
            long id = alarmDao.insertAlarm(alarm);
            alarm.setUid(id);
            AlarmSetter.setAlarm(alarm, this);
        } else if (mode.equals("update")) {
            Toast.makeText(this, "Alarm has been updated", Toast.LENGTH_LONG).show();
            updatedAlarm.setTime(getSelectedTime());
            updatedAlarm.setLabel(alarmLabel);
            updatedAlarm.setRepeatMode(selectedRepeatMode);
            updatedAlarm.setRingtoneUri(alarmRingtone);
            updatedAlarm.setVolume(selectedVolume);
            List<Float> values = ringtonePartSlider.getValues();
            updatedAlarm.setRingtoneStart(values.get(0));
            updatedAlarm.setRingtoneEnd(values.get(1));
            updatedAlarm.setEnabled(true);
            alarmDao.updateAlarm(updatedAlarm);
            AlarmSetter.cancelAlarm(updatedAlarm, this);
            AlarmSetter.setAlarm(updatedAlarm, this);
        }
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}