package com.androfocus.location.tracking.andavaruser.admin;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.androfocus.location.tracking.andavaruser.R;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsAdapter extends ArrayAdapter {

    List list = new ArrayList();
    public UserDetailsAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }


    public void add(UserDetailsData object) {
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
        UserDetailsAdapter.UserDetailsDataHolder userDetailsDataHolder;
        if(row == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.user_details_row_layout,parent,false);
            userDetailsDataHolder = new UserDetailsAdapter.UserDetailsDataHolder();

            userDetailsDataHolder.tvUserName = (TextView) row.findViewById(R.id.tvUserName);
            userDetailsDataHolder.tvPhNo = (TextView) row.findViewById(R.id.tvPhNo);
            userDetailsDataHolder.tvEmailId = (TextView) row.findViewById(R.id.tvEmailId);
            userDetailsDataHolder.tvUserId = row.findViewById(R.id.tvUserId);
            userDetailsDataHolder.tvEnable = row.findViewById(R.id.tvEnable);

            row.setTag(userDetailsDataHolder);
        }
        else {
            userDetailsDataHolder = (UserDetailsAdapter.UserDetailsDataHolder) row.getTag();
        }
        UserDetailsData userDetailsData = (UserDetailsData) this.getItem(position);


        userDetailsDataHolder.tvUserId.setText(userDetailsData.getUser_id());
        userDetailsDataHolder.tvUserName.setText(userDetailsData.getName());
        userDetailsDataHolder.tvPhNo.setText(userDetailsData.getPhNo());
        userDetailsDataHolder.tvEmailId.setText(userDetailsData.getEmail());

        String enable = userDetailsData.getEnable();
        if(enable.equals("Yes")){
            userDetailsDataHolder.tvEnable.setText("Activated");
        }else {
            userDetailsDataHolder.tvEnable.setText("Deactivated");
        }

        return row;
    }
    static class  UserDetailsDataHolder
    {
        TextView tvUserName,tvPhNo,tvEmailId,tvUserId,tvEnable ;
    }



}
