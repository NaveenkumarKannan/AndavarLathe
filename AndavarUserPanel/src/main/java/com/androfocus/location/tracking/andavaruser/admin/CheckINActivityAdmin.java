package com.androfocus.location.tracking.andavaruser.admin;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.androfocus.location.tracking.andavaruser.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.ButterKnife;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class CheckINActivityAdmin extends AppCompatActivity {
    CheckInAdapterAdmin checkInAdapterAdmin;
    ListView listView;
    String type;
    ProgressDialog loading;

    CheckInAdapterAdmin searchCancelAdapter;
    AutoCompleteTextView suggestion_box;
    private List<String> listName;
    String searchKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_admin);


        listView = (ListView) findViewById(R.id.lvCheckIn);
        checkInAdapterAdmin = new CheckInAdapterAdmin(this, R.layout.check_in_row_layout_admin);
        searchCancelAdapter = new CheckInAdapterAdmin(this, R.layout.check_in_row_layout_admin);
        loading = ProgressDialog.show(this, "Fetching Data...","Please Wait...",true,true);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                type = "getCheckIn";
                BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                backgroundWorker.execute();
            }
        });

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int pos=i;
                final TextView tvVideoUrl,tvVideoName;
                tvVideoUrl = (TextView) view.findViewById(R.id.tvVideoUrl);
                tvVideoName = view.findViewById(R.id.tvVideoName);
                final String url,fileName;
                url = tvVideoUrl.getText().toString();
                fileName = tvVideoName.getText().toString();

                JzvdStd jzvdStd = (JzvdStd) findViewById(R.id.videoplayer);
                jzvdStd.setVisibility(View.VISIBLE);
                jzvdStd.setUp(url,
                        fileName, Jzvd.SCREEN_WINDOW_NORMAL);
                jzvdStd.startVideo();
                //jzvdStd.thumbImageView.setImage("http://p.qpic.cn/videoyun/0/2449_43b6f696980311e59ed467f22794e792_1/640");
            }
        });
        suggestion_box = findViewById(R.id.suggestion_box);
        listName = new ArrayList<>();
        suggestion_box.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                searchKey = adapterView.getItemAtPosition(i).toString();

                //loading = ProgressDialog.show(SearchMember.this, "Fetching Data...","Please Wait...",true,true);

                Log.e("searchKey",searchKey );
                type = "Select";
                BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                backgroundWorker.execute();
            }
        });

        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loading.dismiss();
    }

    @Override
    public void onBackPressed() {
        finish();
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }
    public void goBack(View view) {
        finish();
    }
    public void cancelSearch(View view) {
        suggestion_box.clearListSelection();
        suggestion_box.setText("",null );
        //suggestion_box.setHint("Type to search");
        checkInAdapterAdmin.list.clear();
        checkInAdapterAdmin.list = searchCancelAdapter.list;
        listView.setAdapter(checkInAdapterAdmin);
        checkInAdapterAdmin.notifyDataSetChanged();
    }
    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }
    public class BackgroundWorkerJson extends AsyncTask<String,Void,String> {
        Context context;
        //ProgressDialog loading;
        String json_string;
        JSONArray jsonArray;
        JSONObject jsonObject;

        @Override
        protected String doInBackground(String... params) {
            try {
                String post_data = null;
                String expenseUrl = null;
                if(type.equals("getCheckIn")){
                    expenseUrl = "http://andavarinfo.com/AandavarLathe/Admin/getCheckIn.php";
                    Log.w(type,type );

                    post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode("some data", "UTF-8")
                            +"&"+ URLEncoder.encode("searchKey", "UTF-8") + "=" + URLEncoder.encode("All", "UTF-8")
                    ;
                }
                if(type.equals("Select")){
                    expenseUrl = "http://andavarinfo.com/AandavarLathe/Admin/getCheckIn.php";
                    Log.w(type,type );

                    post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode("some data", "UTF-8")
                            +"&"+ URLEncoder.encode("searchKey", "UTF-8") + "=" + URLEncoder.encode(searchKey, "UTF-8")
                    ;
                }
                URL url = new URL(expenseUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //loading = ProgressDialog.show(getContext(), "Fetching CheckInDataAdmin...","Please Wait...",true,true);
        }

        @Override
        protected void onPostExecute(String result) {

            if(type.equals("getCheckIn")){
                json_string = result;
                //      Log.e("Image JSON", json_string);
                if(json_string == null){
                    //Toast.makeText(getContext(),"First Get JSON checkInData",Toast.LENGTH_LONG).show();
                }
                else {
                    try {
                        jsonObject = new JSONObject(json_string);
                        jsonArray = jsonObject.getJSONArray("getCheckIn");

                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jo = jsonArray.getJSONObject(i);
                            String name,location,time,date,video_name,video_url,user_name,bitmapPhotoString;

                            Bitmap bitmapPhoto;
                            name =  jo.getString("place_work_name");
                            location = jo.getString("location_address");
                            time = jo.getString("check_in_time");
                            date = jo.getString("check_in_date");
                            video_name = jo.getString("video_name");
                            video_url = jo.getString("video_url");
                            user_name = jo.getString("user_name");
                            bitmapPhotoString = jo.getString("photo");

                            //byte[] decodedString = Base64.decode(bitmapPhotoString, Base64.DEFAULT);
                            //bitmapPhoto = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                            CheckInDataAdmin checkInDataAdmin = new CheckInDataAdmin( name,location,time,date,video_name,video_url,user_name,bitmapPhotoString);
                            checkInAdapterAdmin.add(checkInDataAdmin);
                            listName.add(user_name);
                            searchCancelAdapter.add(checkInDataAdmin);
                        }
                        loading.dismiss();
                        listView.setAdapter(checkInAdapterAdmin);

                        suggestion_box.setVisibility(View.VISIBLE);
                        ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>(
                                CheckINActivityAdmin.this,
                                android.R.layout.simple_spinner_dropdown_item,
                                listName);
                        suggestion_box.setAdapter(arrayAdapter1);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

            if(type.equals("Select")){
                json_string = result;
                //      Log.e("Image JSON", json_string);
                if(json_string == null){
                    //Toast.makeText(getContext(),"First Get JSON checkInData",Toast.LENGTH_LONG).show();
                }
                else {
                    try {
                        checkInAdapterAdmin.list.clear();
                        jsonObject = new JSONObject(json_string);
                        jsonArray = jsonObject.getJSONArray("getCheckIn");

                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jo = jsonArray.getJSONObject(i);
                            String name,location,time,date,video_name,video_url,user_name,bitmapPhotoString;

                            //Bitmap bitmapPhoto;
                            name =  jo.getString("place_work_name");
                            location = jo.getString("location_address");
                            time = jo.getString("check_in_time");
                            date = jo.getString("check_in_date");
                            video_name = jo.getString("video_name");
                            video_url = jo.getString("video_url");
                            user_name = jo.getString("user_name");
                            bitmapPhotoString = jo.getString("photo");

                            //byte[] decodedString = Base64.decode(bitmapPhotoString, Base64.DEFAULT);
                            //bitmapPhoto = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                            CheckInDataAdmin checkInDataAdmin = new CheckInDataAdmin( name,location,time,date,video_name,video_url,user_name,bitmapPhotoString);
                            checkInAdapterAdmin.add(checkInDataAdmin);
                        }
                        loading.dismiss();
                        listView.setAdapter(checkInAdapterAdmin);

                        checkInAdapterAdmin.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

    }
}
