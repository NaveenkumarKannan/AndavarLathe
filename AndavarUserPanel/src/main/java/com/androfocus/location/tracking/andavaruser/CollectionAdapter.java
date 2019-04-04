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

public class CollectionAdapter extends ArrayAdapter {

    List list = new ArrayList();
    public CollectionAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }


    public void add(CollectionData object) {
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
        CollectionAdapter.CollectionDataHolder collectionDataHolder;
        if(row == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.collection_row_layout,parent,false);
            collectionDataHolder = new CollectionAdapter.CollectionDataHolder();

            collectionDataHolder.tvExpensesName = (TextView) row.findViewById(R.id.tvExpensesName);
            collectionDataHolder.tvExpensesDetail = (TextView) row.findViewById(R.id.tvExpensesDetail);
            collectionDataHolder.tvExpensesAmount = (TextView) row.findViewById(R.id.tvExpensesAmount);
            collectionDataHolder.tvExpensesDate = (TextView) row.findViewById(R.id.tvExpensesDate);

            row.setTag(collectionDataHolder);
        }
        else {
            collectionDataHolder = (CollectionAdapter.CollectionDataHolder) row.getTag();
        }
        CollectionData collectionData = (CollectionData) this.getItem(position);


        collectionDataHolder.tvExpensesName.setText(collectionData.getName());
        collectionDataHolder.tvExpensesDetail.setText(collectionData.getDetails());
        collectionDataHolder.tvExpensesAmount.setText("₹ "+collectionData.getAmount());
        collectionDataHolder.tvExpensesDate.setText(collectionData.getDate());

        return row;
    }
    static class  CollectionDataHolder
    {
        TextView tvExpensesName,tvExpensesDetail,tvExpensesAmount,tvExpensesDate ;
    }



}
