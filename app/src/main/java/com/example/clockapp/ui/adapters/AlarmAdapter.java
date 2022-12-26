package com.example.clockapp.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.example.clockapp.R;
import com.example.clockapp.db.daos.AlarmDao;
import com.example.clockapp.db.entities.Alarm;
import com.example.clockapp.helpers.AlarmSetter;
import com.example.clockapp.ui.alarm.AlarmFragment;

import java.util.List;

public class AlarmAdapter extends ArrayAdapter {

    private List<Alarm> _list;
    private Context _context;
    private int _resource;
    private boolean isDeleting;
    private AlarmDao alarmDao;

    public AlarmAdapter(@NonNull Context context, int resource, @NonNull List objects, boolean isDeleting) {
        super(context, resource, objects);
        this._list = objects;
        this._context = context;
        this._resource = resource;
        this.isDeleting = isDeleting;
        this.alarmDao = AlarmFragment.db.alarmDao();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(_resource, null);
        Alarm alarm = _list.get(position);
        TextView alarmTime = convertView.findViewById(R.id.alarm_time);
        alarmTime.setText(alarm.getTime());
        TextView alarmTitle = convertView.findViewById(R.id.alarm_title);
        alarmTitle.setText(alarm.getLabel());
        SwitchCompat alarmEnabledSwitch = convertView.findViewById(R.id.alarm_enabled_switch);
        ImageView selectAlarmImageView = convertView.findViewById(R.id.select_alarm_image);
        if (isDeleting) {
            alarmEnabledSwitch.setVisibility(View.GONE);
            selectAlarmImageView.setVisibility(View.VISIBLE);
            fixSelectedImage(selectAlarmImageView, alarm.isSelected());
            selectAlarmImageView.setOnClickListener(view -> {
                alarm.setSelected(!alarm.isSelected());
                fixSelectedImage(selectAlarmImageView, alarm.isSelected());
            });
        } else {
            alarmEnabledSwitch.setVisibility(View.VISIBLE);
            selectAlarmImageView.setVisibility(View.GONE);
            alarmEnabledSwitch.setChecked(alarm.isEnabled());
            alarmEnabledSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
                if (checked) {
                    AlarmSetter.setAlarm(alarm, _context);
                } else {
                    AlarmSetter.cancelAlarm(alarm, _context);
                }
                _list.get(position).setEnabled(checked);
                alarmDao.updateAlarm(_list.get(position));
            });
        }
        return convertView;
    }

    private void fixSelectedImage(ImageView selectedImage, boolean isSelected) {
        if (isSelected) {
            selectedImage.setImageResource(R.drawable.ic_checkbox_checked);
        } else {
            selectedImage.setImageResource(R.drawable.ic_checkbox_disabled);
        }
    }

    @Override
    public int getCount() {
        return _list.size();
    }
}
