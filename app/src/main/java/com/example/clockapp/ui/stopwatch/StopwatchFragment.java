package com.example.clockapp.ui.stopwatch;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.clockapp.LeaderboardActivity;
import com.example.clockapp.R;
import com.example.clockapp.SettingsActivity;
import com.example.clockapp.db.daos.ResultDao;
import com.example.clockapp.db.entities.Result;
import com.example.clockapp.helpers.Helpers;
import com.example.clockapp.ui.adapters.LapAdapter;
import com.example.clockapp.ui.alarm.AlarmFragment;
import com.example.clockapp.ui.alarm.LapEntry;

import java.util.ArrayList;
import java.util.List;

public class StopwatchFragment extends Fragment {

    private StopwatchViewModel mViewModel;
    private TextView timeTextView;
    private ImageButton playButton;
    private ImageView restartButton;
    private ImageView lapButton;
    private TextView lapTimeLabel;
    private TextView totalTimeLabel;
    private ListView lapsListView;
    private boolean isRunning = false;
    private long startTime = 0;
    private int interval = 50;
    private long timeOffset = 0;
    private long currentTime;
    private List<LapEntry> laps = new ArrayList<LapEntry>();
    private ResultDao resultDao;

    public static StopwatchFragment newInstance() {
        return new StopwatchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stopwatch, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(StopwatchViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        timeTextView = view.findViewById(R.id.time_text_view);
        playButton = view.findViewById(R.id.play_button);
        restartButton = view.findViewById(R.id.restart_button);
        lapButton = view.findViewById(R.id.lap_button);
        lapsListView = view.findViewById(R.id.laps_list_view);
        lapTimeLabel = view.findViewById(R.id.lap_time_label);
        totalTimeLabel = view.findViewById(R.id.total_time_label);

        resultDao = AlarmFragment.db.resultDao();

        playButton.setOnClickListener(innerView -> {
            if (isRunning) {
                stopTimer();
            } else {
                startTimer();
            }
        });

        restartButton.setOnClickListener(innerView -> {
            resetTimer();
        });

        lapButton.setOnClickListener(innerView -> {
            if (isRunning) {
                makeALap();
            } else {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, String.format("My time is %s!", timeTextView.getText()));
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
            }
        });
    }

    private void startTimer() {
        playButton.setImageResource(R.drawable.ic_pause);
        restartButton.setVisibility(View.GONE);
        lapButton.setVisibility(View.VISIBLE);
        lapButton.setImageResource(R.drawable.ic_flag);
        isRunning = true;
        startTime = System.currentTimeMillis();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long time = timeOffset + getDiff();
                timeTextView.setText(Helpers.convertMillisToTime(time, true));
                currentTime = time;
                if (laps.size() > 0) {
                    updateLastLap(time);
                }

                if (laps.size() > 1) {
                    findFastestAndSlowestLap();
                }

                if (isRunning) {
                    handler.postDelayed(this, interval);
                } else {
                    timeOffset = time;
                }
            }
        }, interval);
    }

    private void stopTimer() {
        playButton.setImageResource(R.drawable.ic_play);
        lapButton.setImageResource(R.drawable.ic_share);
        restartButton.setVisibility(View.VISIBLE);
        isRunning = false;
    }

    private void resetTimer() {
        saveResult();
        timeTextView.setText(getResources().getString(R.string.time_default_label));
        restartButton.setVisibility(View.GONE);
        lapButton.setVisibility(View.GONE);
        lapTimeLabel.setVisibility(View.GONE);
        totalTimeLabel.setVisibility(View.GONE);
        startTime = 0;
        timeOffset = 0;
        laps.clear();
        updateLaps();
    }

    private void saveResult() {
        Result result = new Result(currentTime, System.currentTimeMillis());
        long id = resultDao.insertResult(result);
        result.setUid(id);
    }

    private void updateLastLap(long totalTime) {
        long lapTime = totalTime - laps.get(1).getTotalTime();
        laps.get(0).setLapTime(lapTime);
        laps.get(0).setTotalTime(totalTime);
        updateLaps();
    }

    private long getDiff() {
        return System.currentTimeMillis() - startTime;
    }

    private void makeALap() {
        int id = laps.size() == 0 ? 1 : laps.get(0).getId() + 1;
        long totalTime = timeOffset + getDiff();
        long lapTime = laps.size() == 0 ? totalTime : totalTime - laps.get(0).getTotalTime();
        if (laps.size() == 0) {
            laps.add(0, new LapEntry(id, lapTime, totalTime));
            id += 1;
            lapTimeLabel.setVisibility(View.VISIBLE);
            totalTimeLabel.setVisibility(View.VISIBLE);
        }
        laps.add(0, new LapEntry(id, 0, 0));
        updateLaps();
    }

    private void updateLaps() {
        LapAdapter adapter = new LapAdapter(
                getActivity(),
                R.layout.lap_row,
                laps
        );
        lapsListView.setAdapter(adapter);
    }

    private void findFastestAndSlowestLap() {
        long minLapMs = laps.get(0).getLapTime();
        long maxLapMs = laps.get(0).getLapTime();
        for(LapEntry lap: laps) {
            lap.setLongest(false);
            lap.setShortest(false);
            if (lap.getLapTime() < minLapMs) {
                minLapMs = lap.getLapTime();
            }
            if (lap.getLapTime() > maxLapMs) {
                maxLapMs = lap.getLapTime();
            }
        }
        final long minLap = minLapMs;
        final long maxLap = maxLapMs;
        LapEntry shortestLap = laps.stream().filter(lap -> lap.getLapTime() == minLap).findFirst().get();
        shortestLap.setShortest(true);
        LapEntry longestLap = laps.stream().filter(lap -> lap.getLapTime() == maxLap).findFirst().get();
        longestLap.setLongest(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_leaderboard);
        item.setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings: {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.action_leaderboard: {
                Intent intent = new Intent(getActivity(), LeaderboardActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}