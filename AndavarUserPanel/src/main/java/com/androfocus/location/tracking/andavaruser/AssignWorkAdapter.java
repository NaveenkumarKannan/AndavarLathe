package com.androfocus.location.tracking.andavaruser;

import android.content.Context;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AssignWorkAdapter extends ArrayAdapter {

    List list = new ArrayList();
    public AssignWorkAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }


    public void add(AssignWorkData object) {
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
        AssignWorkDataHolder assignWorkDataHolder;
        if(row == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.assign_work_row_layout,parent,false);
            assignWorkDataHolder = new AssignWorkDataHolder();

            assignWorkDataHolder.tvFLetter = (TextView) row.findViewById(R.id.tvFLetter);
            assignWorkDataHolder.tvTitle = (TextView) row.findViewById(R.id.tvTitle);
            assignWorkDataHolder.tvDetail = (TextView) row.findViewById(R.id.tvDetail);
            assignWorkDataHolder.tvDeadline = (TextView) row.findViewById(R.id.tvDeadline);
            assignWorkDataHolder.tvCollectionAmt = (TextView) row.findViewById(R.id.tvCollectionAmt);
            assignWorkDataHolder.tvWorkType = (TextView) row.findViewById(R.id.tvWorkType);
            assignWorkDataHolder.llCollectionAmt= (LinearLayout) row.findViewById(R.id.llCollectionAmt);
            assignWorkDataHolder.llAssignWork= (LinearLayout) row.findViewById(R.id.llAssignWork);
            assignWorkDataHolder.tvCompanyName = (TextView) row.findViewById(R.id.tvCompanyName);
            assignWorkDataHolder.tvCompanyAddr = (TextView) row.findViewById(R.id.tvCompanyAddr);
            assignWorkDataHolder.tvWorkId = row.findViewById(R.id.tvWorkId);

//            assignWorkDataHolder.imageView = (ImageView) row.findViewById(R.id.imageView);

            row.setTag(assignWorkDataHolder);
        }
        else {
            assignWorkDataHolder = (AssignWorkDataHolder) row.getTag();
        }
        AssignWorkData assignWorkData = (AssignWorkData) this.getItem(position);


        String workType,workId;
        workType = assignWorkData.getWork_type();
        workId =assignWorkData.getWork_id();
        Log.e("workId",workId);

        assignWorkDataHolder.tvWorkId.setText(workId);
        if(workType.equals("collection")){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                assignWorkDataHolder.llAssignWork.setBackground(getContext().getDrawable(R.drawable.shape2));
            }
            assignWorkDataHolder.llAssignWork.setBackgroundResource(R.drawable.shape2);

            assignWorkDataHolder.llCollectionAmt.setVisibility(View.VISIBLE);
            assignWorkDataHolder.tvFLetter.setText(assignWorkData.getfLetter());
            assignWorkDataHolder.tvTitle.setText(assignWorkData.getWork_title());
            assignWorkDataHolder.tvDetail.setText(assignWorkData.getDetails());
            assignWorkDataHolder.tvDeadline.setText(assignWorkData.getDeadline());

            assignWorkDataHolder.tvWorkType.setText("Collection");
            assignWorkDataHolder.tvCollectionAmt.setText(assignWorkData.getCollection_amt());
        }else if(workType.equals("work")){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                assignWorkDataHolder.llAssignWork.setBackground(getContext().getDrawable(R.drawable.shape));
            }
            assignWorkDataHolder.llAssignWork.setBackgroundResource(R.drawable.shape);

            assignWorkDataHolder.llCollectionAmt.setVisibility(View.GONE);
            assignWorkDataHolder.tvFLetter.setText(assignWorkData.getfLetter());
            assignWorkDataHolder.tvTitle.setText(assignWorkData.getWork_title());
            assignWorkDataHolder.tvDetail.setText(assignWorkData.getDetails());
            assignWorkDataHolder.tvDeadline.setText(assignWorkData.getDeadline());
            assignWorkDataHolder.tvCompanyName.setText(assignWorkData.getCompanyName());
            assignWorkDataHolder.tvCompanyAddr.setText(assignWorkData.getCompanyAddr());
            assignWorkDataHolder.tvWorkType.setText("Work");
        }else {
            assignWorkDataHolder.tvDetail.setText("Work type is different");
        }


        /*
        final int pos=position;
        assignWorkDataHolder.imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {


                switch (view.getId()) {
                    case R.id.imageView:

                        PopupMenu popup = new PopupMenu(getContext(), view);
                        popup.getMenuInflater().inflate(R.menu.assign_menu,
                                popup.getMenu());
                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                switch (item.getItemId()) {
                                    case R.id.assign_convert_to_checkIn:

                                        //Or Some other code you want to put here.. This is just an example.
                                        Toast.makeText(getContext(), " Convert to Check in Clicked at position " + " : " + pos, Toast.LENGTH_LONG).show();

                                        break;
                                    case R.id.assign_mark_as_done:

                                        Toast.makeText(getContext(), "Mark as done Clicked at position " + " : " + pos, Toast.LENGTH_LONG).show();

                                        break;

                                    default:
                                        break;
                                }

                                return true;
                            }
                        });

                        break;

                    default:
                        break;
                }
            }
        });
        */
        return row;
    }
    static class  AssignWorkDataHolder
    {
        TextView tvFLetter, tvTitle, tvDetail, tvDeadline,tvCollectionAmt,tvWorkType,tvCompanyName,tvCompanyAddr,tvWorkId;
        LinearLayout llCollectionAmt,llAssignWork;
    }



}
