package com.androfocus.location.tracking.andavaruser.admin;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.androfocus.location.tracking.andavaruser.MainActivity;
import com.androfocus.location.tracking.andavaruser.R;
import com.androfocus.location.tracking.andavaruser.SessionManager;

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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TrackedLocationsActivity extends AppCompatActivity {
    TrackedLocationsAdapter trackedLocationsAdapter;
    ListView listView;
    String type;
    ProgressDialog loading;
    TrackedLocationsAdapter searchCancelAdapter;
    AutoCompleteTextView suggestion_box;
    private List<String> listName;
    String searchKey;

    String user_name,dateArr[],time[],date;
    double[] lat,lon;
    int length;
    TextView tvDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracked_locations);

        tvDate = findViewById(R.id.tvDate);
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c);
        tvDate.setText(formattedDate);
        Log.e("Date:d-m-y",formattedDate);
        df = new SimpleDateFormat("yyyy-MM-dd");
        date = df.format(c);
        Log.e("Date:y-m-d",date);

        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int day, month, year;
                Calendar calendar = Calendar.getInstance();
                day = calendar.get(Calendar.DAY_OF_MONTH);
                month = calendar.get(Calendar.MONTH);
                year = calendar.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(TrackedLocationsActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int iYear, int iMonth, int iDay) {
                        NumberFormat numberFormat = new DecimalFormat("00");
                        String day,month,year;
                        day = String.valueOf(numberFormat.format(iDay));
                        month = String.valueOf(numberFormat.format(iMonth+1));
                        year = String.valueOf(iYear);
                        date = day+"-"+month+"-"+year;
                        tvDate.setText(date);
                        date = year+"-"+month+"-"+day;

                        trackedLocationsAdapter.list.clear();
                        searchCancelAdapter.list.clear();

                        loading = ProgressDialog.show(TrackedLocationsActivity.this, "Fetching Data...","Please Wait...",true,true);
                        type = "getTrackedLocations";
                        BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                        backgroundWorker.execute();
                    }
                },year,month,day);
                datePickerDialog.show();

            }
        });

        listView = (ListView) findViewById(R.id.lvTrackedLocations);
        trackedLocationsAdapter = new TrackedLocationsAdapter(this, R.layout.tracked_locations_row_layout_admin);
        searchCancelAdapter = new TrackedLocationsAdapter(this, R.layout.tracked_locations_row_layout_admin);
        loading = ProgressDialog.show(this, "Fetching Data...","Please Wait...",true,true);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                type = "getTrackedLocations";
                BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                backgroundWorker.execute();
            }
        });

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        
        suggestion_box = findViewById(R.id.suggestion_box);
        listName = new ArrayList<>();
        suggestion_box.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                searchKey = adapterView.getItemAtPosition(i).toString();

                loading = ProgressDialog.show(TrackedLocationsActivity.this, "Fetching Data...","Please Wait...",true,true);

                Log.e("searchKey",searchKey );
                type = "Select";
                BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                backgroundWorker.execute();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Log.e("listView","pos"+i );

                TrackedLocationsData trackedLocationsData = (TrackedLocationsData) trackedLocationsAdapter.list.get(i);
                String date,time;
                Double lattitude,longitude;
                user_name = trackedLocationsData.getUser_name();
                lattitude = Double.valueOf(trackedLocationsData.getLattitude());
                longitude = Double.valueOf(trackedLocationsData.getLongitude());
                date = trackedLocationsData.getDate();
                time = trackedLocationsData.getTime();

                loading = ProgressDialog.show(TrackedLocationsActivity.this, "Fetching Data...","Please Wait...",true,true);
                type = "getMapLocations";
                BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                backgroundWorker.execute();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        loading.dismiss();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
    public void goBack(View view) {
        finish();
    }
    public void cancelSearch(View view) {
        suggestion_box.clearListSelection();
        suggestion_box.setText("",null );
        //suggestion_box.setHint("Type to search");
        trackedLocationsAdapter.list.clear();
        trackedLocationsAdapter.list = searchCancelAdapter.list;
        listView.setAdapter(trackedLocationsAdapter);
        trackedLocationsAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    public class BackgroundWorkerJson extends AsyncTask<String,Void,String> {
        //ProgressDialog loading;
        String json_string;
        JSONArray jsonArray;
        JSONObject jsonObject;

        @Override
        protected String doInBackground(String... params) {
            try {
                SessionManager session;
                session = new SessionManager(TrackedLocationsActivity.this);
                //Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();

                HashMap<String, String> user = session.getUserDetails();
                String userId = user.get(SessionManager.KEY_NAME);

                String post_data = null;
                String expenseUrl = null;
                if(type.equals("getTrackedLocations")){
                    expenseUrl = "http://andavarinfo.com/AandavarLathe/Admin/getTrackedLocations.php";
                    Log.e(type,type );

                    post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
                            +"&"+ URLEncoder.encode("searchKey", "UTF-8") + "=" + URLEncoder.encode("All", "UTF-8")
                            +"&"+ URLEncoder.encode("date", "UTF-8") + "=" + URLEncoder.encode(date, "UTF-8")
                    ;
                }
                if(type.equals("Select")){
                    expenseUrl = "http://andavarinfo.com/AandavarLathe/Admin/getTrackedLocations.php";
                    Log.e(type,type );

                    post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
                            +"&"+ URLEncoder.encode("searchKey", "UTF-8") + "=" + URLEncoder.encode(searchKey, "UTF-8")
                            +"&"+ URLEncoder.encode("date", "UTF-8") + "=" + URLEncoder.encode(date, "UTF-8")
                    ;
                }
                if(type.equals("getMapLocations")){
                    expenseUrl = "http://andavarinfo.com/AandavarLathe/Admin/getMapLocations.php";
                    Log.e(type,type );

                    post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(user_name, "UTF-8")
                            +"&"+ URLEncoder.encode("searchKey", "UTF-8") + "=" + URLEncoder.encode("All", "UTF-8")
                            +"&"+ URLEncoder.encode("date", "UTF-8") + "=" + URLEncoder.encode(date, "UTF-8")
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
            //loading = ProgressDialog.show(getContext(), "Fetching TrackedLocationsData...","Please Wait...",true,true);
        }

        @Override
        protected void onPostExecute(String result) {

            if(type.equals("getTrackedLocations")){
                json_string = result;
                //      Log.e("Image JSON", json_string);
                if(json_string == null){
                    //Toast.makeText(getContext(),"First Get JSON checkInData",Toast.LENGTH_LONG).show();
                }
                else {
                    try {
                        jsonObject = new JSONObject(json_string);
                        jsonArray = jsonObject.getJSONArray("getTrackedLocations");

                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jo = jsonArray.getJSONObject(i);
                            String user_name,location_address,time,date,lattitude,longitude;

                            user_name =  jo.getString("user_name");
                            location_address = jo.getString("location_address");
                            time = jo.getString("time");
                            date = jo.getString("date");
                            lattitude = jo.getString("lattitude");
                            longitude = jo.getString("longitude");

                            TrackedLocationsData trackedLocationsData = new TrackedLocationsData( user_name,location_address,time,date,lattitude,longitude);
                            trackedLocationsAdapter.add(trackedLocationsData);
                            listName.add(user_name);
                            searchCancelAdapter.add(trackedLocationsData);
                        }
                        loading.dismiss();
                        listView.setAdapter(trackedLocationsAdapter);

                        trackedLocationsAdapter.notifyDataSetChanged();

                        suggestion_box.setVisibility(View.VISIBLE);
                        ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>(
                                TrackedLocationsActivity.this,
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
                        trackedLocationsAdapter.list.clear();
                        jsonObject = new JSONObject(json_string);
                        jsonArray = jsonObject.getJSONArray("getTrackedLocations");

                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jo = jsonArray.getJSONObject(i);
                            String user_name,location_address,time,date,lattitude,longitude;

                            user_name =  jo.getString("user_name");
                            location_address = jo.getString("location_address");
                            time = jo.getString("time");
                            date = jo.getString("date");
                            lattitude = jo.getString("lattitude");
                            longitude = jo.getString("longitude");

                            TrackedLocationsData trackedLocationsData = new TrackedLocationsData( user_name,location_address,time,date,lattitude,longitude);
                            trackedLocationsAdapter.add(trackedLocationsData);
                        }
                        loading.dismiss();
                        listView.setAdapter(trackedLocationsAdapter);

                        trackedLocationsAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
            if(type.equals("getMapLocations")){
                json_string = result;
                //      Log.e("Image JSON", json_string);
                if(json_string == null){
                    //Toast.makeText(getContext(),"First Get JSON checkInData",Toast.LENGTH_LONG).show();
                }
                else {
                    try {
                        Log.e("Location JSON", json_string);
                        jsonObject = new JSONObject(json_string);
                        jsonArray = jsonObject.getJSONArray("getMapLocations");

                        length = jsonArray.length();
                        time = new String[length];
                        dateArr = new String[length];
                        lat = new double[length];
                        lon = new double[length];
                        for(int i=0;i<length;i++){
                            JSONObject jo = jsonArray.getJSONObject(i);
                            String user_name,location_address;
                            Double lattitude,longitude;

                            user_name =  jo.getString("user_name");
                            location_address = jo.getString("location_address");
                            time[i] = jo.getString("time");
                            dateArr[i] = jo.getString("date");
                            lattitude = jo.getDouble("lattitude");
                            longitude = jo.getDouble("longitude");
                            lat[i]=lattitude;
                            lon[i]=longitude;
                        }
                        loading.dismiss();

                        Intent intent = new Intent(TrackedLocationsActivity.this,MapsTrackingActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Bundle extras = new Bundle();
                        extras.putString("user_name",user_name);
                        extras.putDoubleArray("lattitude", lat);
                        extras.putDoubleArray("longitude",lon);
                        extras.putStringArray("date",dateArr);
                        extras.putStringArray("time",time);
                        extras.putInt("length", length);
                        intent.putExtras(extras);
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

    }
}
