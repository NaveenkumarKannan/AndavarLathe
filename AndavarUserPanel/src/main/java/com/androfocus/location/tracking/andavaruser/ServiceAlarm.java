package com.androfocus.location.tracking.andavaruser;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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
import java.util.List;
import java.util.Locale;

public class ServiceAlarm extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "ServiceAlarm";
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

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        session = new SessionManager(getApplicationContext());

        HashMap<String, String> user = session.getUserDetails();
        userId = user.get(SessionManager.KEY_NAME);


        Log.e(TAG, "onStartCommand");
        startTracking();

        return super.onStartCommand(intent, flags, startId);
    }

    private void startTracking() {
        Log.e(TAG, "startTracking");

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            Log.e(TAG, "GooglePlayServicesAvailable");
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                Log.e(TAG, "googleApiClient.connect();");
                googleApiClient.connect();
            }
            //googleApiClient.connect();
        } else {
            Log.e(TAG, "unable to connect to google play services.");
        }
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        stopSelf();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.e(TAG, "position: " + location.getLatitude() + ", " + location.getLongitude() + " accuracy: " + location.getAccuracy());

            Geocoder geoCoder = new Geocoder(ServiceAlarm.this, Locale.getDefault());
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

                this.latitude = latitude;
                this.longitude = longitude;
                currentAddr = finalAddress;

                session.create_location(String.valueOf(latitude),String.valueOf(longitude) , finalAddress);
                type = "track";
                BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                backgroundWorker.execute();
                stopLocationUpdates();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        } else {
            Log.e(TAG, "NO POSITION FOUND");
        }
    }

    private void stopLocationUpdates() {
        Log.e(TAG, "stopLocationUpdates");
        googleApiClient.disconnect();
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
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");

        stopLocationUpdates();
        stopSelf();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "GoogleApiClient connection has been suspend");
    }

    public class BackgroundWorkerJson extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                String post_data = null;
                String idolUrl = null;
                if(type.equals("track")){
                    idolUrl = "http://andavarinfo.com/AandavarLathe/location_tracking.php";
                    Log.e(type,type );

                    post_data = URLEncoder.encode("lattitude", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(latitude), "UTF-8")
                            +"&"+URLEncoder.encode("longitude", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(longitude), "UTF-8")
                            +"&"+URLEncoder.encode("location_address", "UTF-8") + "=" + URLEncoder.encode(currentAddr, "UTF-8")
                            +"&"+URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
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

            if(type.equals("track")){
                Log.e(type,result );
                //Toast.makeText(getApplicationContext(),result ,Toast.LENGTH_LONG ).show();
                //loading.dismiss();
            }
        }
    }

}
