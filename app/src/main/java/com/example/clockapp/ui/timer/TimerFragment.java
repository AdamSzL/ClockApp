package com.example.clockapp.ui.timer;

import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clockapp.R;
import com.example.clockapp.SettingsActivity;
import com.example.clockapp.helpers.Helpers;

import java.util.ArrayList;
import java.util.Arrays;

public class TimerFragment extends Fragment {

    private TimerViewModel mViewModel;
    private NumberPicker hourPicker;
    private NumberPicker minutePicker;
    private NumberPicker secondPicker;
    private ImageView resetTimerBtn;
    private ImageButton startTimerBtn;
    private TextView timer;
    private SwitchCompat learningModeToggleSwitch;
    private RelativeLayout addTimeBtn;
    private Spinner addTimeUnitTypeSpinner;
    private boolean isTimerActive = false;
    private boolean shouldReset = false;
    private long startTime = 0;
    private long timerTime = 0;
    private int interval = 50;

    public static TimerFragment newInstance() {
        return new TimerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(TimerViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initPickers(view);

        timer = view.findViewById(R.id.timer);
        startTimerBtn = view.findViewById(R.id.start_timer_btn);
        resetTimerBtn = view.findViewById(R.id.reset_timer_btn);
        learningModeToggleSwitch = view.findViewById(R.id.learning_mode_toggle_switch);
        addTimeBtn = view.findViewById(R.id.add_time_button);
        addTimeUnitTypeSpinner = view.findViewById(R.id.add_time_unit_type);

        ArrayList<String> units = new ArrayList<>(Arrays.asList("H", "M", "S"));
        ArrayAdapter spinnerAdapter = new ArrayAdapter(
                getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                units
            );
        addTimeUnitTypeSpinner.setAdapter(spinnerAdapter);

        startTimerBtn.setOnClickListener(innerView -> {
            toggleTimer();
        });

        resetTimerBtn.setOnClickListener(innerView -> {
            if (!isTimerActive) {
                resetTimer();
            } else {
                shouldReset = true;
            }
        });

        addTimeBtn.setOnClickListener(innerView -> {
            addTime();
        });
    }

    private void toggleTimer() {
        if (isTimerActive) {
            stopTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer() {
        boolean shouldStart = false;
        if (learningModeToggleSwitch.isChecked()) {
            AudioManager mAlramMAnager = (AudioManager) getActivity().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);

            if(Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 0){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("To use the learning mode properly, turn on the airplane mode")
                        .setTitle("Learning mode")
                        .setCancelable(false)
                        .setPositiveButton("Settings",
                                (dialog, id) -> {
                                    Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                    startActivity(i);
                                }
                        )
                        .setNegativeButton("Cancel",
                                (dialog, id) -> {
                                    Toast.makeText(getActivity(), "You have to enable the airplane mode", Toast.LENGTH_LONG).show();
                                }
                        );
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                shouldStart = true;
            }
        } else {
            shouldStart = true;
        }

        if (shouldStart) {
            isTimerActive = true;
            startTimerBtn.setImageResource(R.drawable.ic_pause);
            togglePickers(false);
            startTime = System.currentTimeMillis();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    long currentTime = System.currentTimeMillis();
                    long time = timerTime - (currentTime - startTime);
                    if (time >= 0) {
                        timer.setText(Helpers.convertMillisToTime(time, false));
                    } else {
                        shouldReset = true;
                    }

                    if (isTimerActive) {
                        if (shouldReset) {
                            resetTimer();
                        } else {
                            handler.postDelayed(this, interval);
                        }
                    } else {
                        timerTime = time;
                    }
                }
            }, interval);
        }
    }

    private void addTime() {
        String timeUnit = addTimeUnitTypeSpinner.getSelectedItem().toString();
        if (timeUnit.equals("H")) {
            timerTime += 3_600_000;
        } else if (timeUnit.equals("M")) {
            timerTime += 60_000;
        } else if (timeUnit.equals("S")) {
            timerTime += 1_000;
        }
        if (!isTimerActive) {
            fixTimer();
        }
    }

    private void fixTimer() {
        String time = Helpers.convertMillisToTime(timerTime, false);
        timer.setText(time);
    }

    private void stopTimer() {
        isTimerActive = false;
        startTimerBtn.setImageResource(R.drawable.ic_play);
    }

    private void togglePickers(boolean enabled) {
        hourPicker.setEnabled(enabled);
        minutePicker.setEnabled(enabled);
        secondPicker.setEnabled(enabled);
        learningModeToggleSwitch.setEnabled(enabled);
    }

    private long getTimerTime() {
        int hour = hourPicker.getValue();
        int minute = minutePicker.getValue();
        int second = secondPicker.getValue();

        return hour * 3_600_000L + minute * 60_000L + second * 1_000L;
    }

    private void resetTimer() {
        hourPicker.setValue(0);
        minutePicker.setValue(0);
        secondPicker.setValue(0);
        timer.setText("00:00:00");
        isTimerActive = false;
        shouldReset = false;
        startTimerBtn.setVisibility(View.GONE);
        resetTimerBtn.setVisibility(View.GONE);
        addTimeBtn.setVisibility(View.GONE);
        addTimeUnitTypeSpinner.setVisibility(View.GONE);
        togglePickers(true);
        timerTime = 0;
        learningModeToggleSwitch.setChecked(false);
        startTimerBtn.setImageResource(R.drawable.ic_play);
    }

    private void initPickers(View view) {
        hourPicker = view.findViewById(R.id.hour_picker);
        minutePicker = view.findViewById(R.id.minute_picker);
        secondPicker = view.findViewById(R.id.second_picker);

        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(99);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(99);
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(99);

        hourPicker.setOnValueChangedListener((numberPicker, oldVal, newVal) -> {
            pickerChanged();
        });

        minutePicker.setOnValueChangedListener((numberPicker, oldVal, newVal) -> {
            pickerChanged();
        });

        secondPicker.setOnValueChangedListener((numberPicker, oldVal, newVal) -> {
            pickerChanged();
        });
    }

    private void pickerChanged() {
        int hour = hourPicker.getValue();
        int minute = minutePicker.getValue();
        int second = secondPicker.getValue();

        if (hour != 0 || minute != 0 || second != 0) {
            startTimerBtn.setVisibility(View.VISIBLE);
            resetTimerBtn.setVisibility(View.VISIBLE);
            addTimeBtn.setVisibility(View.VISIBLE);
            addTimeUnitTypeSpinner.setVisibility(View.VISIBLE);
            timer.setText(formatTime(hour, minute, second));
            timerTime = getTimerTime();
        } else {
            resetTimer();
        }
    }

    private String formatTime(int hour, int minute, int second) {
        return String.format("%s:%s:%s", Helpers.formatIntegerToTwoDigits(hour), Helpers.formatIntegerToTwoDigits(minute), Helpers.formatIntegerToTwoDigits(second));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings: {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}