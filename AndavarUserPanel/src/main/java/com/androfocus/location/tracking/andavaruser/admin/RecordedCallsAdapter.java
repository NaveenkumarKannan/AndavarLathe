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

public class RecordedCallsAdapter extends ArrayAdapter {

    List list = new ArrayList();
    public RecordedCallsAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }


    public void add(RecordedCallsData object) {
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
        RecordedCallsAdapter.RecordedCallsDataHolder recordedCallsDataHolder;
        if(row == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.recorded_calls_row_layout_admin,parent,false);
            recordedCallsDataHolder = new RecordedCallsAdapter.RecordedCallsDataHolder();

            recordedCallsDataHolder.tvDate = (TextView) row.findViewById(R.id.tvDate);
            recordedCallsDataHolder.tvUserName = (TextView) row.findViewById(R.id.tvUserName);
            recordedCallsDataHolder.tvCallTime = (TextView) row.findViewById(R.id.tvCallTime);
            recordedCallsDataHolder.tvDuration = (TextView) row.findViewById(R.id.tvDuration);
            recordedCallsDataHolder.tvUrl = (TextView) row.findViewById(R.id.tvUrl);
            recordedCallsDataHolder.tvFileName = row.findViewById(R.id.tvFileName);
            recordedCallsDataHolder.tvLocation = row.findViewById(R.id.tvLocation);

            row.setTag(recordedCallsDataHolder);
        }
        else {
            recordedCallsDataHolder = (RecordedCallsAdapter.RecordedCallsDataHolder) row.getTag();
        }
        RecordedCallsData recordedCallsData = (RecordedCallsData) this.getItem(position);


        recordedCallsDataHolder.tvDate.setText(recordedCallsData.getDate());
        recordedCallsDataHolder.tvUserName.setText(recordedCallsData.getName());
        recordedCallsDataHolder.tvCallTime.setText(recordedCallsData.getTime());
        recordedCallsDataHolder.tvDuration.setText(recordedCallsData.getDuration());
        recordedCallsDataHolder.tvUrl.setText(recordedCallsData.getFile_url());
        recordedCallsDataHolder.tvFileName.setText(recordedCallsData.getFile_name());
        recordedCallsDataHolder.tvLocation.setText(recordedCallsData.getLocation_address());

        return row;
    }
    static class  RecordedCallsDataHolder
    {
        TextView tvDate,tvUserName,tvCallTime,tvDuration,tvUrl,tvFileName,tvLocation;
    }



}

