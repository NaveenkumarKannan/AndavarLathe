package com.androfocus.location.tracking.andavaruser.admin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;

public class UserDetailsActivity extends AppCompatActivity {

    UserDetailsAdapter userDetailsAdapter;
    ListView listView;
    String type;
    ProgressDialog loading;
    String userId,enable;
    UserDetailsData userDetailsData;
    int position;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        listView = (ListView) findViewById(R.id.lvUserDetails);
        userDetailsAdapter = new UserDetailsAdapter(this, R.layout.user_details_row_layout);

        loading = ProgressDialog.show(this, "Fetching Data...","Please Wait...",true,true);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                type = "getUserDetails";
                BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                backgroundWorker.execute();
            }
        });

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(UserDetailsActivity.this, "Stop Clicking me", Toast.LENGTH_SHORT).show();
                position = i;
                final TextView tvEnable;
                TextView tvUserId,tvUserName;
                tvUserId = view.findViewById(R.id.tvUserId);
                tvEnable = view.findViewById(R.id.tvEnable);
                tvUserName = view.findViewById(R.id.tvUserName);

                userDetailsData = (UserDetailsData) userDetailsAdapter.list.get(position);
                userId = userDetailsData.getUser_id();
                final String user = userDetailsData.getName();
                enable = userDetailsData.getEnable();
                Log.e("switch","pos"+position );
                Toast.makeText(UserDetailsActivity.this,user+" is selected",Toast.LENGTH_SHORT ).show();
                tvEnable.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String message = null;
                        if (enable.equals("Yes")){
                            enable = "No";
                            message = "deactivate";
                            Log.e("switch","true" );
                            //tvEnable.setText("Deactivated");
                            final AlertDialog.Builder alertUser = new AlertDialog.Builder(UserDetailsActivity.this);
                            alertUser.setTitle("Confirm Action!");
                            alertUser.setMessage("Do you want to "+message+" the user "+user+"?");
                            alertUser.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    type = "enableUser";
                                    BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                                    backgroundWorker.execute();
                                }
                            });
                            alertUser.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertUser.setCancelable(true);
                                    enable = "Yes";
                                }
                            });
                            alertUser.show();
                        }else if(enable.equals("No")){
                            enable = "Yes";
                            message = "activate";
                            Log.e("switch","false" );
                            //tvEnable.setText("Activated");
                            final AlertDialog.Builder alertUser = new AlertDialog.Builder(UserDetailsActivity.this);
                            alertUser.setTitle("Confirm Action!");
                            alertUser.setMessage("Do you want to "+message+" the user "+user+"?");
                            alertUser.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    type = "enableUser";
                                    BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                                    backgroundWorker.execute();
                                }
                            });
                            alertUser.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertUser.setCancelable(true);
                                    enable = "No";
                                }
                            });
                            alertUser.show();
                        }else {
                            Toast.makeText(UserDetailsActivity.this,"Some tasks are running. Try again later...",Toast.LENGTH_SHORT ).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loading.dismiss();
    }

    public void goBack(View view) {
        finish();
    }

    public class BackgroundWorkerJson extends AsyncTask<String,Void,String> {
        String json_string;
        JSONArray jsonArray;
        JSONObject jsonObject;

        @Override
        protected String doInBackground(String... params) {
            String getUserDetailsUrl ="http://andavarinfo.com/AandavarLathe/Admin/getUserDetails.php";
            String enableUserUrl ="http://andavarinfo.com/AandavarLathe/Admin/enableUser.php";
            if(type.equals("getUserDetails")){

                try {
                    URL url = new URL(getUserDetailsUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(" ", "UTF-8");
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

            }else if (type.equals("enableUser")){

                try {
                    URL url = new URL(enableUserUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
                            +"&"+URLEncoder.encode("enable", "UTF-8") + "=" + URLEncoder.encode(enable, "UTF-8")
                            ;
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

            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //loading = ProgressDialog.show(getContext(), "Fetching UserDetailsData...","Please Wait...",true,true);
        }

        @Override
        protected void onPostExecute(String result) {

            if(type.equals("getUserDetails")){
                json_string = result;
                //      Log.e("Image JSON", json_string);
                if(json_string == null){
                    //Toast.makeText(getContext(),"First Get JSON userDetailsData",Toast.LENGTH_SHORT).show();
                }
                else {
                    try {
                        jsonObject = new JSONObject(json_string);
                        jsonArray = jsonObject.getJSONArray("getUserDetails");

                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jo = jsonArray.getJSONObject(i);
                            String user_id,name,email,phNo,enable;

                            user_id = jo.getString("user_id");
                            name =  jo.getString("user_name");
                            email = jo.getString("email");
                            phNo = jo.getString("phNo");
                            enable = jo.getString("enable");


                            UserDetailsData userDetailsData = new UserDetailsData( user_id,name,email,phNo,enable);
                            userDetailsAdapter.add(userDetailsData);
                        }
                        loading.dismiss();
                        listView.setAdapter(userDetailsAdapter);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }else if (type.equals("enableUser")){

                if(enable.equals("Yes")){
                    if(userDetailsData == null){
                        Toast.makeText(UserDetailsActivity.this,"Some tasks are running. Try again later...",Toast.LENGTH_SHORT ).show();
                    }else {
                        userDetailsData.setEnable(enable);
                        userDetailsAdapter.list.set(position,userDetailsData);
                        userDetailsAdapter.notifyDataSetChanged();
                        //userDetailsData = null;
                        Toast.makeText(UserDetailsActivity.this,"The user is activated",Toast.LENGTH_SHORT ).show();
                    }
                }else if(enable.equals("No")){
                    if(userDetailsData == null){
                        Toast.makeText(UserDetailsActivity.this,"Some tasks are running. Try again later...",Toast.LENGTH_SHORT ).show();
                    }else {
                        userDetailsData.setEnable(enable);
                        userDetailsAdapter.list.set(position,userDetailsData);
                        userDetailsAdapter.notifyDataSetChanged();
                        //userDetailsData = null;
                        Toast.makeText(UserDetailsActivity.this,"The user is deactivated",Toast.LENGTH_SHORT ).show();
                    }
                }else {
                    Toast.makeText(UserDetailsActivity.this,"Some tasks are running. Try again later...",Toast.LENGTH_SHORT ).show();
                }
            }

        }

    }
}
