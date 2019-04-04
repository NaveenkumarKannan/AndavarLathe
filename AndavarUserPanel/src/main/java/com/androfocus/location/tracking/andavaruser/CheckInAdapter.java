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

public class CheckInAdapter extends ArrayAdapter {

    List list = new ArrayList();
    public CheckInAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }


    public void add(CheckInData object) {
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
        CheckInAdapter.CheckInDataHolder checkInDataHolder;
        if(row == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.check_in_row_layout,parent,false);
            checkInDataHolder = new CheckInAdapter.CheckInDataHolder();

            checkInDataHolder.tvCheckInName = (TextView) row.findViewById(R.id.tvCheckInName);
            checkInDataHolder.tvCheckInAddr = (TextView) row.findViewById(R.id.tvCheckInAddr);
            checkInDataHolder.tvCheckInTime = (TextView) row.findViewById(R.id.tvCheckInTime);
            checkInDataHolder.tvCheckInDate = (TextView) row.findViewById(R.id.tvCheckInDate);

            row.setTag(checkInDataHolder);
        }
        else {
            checkInDataHolder = (CheckInAdapter.CheckInDataHolder) row.getTag();
        }
        CheckInData checkInData = (CheckInData) this.getItem(position);


        checkInDataHolder.tvCheckInName.setText(checkInData.getName());
        checkInDataHolder.tvCheckInAddr.setText(checkInData.getLocation());
        checkInDataHolder.tvCheckInTime.setText(checkInData.getTime());
        checkInDataHolder.tvCheckInDate.setText(checkInData.getDate());

        return row;
    }
    static class  CheckInDataHolder
    {
        TextView tvCheckInName,tvCheckInAddr,tvCheckInTime,tvCheckInDate ;
    }



}
