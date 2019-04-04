package com.androfocus.location.tracking.andavaruser;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PastAttendanceAdapter extends ArrayAdapter {

    List list = new ArrayList();
    public PastAttendanceAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }


    public void add(PastAttendanceData object) {
        super.add(object);
        list.add(object);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row;
        row = convertView;
        PastAttendanceAdapter.PastAttendanceDataHolder pastAttendanceDataHolder;
        if(row == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.past_attendance_row_layout,parent,false);
            pastAttendanceDataHolder = new PastAttendanceAdapter.PastAttendanceDataHolder();

            pastAttendanceDataHolder.tvDate = (TextView) row.findViewById(R.id.tvDate);
            pastAttendanceDataHolder.tvStartTime = (TextView) row.findViewById(R.id.tvStartTime);
            pastAttendanceDataHolder.tvEndTime = (TextView) row.findViewById(R.id.tvEndTime);
            pastAttendanceDataHolder.tvDuration = (TextView) row.findViewById(R.id.tvDuration);

            row.setTag(pastAttendanceDataHolder);
        }
        else {
            pastAttendanceDataHolder = (PastAttendanceAdapter.PastAttendanceDataHolder) row.getTag();
        }
        PastAttendanceData pastAttendanceData = (PastAttendanceData) this.getItem(position);


        pastAttendanceDataHolder.tvDate.setText(pastAttendanceData.getDate());
        pastAttendanceDataHolder.tvStartTime.setText(pastAttendanceData.getStart_time());
        pastAttendanceDataHolder.tvEndTime.setText(pastAttendanceData.getEnd_time());
        pastAttendanceDataHolder.tvDuration.setText(pastAttendanceData.getDuration());

        return row;
    }
    static class  PastAttendanceDataHolder
    {
        TextView tvDate,tvStartTime,tvEndTime,tvDuration;
    }



}
