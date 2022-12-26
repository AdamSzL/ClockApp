package com.example.clockapp.ui.alarm;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlarmManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.clockapp.R;
import com.example.clockapp.SetAlarmActivity;
import com.example.clockapp.SettingsActivity;
import com.example.clockapp.db.AppDatabase;
import com.example.clockapp.db.daos.AlarmDao;
import com.example.clockapp.db.entities.Alarm;
import com.example.clockapp.ui.adapters.AlarmAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AlarmFragment extends Fragment {

    private AlarmViewModel mViewModel;
    private ListView alarmsListView;
    private FloatingActionButton fabSetAlarm;
    private Spinner filterTypeSpinner;
    private EditText filterTermInput;
    private LinearLayout sortRow;
    private LinearLayout sortTimeContainer;
    private LinearLayout sortTitleContainer;
    private LinearLayout sortEnabledContainer;
    private ImageView sortTimeIcon;
    private ImageView sortTitleIcon;
    private ImageView sortEnabledIcon;
    private TextView sortTimeLabel;
    private TextView sortTitleLabel;
    private TextView sortEnabledLabel;
    private boolean isDeleting = false;
    private boolean areAllSelected = false;
    private boolean filterRowVisible = false;
    public static AppDatabase db;
    public static AlarmDao alarmDao;
    private List<Alarm> alarms;
    private String selectedFilterType = "time";
    private boolean sortRowVisible = false;
    private String sortBy = "time";
    private boolean sortAsc = true;

    public static AlarmFragment newInstance() {
        return new AlarmFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alarm, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        fabSetAlarm = view.findViewById(R.id.fab_set_alarm);
        alarmsListView = view.findViewById(R.id.alarms_list_view);
        filterTypeSpinner = view.findViewById(R.id.filter_type_spinner);
        filterTermInput = view.findViewById(R.id.filter_term);
        sortRow = view.findViewById(R.id.sort_row);
        sortTimeContainer = view.findViewById(R.id.sort_time_container);
        sortTitleContainer = view.findViewById(R.id.sort_title_container);
        sortEnabledContainer = view.findViewById(R.id.sort_enabled_container);
        sortTimeIcon = view.findViewById(R.id.sort_time_icon);
        sortTitleIcon = view.findViewById(R.id.sort_title_icon);
        sortEnabledIcon = view.findViewById(R.id.sort_enabled_icon);
        sortTimeLabel = view.findViewById(R.id.sort_time_label);
        sortTitleLabel = view.findViewById(R.id.sort_title_label);
        sortEnabledLabel = view.findViewById(R.id.sort_enabled_label);

        handleFab();
        handleAlarmFiltering();
        handleAlarmSorting();

        db = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, "clock").fallbackToDestructiveMigration().allowMainThreadQueries().build();
        alarmDao = db.alarmDao();
        getAlarms();
    }

    private void handleFab() {
        fabSetAlarm.setOnClickListener(innerView -> {
            if (isDeleting) {
                List<Long> selectedAlarmIds = alarms.stream().filter(Alarm::isSelected).map(alarm -> alarm.uid).collect(Collectors.toList());
                for (Long id: selectedAlarmIds) {
                    alarms.removeIf(alarm -> alarm.uid == id);
                }
                alarmDao.deleteAlarms(selectedAlarmIds);
                toggleDeleteMode();
                alarmsChanged();
            } else {
                Intent intent = new Intent(getActivity(), SetAlarmActivity.class);
                intent.putExtra("mode", "insert");
                startActivity(intent);
            }
        });
    }

    private void handleAlarmFiltering() {
        ArrayList<String> filterTypes = new ArrayList<>(Arrays.asList("time", "title", "enabled"));
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, filterTypes);
        filterTypeSpinner.setAdapter(spinnerAdapter);

        filterTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String type = filterTypes.get(i);
                selectedFilterType = type;
                if (type.equals("time")) {
                    filterTermInput.setVisibility(View.VISIBLE);
                    filterTermInput.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
                    filterTermInput.setHint(R.string.filter_term_time_hint);
                    filterTermInput.setText("");
                } else if (type.equals("title")) {
                    filterTermInput.setVisibility(View.VISIBLE);
                    filterTermInput.setInputType(InputType.TYPE_CLASS_TEXT);
                    filterTermInput.setHint(R.string.filter_term_title_hint);
                    filterTermInput.setText("");
                } else if (type.equals("enabled")) {
                    alarmsChanged();
                    filterTermInput.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        filterTermInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                alarmsChanged();
            }
        });
    }

    private void handleAlarmSorting() {
        sortTimeContainer.setOnClickListener(view -> {
            sortingTypeChanged("time", sortTimeIcon, sortTimeLabel);
        });

        sortTitleContainer.setOnClickListener(view -> {
            sortingTypeChanged("title", sortTitleIcon, sortTitleLabel);
        });

        sortEnabledContainer.setOnClickListener(view -> {
            sortingTypeChanged("enabled", sortEnabledIcon, sortEnabledLabel);
        });
    }

    private void resetSortingOptions() {
        sortTimeIcon.setImageResource(R.drawable.ic_sort_down);
        sortTitleIcon.setImageResource(R.drawable.ic_sort_down);
        sortEnabledIcon.setImageResource(R.drawable.ic_sort_down);
        sortEnabledLabel.setTextColor(ContextCompat.getColor(getActivity(), R.color.dark_gray));
        sortTitleLabel.setTextColor(ContextCompat.getColor(getActivity(), R.color.dark_gray));
        sortTimeLabel.setTextColor(ContextCompat.getColor(getActivity(), R.color.dark_gray));
    }

    private void setSelectedOptionTextColor(TextView label) {
        int nightModeFlags =
                getContext().getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                label.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                label.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
                break;
        }
    }

    private void sortingTypeChanged(String type, ImageView icon, TextView label) {
        if (sortBy.equals(type)) {
            sortAsc = !sortAsc;
            icon.setImageResource(sortAsc ? R.drawable.ic_sort_down : R.drawable.ic_sort_up);
        } else {
            sortBy = type;
            sortAsc = true;
            resetSortingOptions();
            sortTimeIcon.setImageResource(R.drawable.ic_sort_down);
            setSelectedOptionTextColor(label);
        }
        alarmsChanged();
    }

    private void handleSortingRowVisibility() {
        if (alarms.size() >= 1) {
            sortRow.setVisibility(View.VISIBLE);
        } else {
            sortRow.setVisibility(View.GONE);
        }
    }

    private void getAlarms() {
        alarms = alarmDao.getAll();
        handleSortingRowVisibility();
        AlarmAdapter alarmAdapter = new AlarmAdapter(getActivity(), R.layout.alarm_row, sortAlarms(alarms), isDeleting);
        alarmsListView.setAdapter(alarmAdapter);

        alarmsListView.setOnItemClickListener((adapterView, view, position, l) -> {
            Alarm alarm = alarms.get(position);
            if (isDeleting) {
                alarm.setSelected(!alarm.isSelected());
                alarmsChanged();
            } else {
                Intent intent = new Intent(getActivity(), SetAlarmActivity.class);
                intent.putExtra("mode", "update");
                intent.putExtra("alarm", alarms.get(position));
                startActivity(intent);
            }
        });

        alarmsListView.setOnItemLongClickListener((adapterView, view, position, l) -> {
            Alarm alarm = alarms.get(position);
            if (isDeleting) {
                alarm.setSelected(true);
            } else {
                alarm.setSelected(true);
                toggleDeleteMode();
            }
            alarmsChanged();
            return true;
        });
    }

    private void alarmsChanged() {
        handleSortingRowVisibility();
        if (filterRowVisible) {
            String filterTerm = filterTermInput.getText().toString();
            List<Alarm> filteredAlarms = alarms;
            if (selectedFilterType.equals("time")) {
                filteredAlarms = alarms.stream().filter(alarm -> alarm.time.contains(filterTerm)).collect(Collectors.toList());
            } else if (selectedFilterType.equals("title")) {
                filteredAlarms = alarms.stream().filter(alarm -> alarm.label.toLowerCase().contains(filterTerm.toLowerCase())).collect(Collectors.toList());
            } else if (selectedFilterType.equals("enabled")) {
                filteredAlarms = alarms.stream().filter(Alarm::isEnabled).collect(Collectors.toList());
            }
            AlarmAdapter alarmAdapter = new AlarmAdapter(getActivity(), R.layout.alarm_row, sortAlarms(filteredAlarms), isDeleting);
            alarmsListView.setAdapter(alarmAdapter);
        } else {
            AlarmAdapter alarmAdapter = new AlarmAdapter(getActivity(), R.layout.alarm_row, sortAlarms(alarms), isDeleting);
            alarmsListView.setAdapter(alarmAdapter);
        }
    }

    private List<Alarm> sortAlarms(List<Alarm> alarms) {
        if (sortBy.equals("time")) {
            alarms.sort(Comparator.comparing(Alarm::getTime));
        } else if (sortBy.equals("title")) {
            alarms.sort(Comparator.comparing(Alarm::getLabel));
        } else if (sortBy.equals("enabled")) {
            alarms.sort(Comparator.comparing(Alarm::isEnabled));
        }
        if ((!sortAsc && !sortBy.equals("enabled")) || (sortAsc && sortBy.equals("enabled"))) {
            Collections.reverse(alarms);
        }
        return alarms;
    }

    @Override
    public void onResume() {
        filterRowVisible = false;
        alarms = alarmDao.getAll();
        alarmsChanged();
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:  {
                int targetVisibility;
                if (filterRowVisible) {
                    filterRowVisible = false;
                    alarmsChanged();
                    targetVisibility = View.GONE;
                } else {
                    filterRowVisible = true;
                    targetVisibility = View.VISIBLE;
                    filterTypeSpinner.setSelection(0);
                }
                filterTermInput.setVisibility(targetVisibility);
                filterTypeSpinner.setVisibility(targetVisibility);
                return true;
            }
            case R.id.action_settings: {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.action_delete_all: {
                boolean targetValue;
                if (areAllSelected) {
                    item.setIcon(R.drawable.ic_checkbox_disabled);
                    targetValue = false;
                    areAllSelected = false;
                } else {
                    item.setIcon(R.drawable.ic_checkbox_checked);
                    targetValue = true;
                    areAllSelected = true;
                }

                for (Alarm alarm: alarms) {
                    alarm.setSelected(targetValue);
                }
                alarmsChanged();
                return true;
            }
            case R.id.action_exit_delete_mode:
                toggleDeleteMode();
                for (Alarm alarm: alarms) {
                    alarm.setSelected(false);
                }
                alarmsChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (isDeleting) {
            MenuItem settings = menu.findItem(R.id.action_settings);
            settings.setVisible(false);
            MenuItem exitDelMode = menu.findItem(R.id.action_exit_delete_mode);
            exitDelMode.setVisible(true);
            MenuItem deleteAll = menu.findItem(R.id.action_delete_all);
            deleteAll.setVisible(true);
        } else {
            MenuItem item = menu.findItem(R.id.action_search);
            item.setVisible(true);
        }
    }

    public void toggleDeleteMode() {
        isDeleting = !isDeleting;
        if (isDeleting) {
            fabSetAlarm.setImageResource(R.drawable.ic_delete);
        } else {
            fabSetAlarm.setImageResource(R.drawable.ic_add);
        }
        requireActivity().invalidateOptionsMenu();
    }
}