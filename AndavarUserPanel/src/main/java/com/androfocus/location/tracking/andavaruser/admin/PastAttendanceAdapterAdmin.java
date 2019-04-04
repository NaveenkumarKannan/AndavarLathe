package com.androfocus.location.tracking.andavaruser.admin;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.androfocus.location.tracking.andavaruser.R;

import java.util.ArrayList;
import java.util.List;

public class PastAttendanceAdapterAdmin extends ArrayAdapter {

    List list = new ArrayList();
    public PastAttendanceAdapterAdmin(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }


    public void add(PastAttendanceDataAdmin object) {
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
        PastAttendanceAdapterAdmin.PastAttendanceDataHolder pastAttendanceDataHolder;
        if(row == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.past_attendance_row_layout_admin,parent,false);
            pastAttendanceDataHolder = new PastAttendanceAdapterAdmin.PastAttendanceDataHolder();

            pastAttendanceDataHolder.tvDate = (TextView) row.findViewById(R.id.tvDate);
            pastAttendanceDataHolder.tvStartTime = (TextView) row.findViewById(R.id.tvStartTime);
            pastAttendanceDataHolder.tvEndTime = (TextView) row.findViewById(R.id.tvEndTime);
            pastAttendanceDataHolder.tvDuration = (TextView) row.findViewById(R.id.tvDuration);

            pastAttendanceDataHolder.tvUserName = (TextView) row.findViewById(R.id.tvUserName);
            pastAttendanceDataHolder.tvLocationStart = (TextView) row.findViewById(R.id.tvLocationStart);
            pastAttendanceDataHolder.tvLocationEnd = (TextView) row.findViewById(R.id.tvLocationEnd);


            row.setTag(pastAttendanceDataHolder);
        }
        else {
            pastAttendanceDataHolder = (PastAttendanceAdapterAdmin.PastAttendanceDataHolder) row.getTag();
        }
        PastAttendanceDataAdmin pastAttendanceDataAdmin = (PastAttendanceDataAdmin) this.getItem(position);


        pastAttendanceDataHolder.tvDate.setText(pastAttendanceDataAdmin.getDate());
        pastAttendanceDataHolder.tvStartTime.setText(pastAttendanceDataAdmin.getStart_time());
        pastAttendanceDataHolder.tvEndTime.setText(pastAttendanceDataAdmin.getEnd_time());
        pastAttendanceDataHolder.tvDuration.setText(pastAttendanceDataAdmin.getDuration());
        
        pastAttendanceDataHolder.tvUserName.setText(pastAttendanceDataAdmin.getUserName());
        pastAttendanceDataHolder.tvLocationStart.setText(pastAttendanceDataAdmin.getLocationStart());
        pastAttendanceDataHolder.tvLocationEnd.setText(pastAttendanceDataAdmin.getLocationEnd());

        return row;
    }
    static class  PastAttendanceDataHolder
    {
        TextView tvDate,tvStartTime,tvEndTime,tvDuration,tvUserName,tvLocationStart,tvLocationEnd;
    }



}
