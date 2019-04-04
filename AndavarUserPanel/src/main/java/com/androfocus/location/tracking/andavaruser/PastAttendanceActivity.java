package com.androfocus.location.tracking.andavaruser;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

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
import java.util.HashMap;

public class PastAttendanceActivity extends AppCompatActivity {
    PastAttendanceAdapter pastAttendanceAdapter;
    ListView listView;
    String type;
    ProgressDialog loading;
    SessionManager session;
    String userId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_attendance);

        session = new SessionManager(getApplicationContext());
        //Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();

        HashMap<String, String> user = session.getUserDetails();
        userId = user.get(SessionManager.KEY_NAME);

        listView = (ListView) findViewById(R.id.lvPastAttendance);
        pastAttendanceAdapter = new PastAttendanceAdapter(this, R.layout.past_attendance_row_layout);

        loading = ProgressDialog.show(this, "Fetching Data...","Please Wait...",true,true);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                backgroundWorker.execute();
            }
        });

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
    @Override
    public void onBackPressed() {
        finish();
    }
    public void goBack(View view) {
        finish();
    }

    public class BackgroundWorkerJson extends AsyncTask<String,Void,String> {
        Context context;
        String type;
        //ProgressDialog loading;
        String json_string;
        JSONArray jsonArray;
        JSONObject jsonObject;

        @Override
        protected String doInBackground(String... params) {

            String expenseUrl ="http://andavarinfo.com/AandavarLathe/getPastAttendance.php";

            try {
                URL url = new URL(expenseUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8");
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
            //loading = ProgressDialog.show(getContext(), "Fetching PastAttendanceData...","Please Wait...",true,true);
        }

        @Override
        protected void onPostExecute(String result) {

            json_string = result;
            //      Log.e("Image JSON", json_string);
            if(json_string == null){
                //Toast.makeText(getContext(),"First Get JSON pastAttendanceData",Toast.LENGTH_LONG).show();
            }
            else {
                try {
                    jsonObject = new JSONObject(json_string);
                    jsonArray = jsonObject.getJSONArray("getPastAttendance");

                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject jo = jsonArray.getJSONObject(i);
                        String start_time,att_date,end_time,tot_work_time;

                        start_time =  jo.getString("start_time");
                        att_date = jo.getString("att_date");
                        end_time = jo.getString("end_time");
                        tot_work_time = jo.getString("tot_work_time");


                        PastAttendanceData pastAttendanceData = new PastAttendanceData( start_time,end_time,att_date,tot_work_time);
                        pastAttendanceAdapter.add(pastAttendanceData);
                    }
                    loading.dismiss();
                    listView.setAdapter(pastAttendanceAdapter);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

    }
}
