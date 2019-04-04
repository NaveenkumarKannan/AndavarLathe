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

public class ExpensesAdapter extends ArrayAdapter {

    List list = new ArrayList();
    public ExpensesAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }


    public void add(ExpensesData object) {
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
        ExpensesAdapter.ExpensesDataHolder expensesDataHolder;
        if(row == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.expenses_row_layout,parent,false);
            expensesDataHolder = new ExpensesAdapter.ExpensesDataHolder();
            
            expensesDataHolder.tvExpensesName = (TextView) row.findViewById(R.id.tvExpensesName);
            expensesDataHolder.tvExpensesDetail = (TextView) row.findViewById(R.id.tvExpensesDetail);
            expensesDataHolder.tvExpensesAmount = (TextView) row.findViewById(R.id.tvExpensesAmount);
            expensesDataHolder.tvExpensesDate = (TextView) row.findViewById(R.id.tvExpensesDate);
            
            row.setTag(expensesDataHolder);
        }
        else {
            expensesDataHolder = (ExpensesAdapter.ExpensesDataHolder) row.getTag();
        }
        ExpensesData expensesData = (ExpensesData) this.getItem(position);


        expensesDataHolder.tvExpensesName.setText(expensesData.getName());
        expensesDataHolder.tvExpensesDetail.setText(expensesData.getDetails());
        expensesDataHolder.tvExpensesAmount.setText("₹ "+expensesData.getAmount());
        expensesDataHolder.tvExpensesDate.setText(expensesData.getDate());

        return row;
    }
    static class  ExpensesDataHolder
    {
        TextView tvExpensesName,tvExpensesDetail,tvExpensesAmount,tvExpensesDate ;
    }



}
