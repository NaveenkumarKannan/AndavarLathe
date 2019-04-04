package com.androfocus.location.tracking.andavaruser;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AttendanceActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    Button btnStart,btnEnd;

    private static final String TAG = "AttendanceLocation";
    private static final long INTERVAL = 1000;
    private static final long FASTEST_INTERVAL = 1000;

    private boolean currentlyProcessingLocation = false;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;

    Double latitude, longitude;
    String currentAddr;
    String type;


    SessionManager session;
    String userId;
    //ProgressDialog loading;
    String start_time, end_time;

    private static final long ALARM_INTERVAL = 60000*3;
    AlarmManager alarms;
    PendingIntent recurringLl24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        //By using Broadcast
        //Intent ll24 = new Intent(this, AlarmReceiver.class);
        //PendingIntent recurringLl24 = PendingIntent.getBroadcast(this, 0, ll24, PendingIntent.FLAG_CANCEL_CURRENT);
        //By using service
        Intent ll24 = new Intent(AttendanceActivity.this, ServiceAlarm.class);
        recurringLl24 = PendingIntent.getService(AttendanceActivity.this, 0, ll24, PendingIntent.FLAG_CANCEL_CURRENT);
        alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        session = new SessionManager(getApplicationContext());

        HashMap<String, String> user = session.getUserDetails();
        userId = user.get(SessionManager.KEY_NAME);


        btnStart = findViewById(R.id.btnStart);
        btnEnd = findViewById(R.id.btnEnd);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                System.out.println("Current time => "+c.getTime());
                SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                start_time = df.format(c.getTime());

                //loading = ProgressDialog.show(AttendanceActivity.this, "Processing...","Please Wait...",true,true);
                if(currentAddr == null){
                    Toast.makeText(AttendanceActivity.this,"Location not found. Wait for a minute and try again." , Toast.LENGTH_LONG).show();
                }else {
                    df = new SimpleDateFormat("hh:mm:ss aa");
                    btnStart.setText("Started at "+df.format(c.getTime()));
                    btnEnd.setVisibility(View.VISIBLE);
                    Log.e("Button","start" );

                    type = "start";
                    BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                    backgroundWorker.execute(start_time);
                }
            }
        });
        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                System.out.println("Current time => "+c.getTime());
                SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

                end_time = df.format(c.getTime());
                Log.e("EndTime",end_time );
                if(currentAddr == null){
                    Toast.makeText(AttendanceActivity.this,"Location not found. Wait for a minute and try again." , Toast.LENGTH_LONG).show();
                }else {
                    type = "end";
                    ////loading = ProgressDialog.show(AttendanceActivity.this, "Processing...","Please Wait...",true,true);
                    BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                    backgroundWorker.execute();
                }

                Log.e("Button","end" );
            }
        });
        if (!currentlyProcessingLocation) {
            Log.e(TAG, "about to start tracking....");
            currentlyProcessingLocation = true;
            startTracking();
        }
        type = "getAttendance";
        BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
        backgroundWorker.execute();
    }

    @Override
    public void onBackPressed() {
        stopLocationUpdates();
        Intent intent = new Intent(AttendanceActivity.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    public void goBack(View view) {
        stopLocationUpdates();
        Intent intent = new Intent(AttendanceActivity.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        stopLocationUpdates();
    }
    private void startTracking() {
        Log.e(TAG, "startTracking");

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {

            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {
            Log.e(TAG, "unable to connect to google play services.");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.e(TAG, "position: " + location.getLatitude() + ", " + location.getLongitude() + " accuracy: " + location.getAccuracy());

            Geocoder geoCoder = new Geocoder(AttendanceActivity.this, Locale.getDefault());
            StringBuilder builder = new StringBuilder();
            try {
                Double latitude,longitude;
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                List<Address> address = geoCoder.getFromLocation(latitude, longitude, 1);
                int maxLines = address.get(0).getMaxAddressLineIndex();
                for (int i=0; i<maxLines; i++) {
                    String addressStr = address.get(0).getAddressLine(i);
                    builder.append(addressStr);
                    builder.append(" ");
                }

                String finalAddress;
                //finalAddress= builder.toString(); //This is the complete address.
                finalAddress = address.get(0).getAddressLine(0)
                //        +"\n"+ address.get(0).getLocality()+"\n"+
                //      address.get(0).getAdminArea()+"\n"+
                //    address.get(0).getCountryName()+"\n"+
                //  address.get(0).getPostalCode()
                ;
                Log.e("Location", "Latitude: " + latitude + "\nLongitude: " + longitude+"\nFinal Address: "+finalAddress);

                //t.setText("Latitude: " + latitude + "\nLongitude: " + longitude+"\nFinal Address: "+finalAddress); //This will display the final address.

                //stopLocationUpdates();

                this.latitude = latitude;
                this.longitude = longitude;
                currentAddr = finalAddress;


            } catch (IOException e) {
                // Handle IOException
            } catch (NullPointerException e) {
                // Handle NullPointerException
            }

        } else {
            Log.e(TAG, "NO POSITION FOUND");
        }
    }

    private void stopLocationUpdates() {
        /*
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        */
        googleApiClient.disconnect();
        Log.e(TAG, "stopLocationUpdates");
    }

    /**
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.e(TAG, "onConnected");

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(INTERVAL); // milliseconds
        locationRequest.setFastestInterval(FASTEST_INTERVAL); // the fastest rate in milliseconds at which your app can handle location updates
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        startLocationUpdates();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
        stopLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }
    protected void onStop() {
        super.onStop();

        googleApiClient.disconnect();
        Log.e(TAG, "isConnected ...............: " + googleApiClient.isConnected());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (googleApiClient.isConnected()) {
            startLocationUpdates();
            Log.e(TAG, "Location update resumed .....................");
        }
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
        Log.e(TAG, "startLocationUpdates");
    }

    public void onPastAttendence(View view) {
        Intent intent = new Intent(AttendanceActivity.this,PastAttendanceActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public class BackgroundWorkerJson extends AsyncTask<String,Void,String> {
        String json_string;
        JSONArray jsonArray;
        JSONObject jsonObject;

        @Override
        protected String doInBackground(String... params) {

            try {
                String post_data = null;
                String idolUrl = null;
                if(type.equals("start")){
                    //idolUrl ="http://ulix.cuccfree.com/idol_android/check_in.php";
                    //idolUrl = "https://ulixsoftware.com/idolsoftware/model/IDOL/attendance_start.php";
                    //idolUrl = "http://arulaudios.com/IDOL/attendance_start.php";
                    idolUrl = "http://andavarinfo.com/AandavarLathe/attendance_start.php";
                    //idolUrl = "http://nutzindia.com/AandavarLathe/attendance_start.php";
                    //idolUrl = "http://arulaudios.com/AandavarLathe/attendance_start.php";
                    Log.e(type,type );
                    String start_time;
                    start_time = params[0];

                    post_data = URLEncoder.encode("lattitude_start", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(latitude), "UTF-8")
                            +"&"+URLEncoder.encode("longitude_start", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(longitude), "UTF-8")
                            +"&"+URLEncoder.encode("start_time", "UTF-8") + "=" + URLEncoder.encode(start_time, "UTF-8")
                            +"&"+URLEncoder.encode("location_addres_start", "UTF-8") + "=" + URLEncoder.encode(currentAddr, "UTF-8")
                            +"&"+URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
                    ;
                }else if(type.equals("end")){
                    //idolUrl ="http://ulix.cuccfree.com/idol_android/check_in.php";
                    //idolUrl = "https://ulixsoftware.com/idolsoftware/model/IDOL/attendance_end.php";
                    idolUrl = "http://andavarinfo.com/AandavarLathe/attendance_end.php";
                    //idolUrl = "http://nutzindia.com/AandavarLathe/attendance_end.php";
                    //idolUrl = "http://arulaudios.com/AandavarLathe/attendance_end.php";
                    Log.e(type,type );

                    post_data = URLEncoder.encode("lattitude_end", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(latitude), "UTF-8")
                            +"&"+URLEncoder.encode("longitude_end", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(longitude), "UTF-8")
                            +"&"+URLEncoder.encode("end_time", "UTF-8") + "=" + URLEncoder.encode(end_time, "UTF-8")
                            +"&"+URLEncoder.encode("location_address_end", "UTF-8") + "=" + URLEncoder.encode(currentAddr, "UTF-8")
                            +"&"+URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
                            +"&"+URLEncoder.encode("start_time", "UTF-8") + "=" + URLEncoder.encode(start_time, "UTF-8")
                    ;
                }else if(type.equals("getAttendance")){
                    //idolUrl ="http://ulix.cuccfree.com/idol_android/expenses.php";
                    //idolUrl = "https://ulixsoftware.com/idolsoftware/model/IDOL/getAttendance.php";
                    idolUrl = "http://andavarinfo.com/AandavarLathe/getAttendance.php";
                    //idolUrl = "http://nutzindia.com/AandavarLathe/getAttendance.php";
                    //idolUrl = "http://arulaudios.com/AandavarLathe/getAttendance.php";
                    Log.e(type,type );


                    post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
                    ;

                }

                URL url = new URL(idolUrl);
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
        }

        @Override
        protected void onPostExecute(String result) {

            if(type.equals("start")){
                Log.e(type,result );
                if(result.charAt(0)=='A') {
                    Log.e("Alarm", "Started...");
                    long currentTimeMillis = System.currentTimeMillis();
                    alarms.setRepeating(AlarmManager.RTC_WAKEUP, currentTimeMillis, ALARM_INTERVAL, recurringLl24); // Log repetition

                    Toast.makeText(getApplicationContext(),result ,Toast.LENGTH_LONG ).show();
                }
                //loading.dismiss();
            }else if(type.equals("end")){
                Log.e(type,result );
                if(result.charAt(0)=='A') {
                    Toast.makeText(getApplicationContext(),result ,Toast.LENGTH_LONG ).show();
                    btnStart.setText("Start");
                    btnEnd.setVisibility(View.GONE);

                    Log.e("Alarm", "Stopped...");
                    alarms.cancel(recurringLl24);
                }

                //loading.dismiss();
            }else if(type.equals("getAttendance")){
                Log.e(type,result );
                json_string = result;

                if(json_string != null)
                {
                    try {
                        jsonObject = new JSONObject(json_string);
                        jsonArray = jsonObject.getJSONArray("getAttendance");

                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jo = jsonArray.getJSONObject(i);

                            start_time =  jo.getString("start_time");
                            end_time =  jo.getString("end_time");
                        }
//                        loading.dismiss();

                        if(start_time!=null){
                            if(end_time.equals("00:00:00")){
                                long currentTimeMillis = System.currentTimeMillis();
                                alarms.setRepeating(AlarmManager.RTC_WAKEUP, currentTimeMillis, ALARM_INTERVAL, recurringLl24); // Log repetition

                                btnStart.setText("Started at "+start_time);
                                btnEnd.setVisibility(View.VISIBLE);
                            }
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else {
                    Log.e("JSON","null" );
                }
            }
        }
    }


}
