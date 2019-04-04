package com.androfocus.location.tracking.andavaruser;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class CheckINActivity extends AppCompatActivity {
    CheckInAdapter checkInAdapter;
    ListView listView;
    String type;
    ProgressDialog loading;
    SessionManager session;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        session = new SessionManager(getApplicationContext());
        //Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();

        HashMap<String, String> user = session.getUserDetails();
        userId = user.get(SessionManager.KEY_NAME);

        listView = (ListView) findViewById(R.id.lvCheckIn);
        checkInAdapter = new CheckInAdapter(this, R.layout.check_in_row_layout);

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
        Intent intent = new Intent(CheckINActivity.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    public void goBack(View view) {
        Intent intent = new Intent(CheckINActivity.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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
            String expenseUrl ="http://andavarinfo.com/AandavarLathe/getCheckIn.php";
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
            //loading = ProgressDialog.show(getContext(), "Fetching CheckInData...","Please Wait...",true,true);
        }

        @Override
        protected void onPostExecute(String result) {

            json_string = result;
            //
            if(json_string == null){
                //Toast.makeText(getContext(),"First Get JSON checkInData",Toast.LENGTH_LONG).show();
            }
            else {
                Log.e("Check In", json_string);
                try {
                    jsonObject = new JSONObject(json_string);
                    jsonArray = jsonObject.getJSONArray("getCheckIn");

                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject jo = jsonArray.getJSONObject(i);
                        String name,location,time,date;

                        name =  jo.getString("place_work_name");
                        location = jo.getString("location_address");
                        time = jo.getString("check_in_time");
                        date = jo.getString("check_in_date");


                        CheckInData checkInData = new CheckInData( name,location,time,date);
                        checkInAdapter.add(checkInData);
                    }
                    loading.dismiss();
                    listView.setAdapter(checkInAdapter);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

    }
}
