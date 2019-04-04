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

public class TrackedLocationsAdapter extends ArrayAdapter {

    List list = new ArrayList();
    public TrackedLocationsAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }


    public void add(TrackedLocationsData object) {
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
        TrackedLocationsAdapter.TrackedLocationsDataHolder trackedLocationsDataHolder;
        if(row == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.tracked_locations_row_layout_admin,parent,false);
            trackedLocationsDataHolder = new TrackedLocationsAdapter.TrackedLocationsDataHolder();

            trackedLocationsDataHolder.tvDate = (TextView) row.findViewById(R.id.tvDate);
            trackedLocationsDataHolder.tvUserName = (TextView) row.findViewById(R.id.tvUserName);
            trackedLocationsDataHolder.tvTime = (TextView) row.findViewById(R.id.tvTime);
            trackedLocationsDataHolder.tvLocationAddr = (TextView) row.findViewById(R.id.tvLocationAddr);

            row.setTag(trackedLocationsDataHolder);
        }
        else {
            trackedLocationsDataHolder = (TrackedLocationsAdapter.TrackedLocationsDataHolder) row.getTag();
        }
        TrackedLocationsData trackedLocationsData = (TrackedLocationsData) this.getItem(position);

        trackedLocationsDataHolder.tvDate.setText(trackedLocationsData.getDate());
        trackedLocationsDataHolder.tvUserName.setText(trackedLocationsData.getUser_name());
        trackedLocationsDataHolder.tvTime.setText(trackedLocationsData.getTime());
        trackedLocationsDataHolder.tvLocationAddr.setText(trackedLocationsData.getLocation_address());

        return row;
    }
    static class  TrackedLocationsDataHolder
    {
        TextView tvDate,tvUserName,tvTime,tvLocationAddr;
    }



}

