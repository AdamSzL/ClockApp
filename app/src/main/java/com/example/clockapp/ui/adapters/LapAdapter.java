package com.example.clockapp.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.clockapp.R;
import com.example.clockapp.helpers.Helpers;
import com.example.clockapp.ui.alarm.LapEntry;

import java.util.List;

public class LapAdapter extends ArrayAdapter {
    private List<LapEntry> _list;
    private Context _context;
    private int _resource;

    public LapAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
        this._list = objects;
        this._context = context;
        this._resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(_resource, null);
        TextView idTv = convertView.findViewById(R.id.lap_id);
        TextView lapTimeTv = convertView.findViewById(R.id.lap_time);
        TextView totalTimeTv = convertView.findViewById(R.id.total_time);
        LapEntry lap = _list.get(position);
        idTv.setText(String.valueOf(lap.getId()));
        lapTimeTv.setText(Helpers.convertMillisToTime(lap.getLapTime(), true));
        totalTimeTv.setText(Helpers.convertMillisToTime(lap.getTotalTime(), true));
        if (lap.isLongest()) {
            lapTimeTv.setTextColor(ContextCompat.getColor(_context, R.color.red));
        } else if (lap.isShortest()) {
            lapTimeTv.setTextColor(ContextCompat.getColor(_context, R.color.green));
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return _list.size();
    }
}
