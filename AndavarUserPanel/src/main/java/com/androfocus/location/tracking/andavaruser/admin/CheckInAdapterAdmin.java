package com.androfocus.location.tracking.andavaruser.admin;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androfocus.location.tracking.andavaruser.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class CheckInAdapterAdmin extends ArrayAdapter {

    List list = new ArrayList();
    public CheckInAdapterAdmin(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }


    public void add(CheckInDataAdmin object) {
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
        CheckInAdapterAdmin.CheckInDataHolder checkInDataHolder;
        if(row == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.check_in_row_layout_admin,parent,false);
            checkInDataHolder = new CheckInAdapterAdmin.CheckInDataHolder();

            checkInDataHolder.tvCheckInName = (TextView) row.findViewById(R.id.tvCheckInName);
            checkInDataHolder.tvCheckInAddr = (TextView) row.findViewById(R.id.tvCheckInAddr);
            checkInDataHolder.tvCheckInTime = (TextView) row.findViewById(R.id.tvCheckInTime);
            checkInDataHolder.tvCheckInDate = (TextView) row.findViewById(R.id.tvCheckInDate);
            checkInDataHolder.tvVideoName = row.findViewById(R.id.tvVideoName);
            checkInDataHolder.tvVideoUrl=row.findViewById(R.id.tvVideoUrl);
            checkInDataHolder.tvUserName = row.findViewById(R.id.tvUserName);
            checkInDataHolder.ivPhoto = ButterKnife.findById(row,R.id.ivPhoto);

            row.setTag(checkInDataHolder);
        }
        else {
            checkInDataHolder = (CheckInAdapterAdmin.CheckInDataHolder) row.getTag();
        }
        CheckInDataAdmin checkInDataAdmin = (CheckInDataAdmin) this.getItem(position);

        checkInDataHolder.tvCheckInName.setText(checkInDataAdmin.getName());
        checkInDataHolder.tvCheckInAddr.setText(checkInDataAdmin.getLocation());
        checkInDataHolder.tvCheckInTime.setText(checkInDataAdmin.getTime());
        checkInDataHolder.tvCheckInDate.setText(checkInDataAdmin.getDate());
        checkInDataHolder.tvVideoName.setText(checkInDataAdmin.getVideo_name());
        checkInDataHolder.tvVideoUrl.setText(checkInDataAdmin.getVideo_url());
        checkInDataHolder.tvUserName.setText(checkInDataAdmin.getUserName());
        checkInDataHolder.ivPhoto.setImageURI(Uri.parse(checkInDataAdmin.getBitmapPhoto()));
        return row;
    }
    static class  CheckInDataHolder
    {
        TextView tvCheckInName,tvCheckInAddr,tvCheckInTime,tvCheckInDate,tvVideoName,tvVideoUrl,tvUserName ;
        SimpleDraweeView ivPhoto;
    }



}
