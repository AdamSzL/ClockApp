package com.example.clockapp.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.clockapp.R;
import com.example.clockapp.db.entities.Result;
import com.example.clockapp.helpers.Helpers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ResultAdapter extends ArrayAdapter {

    private List<Result> _list;
    private Context _context;
    private int _resource;

    public ResultAdapter(@NonNull Context context, int resource, @NonNull List objects) {
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
        Result result = _list.get(position);
        TextView timeTextView = convertView.findViewById(R.id.time_text_view);
        TextView resultTextView = convertView.findViewById(R.id.result_text_view);
        Date date = new Date(result.getTimestamp());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String strDate = dateFormat.format(date);
        timeTextView.setText(strDate);
        resultTextView.setText(Helpers.convertMillisToTime(result.getResult(), true));
        return convertView;
    }

    @Override
    public int getCount() {
        return _list.size();
    }
}
