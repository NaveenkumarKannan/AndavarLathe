package com.androfocus.location.tracking.andavaruser.admin;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.androfocus.location.tracking.andavaruser.SessionManager;
import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
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
import java.util.HashMap;
import java.util.List;

public class RecordedCallsActivity extends AppCompatActivity {
    RecordedCallsAdapter recordedCallsAdapter;
    ListView listView;
    String type;
    ProgressDialog loading;
    JcPlayerView jcPlayerView;

    RecordedCallsAdapter searchCancelAdapter;
    AutoCompleteTextView suggestion_box;
    private List<String> listName;
    String searchKey;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorded_calls_admin);

        listView = (ListView) findViewById(R.id.lvRecordedCalls);
        recordedCallsAdapter = new RecordedCallsAdapter(this, R.layout.recorded_calls_row_layout_admin);
        searchCancelAdapter = new RecordedCallsAdapter(this, R.layout.recorded_calls_row_layout_admin);

        jcPlayerView = (JcPlayerView) findViewById(R.id.jcPlayerView);


        final ArrayList<JcAudio> jcAudios = new ArrayList<>();
        //jcAudios.add(JcAudio.createFromURL("url audio","http://xxx/audio.mp3"));
        //jcAudios.add(JcAudio.createFromAssets("Asset audio", "audio.mp3"));
        jcAudios.add(JcAudio.createFromRaw("Raw audio", R.raw.incoming));
        jcAudios.add(JcAudio.createFromRaw("Raw audio", R.raw.horse_whinnies));

        jcPlayerView.initAnonPlaylist(jcAudios);
        //jcPlayerView.initPlaylist(jcAudios, null);

        //Option 2: Initialize an anonymous playlist with a default title for all
        //    jcPlayerView.initAnonPlaylist(jcAudios);
        //Option 3: Initialize an playlist with a custom title for all
        //    jcPlayerView.initWithTitlePlaylist(urls, "Awesome music");
        loading = ProgressDialog.show(this, "Fetching Data...","Please Wait...",true,true);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                type = "getRecordedCall";
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
                final TextView tvUrl,tvFileName;
                tvUrl = (TextView) view.findViewById(R.id.tvUrl);
                tvFileName = view.findViewById(R.id.tvFileName);
                final String url,fileName;
                url = tvUrl.getText().toString();
                fileName = tvFileName.getText().toString();
                jcPlayerView.playAudio(JcAudio.createFromURL(fileName,url));

                //Call the notification player where you want.
                //jcPlayerView.createNotification(); // default icon
                //jcPlayerView.createNotification(R.drawable.myIcon); // Your icon resource

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
    }
    @Override
    public void onBackPressed() {
        jcPlayerView.pause();
        finish();
    }
    public void goBack(View view) {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        jcPlayerView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        jcPlayerView.kill();
        //unbindService(new JcServiceConnection(this));
    }

    public void cancelSearch(View view) {
        suggestion_box.clearListSelection();
        suggestion_box.setText("",null );
        //suggestion_box.setHint("Type to search");
        recordedCallsAdapter.list.clear();
        recordedCallsAdapter.list = searchCancelAdapter.list;
        listView.setAdapter(recordedCallsAdapter);
        recordedCallsAdapter.notifyDataSetChanged();
    }
    
    public class BackgroundWorkerJson extends AsyncTask<String,Void,String> {
        //ProgressDialog loading;
        String json_string;
        JSONArray jsonArray;
        JSONObject jsonObject;

        @Override
        protected String doInBackground(String... params) {

            SessionManager session;
            session = new SessionManager(RecordedCallsActivity.this);
            //Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();

            HashMap<String, String> user = session.getUserDetails();
            String userId = user.get(SessionManager.KEY_NAME);

            try {
                String post_data = null;
                String expenseUrl = null;
                if(type.equals("getRecordedCall")){
                    expenseUrl = "http://andavarinfo.com/AandavarLathe/Admin/getRecordedCall.php";
                    Log.w(type,type );

                    post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
                            +"&"+ URLEncoder.encode("searchKey", "UTF-8") + "=" + URLEncoder.encode("All", "UTF-8")
                    ;
                }
                if(type.equals("Select")){
                    expenseUrl = "http://andavarinfo.com/AandavarLathe/Admin/getRecordedCall.php";
                    Log.w(type,type );

                    post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
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
            //loading = ProgressDialog.show(getContext(), "Fetching RecordedCallsData...","Please Wait...",true,true);
        }

        @Override
        protected void onPostExecute(String result) {

            if(type.equals("getRecordedCall")){
                json_string = result;
                //      Log.e("Image JSON", json_string);
                if(json_string == null){
                    //Toast.makeText(getContext(),"First Get JSON checkInData",Toast.LENGTH_LONG).show();
                }
                else {
                    try {
                        jsonObject = new JSONObject(json_string);
                        jsonArray = jsonObject.getJSONArray("getRecordedCall");

                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jo = jsonArray.getJSONObject(i);
                            String file_url,call_duration,call_time,call_date,user_name,file_name,location_address;

                            file_url =  jo.getString("file_url");
                            call_duration = jo.getString("call_duration");
                            call_time = jo.getString("call_time");
                            call_date = jo.getString("call_date");
                            user_name = jo.getString("user_name");
                            file_name = jo.getString("file_name");
                            location_address = jo.getString("location_address");
                            
                            RecordedCallsData recordedCallsData = new RecordedCallsData( user_name,file_url,call_duration,call_date,call_time,file_name,location_address);
                            recordedCallsAdapter.add(recordedCallsData);

                            listName.add(user_name);
                            searchCancelAdapter.add(recordedCallsData);
                        }
                        loading.dismiss();
                        listView.setAdapter(recordedCallsAdapter);

                        suggestion_box.setVisibility(View.VISIBLE);
                        ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>(
                                RecordedCallsActivity.this,
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
                        recordedCallsAdapter.list.clear();
                        jsonObject = new JSONObject(json_string);
                        jsonArray = jsonObject.getJSONArray("getRecordedCall");

                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jo = jsonArray.getJSONObject(i);
                            String file_url,call_duration,call_time,call_date,user_name,file_name,location_address;

                            file_url =  jo.getString("file_url");
                            call_duration = jo.getString("call_duration");
                            call_time = jo.getString("call_time");
                            call_date = jo.getString("call_date");
                            user_name = jo.getString("user_name");
                            file_name = jo.getString("file_name");
                            location_address = jo.getString("location_address");
                            RecordedCallsData recordedCallsData = new RecordedCallsData( user_name,file_url,call_duration,call_date,call_time,file_name,location_address);
                            recordedCallsAdapter.add(recordedCallsData);
                        }
                        loading.dismiss();
                        listView.setAdapter(recordedCallsAdapter);

                        recordedCallsAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

    }
}
