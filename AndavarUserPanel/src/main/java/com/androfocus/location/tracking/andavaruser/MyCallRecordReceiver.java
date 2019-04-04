package com.androfocus.location.tracking.andavaruser;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.androfocus.location.tracking.andavaruser.callrecord.CallRecord;
import com.androfocus.location.tracking.andavaruser.callrecord.receiver.CallRecordReceiver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by aykutasil on 26.12.2016.
 */

public class MyCallRecordReceiver extends CallRecordReceiver {

    File file;
    String duration,userId,location_address;
    public MyCallRecordReceiver(CallRecord callRecord) {
        super(callRecord);
        Log.e("MyCallRecordReceiver", "MyCallRecordReceiver");
    }

    @Override
    protected void onIncomingCallReceived(Context context, String number, Date start) {
        Log.e("MyCallRecordReceiver", "onIncomingCallReceived");
        super.onIncomingCallReceived(context, number, start);
    }

    @Override
    protected void onIncomingCallAnswered(Context context, String number, Date start) {
        Log.e("MyCallRecordReceiver", "onIncomingCallAnswered");
        super.onIncomingCallAnswered(context, number, start);
    }

    @Override
    protected void onIncomingCallEnded(Context context, String number, Date start, Date end) {
        Log.e("MyCallRecordReceiver", "onIncomingCallEnded");
        super.onIncomingCallEnded(context, number, start, end);
    }

    @Override
    protected void onOutgoingCallStarted(Context context, String number, Date start) {
        Log.e("MyCallRecordReceiver", "onOutgoingCallStarted");
        super.onOutgoingCallStarted(context, number, start);
    }

    @Override
    protected void onOutgoingCallEnded(Context context, String number, Date start, Date end) {
        Log.e("MyCallRecordReceiver", "onOutgoingCallEnded");
        super.onOutgoingCallEnded(context, number, start, end);
    }

    @Override
    protected void onMissedCall(Context context, String number, Date start) {
        Log.e("MyCallRecordReceiver", "onMissedCall");
        super.onMissedCall(context, number, start);
    }

    @Override
    protected void onRecordingStarted(Context context, CallRecord callRecord, File audioFile) {
        Log.e("MyCallRecordReceiver", "onRecordingStarted");
        super.onRecordingStarted(context, callRecord, audioFile);
    }

    @Override
    protected void onRecordingFinished(Context context, CallRecord callRecord, File audioFile) {
        Log.e("MyCallRecordReceiver", "onRecordingFinished");
        super.onRecordingFinished(context, callRecord, audioFile);

        SessionManager session;
        session = new SessionManager(context);
        //Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();

        HashMap<String, String> user = session.getUserDetails();
        userId = user.get(SessionManager.KEY_NAME);

        HashMap<String, String> location = session.get_location();
        location_address = location.get(SessionManager.Addr);

        file = audioFile;
        String fileName,filePath;
        fileName = file.getName();
        filePath = file.getPath();
        Log.e("AudioFile Name", fileName);
        Log.e("AudioFile Path", filePath);

        Uri uri = Uri.parse(filePath);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context,uri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int millSecond = Integer.parseInt(durationStr);
        duration = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millSecond),
                TimeUnit.MILLISECONDS.toMinutes(millSecond) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millSecond) % TimeUnit.MINUTES.toSeconds(1));

        Log.e("duration",duration);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                BackgroundWorker backgroundWorker = new BackgroundWorker();
                backgroundWorker.execute();
            }
        });

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
    public class BackgroundWorker extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            Log.e("BackgroundWorker", "doInBackground()");
            try {
                // Connect to the web server endpoint
                URL serverUrl =
                        new URL("http://andavarinfo.com/AandavarLathe/insert_call.php");
                        //new URL("http://nutzindia.com/AandavarLathe/insert_call.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection) serverUrl.openConnection();

                String boundaryString = "----SomeRandomText";
                String fileUrl = "/logs/20150208.log";
                File logFileToUpload = file;//new File(fileUrl);

                // Indicate that we want to write to the HTTP request body
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);

                OutputStream outputStreamToRequestBody = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter =
                        new BufferedWriter(new OutputStreamWriter(outputStreamToRequestBody));
                
                //Include value from the myFileDescription text area in the post data
                bufferedWriter.write("\n\n--" + boundaryString + "\n");
                bufferedWriter.write("Content-Disposition: form-data; name=\"myFileDescription\"");
                bufferedWriter.write("\n\n");
                bufferedWriter.write( logFileToUpload.getName());

                //Include value from the myFileDescription text area in the post data
                bufferedWriter.write( "\n--" + boundaryString + "\n");
                bufferedWriter.write("Content-Disposition: form-data; name=\"user_id\"");
                bufferedWriter.write("\n\n");
                bufferedWriter.write(userId);

                //Include value from the myFileDescription text area in the post data
                bufferedWriter.write( "\n--" + boundaryString + "\n");
                bufferedWriter.write("Content-Disposition: form-data; name=\"duration\"");
                bufferedWriter.write("\n\n");
                bufferedWriter.write(duration);

                //Include value from the myFileDescription text area in the post data
                bufferedWriter.write( "\n--" + boundaryString + "\n");
                bufferedWriter.write("Content-Disposition: form-data; name=\"location_address\"");
                bufferedWriter.write("\n\n");
                bufferedWriter.write(location_address);
                
                //Include the section to describe the file
                bufferedWriter.write("\n--" + boundaryString + "\n");
                bufferedWriter.write("Content-Disposition: form-data;"
                        + "name=\"myFile\";"
                        + "filename=\""+ logFileToUpload.getName() +"\""
                        + "\nContent-Type: text/plain\n\n");
                bufferedWriter.flush();
                
                // Write the actual file contents
                FileInputStream inputStreamToLogFile = new FileInputStream(logFileToUpload);

                int bytesRead;
                byte[] dataBuffer = new byte[1024];
                while((bytesRead = inputStreamToLogFile.read(dataBuffer)) != -1) {
                    outputStreamToRequestBody.write(dataBuffer, 0, bytesRead);
                }

                outputStreamToRequestBody.flush();
                
                // Mark the end of the multipart http request
                bufferedWriter.write("\n--" + boundaryString + "--\n");
                bufferedWriter.flush();
                
                // Close the streams
                outputStreamToRequestBody.close();
                bufferedWriter.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String result="";
                String line="";
                while((line = bufferedReader.readLine())!= null) {
                    result += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                //Log.e("result", result);
                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }




            return null;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("result", result);
        }


    }
}
