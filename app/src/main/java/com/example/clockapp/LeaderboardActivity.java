package com.example.clockapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.clockapp.db.daos.ResultDao;
import com.example.clockapp.db.entities.Result;
import com.example.clockapp.ui.adapters.ResultAdapter;
import com.example.clockapp.ui.alarm.AlarmFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private ResultDao resultDao;
    private ListView resultsListView;
    private List<Result> results;
    private Button clearAllButton;
    private Integer resultCount = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        resultsListView = findViewById(R.id.results_list_view);
        clearAllButton = findViewById(R.id.clear_all_results_btn);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Best results");
        actionBar.setDisplayHomeAsUpEnabled(true);

        resultDao = AlarmFragment.db.resultDao();
        getResults();

        clearAllButton.setOnClickListener(view -> {
            resultDao.deleteAll();
            results.clear();
            ResultAdapter newAdapter = new ResultAdapter(this, R.layout.result_row, results);
            resultsListView.setAdapter(newAdapter);
        });
    }

    private void getResults() {
        results = resultDao.getBestResults(resultCount);
        ResultAdapter adapter = new ResultAdapter(this, R.layout.result_row, results);
        resultsListView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                ArrayList<Integer> items = new ArrayList<>(Arrays.asList(1, 3, 5, 10, 20, 50));
                final ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
                final Spinner spinner = new Spinner(this);
                spinner.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                spinner.setAdapter(adapter);
                spinner.setSelection(items.indexOf(resultCount));

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Results");
                builder.setMessage("How many best results should be displayed?");
                builder.setView(spinner);
                builder.setPositiveButton("Save", (dialogInterface, i) -> {
                    resultCount = Integer.parseInt(spinner.getSelectedItem().toString());
                    getResults();
                });
                builder.setNegativeButton("Cancel", null);
                builder.create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}