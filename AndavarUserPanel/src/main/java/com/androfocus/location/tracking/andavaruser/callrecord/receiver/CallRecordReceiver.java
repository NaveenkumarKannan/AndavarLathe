package com.androfocus.location.tracking.andavaruser.callrecord.receiver;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.androfocus.location.tracking.andavaruser.SessionManager;
import com.androfocus.location.tracking.andavaruser.callrecord.CallRecord;
import com.androfocus.location.tracking.andavaruser.callrecord.helper.PrefsHelper;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by aykutasil on 19.10.2016.
 */
public class CallRecordReceiver extends PhoneCallReceiver {


    private static final String TAG = CallRecordReceiver.class.getSimpleName();

    public static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    public static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    public static final String EXTRA_PHONE_NUMBER = "android.intent.extra.PHONE_NUMBER";

    protected CallRecord callRecord;
    private static MediaRecorder recorder;
    private File audiofile;
    private boolean isRecordStarted = false;

    File file;
    String duration,userId,location_address;

    public CallRecordReceiver(CallRecord callRecord) {
        this.callRecord = callRecord;
    }

    @Override
    protected void onIncomingCallReceived(Context context, String number, Date start) {

    }

    @Override
    protected void onIncomingCallAnswered(Context context, String number, Date start) {
        startRecord(context, "Incoming", number);
    }

    @Override
    protected void onIncomingCallEnded(Context context, String number, Date start, Date end) {
        stopRecord(context);
    }

    @Override
    protected void onOutgoingCallStarted(Context context, String number, Date start) {
        startRecord(context, "Outgoing", number);
    }

    @Override
    protected void onOutgoingCallEnded(Context context, String number, Date start, Date end) {
        stopRecord(context);
    }

    @Override
    protected void onMissedCall(Context context, String number, Date start) {

    }

    // Derived classes could override these to respond to specific events of interest
    protected void onRecordingStarted(Context context, CallRecord callRecord, File audioFile) {
        Log.e(TAG, "onRecordingStarted()");
    }

    protected void onRecordingFinished(Context context, CallRecord callRecord, File audioFile) {
        Log.e(TAG, "onRecordingFinished()");
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

    private void startRecord(Context context, String seed, String phoneNumber) {
        try {
            boolean isSaveFile = PrefsHelper.readPrefBool(context, CallRecord.PREF_SAVE_FILE);
            Log.e(TAG, "isSaveFile: " + isSaveFile);

            // dosya kayıt edilsin mi?
            if (!isSaveFile) {
                return;
            }

            if (isRecordStarted) {
                try {
                    recorder.stop();  // stop the recording
                } catch (RuntimeException e) {
                    // RuntimeException is thrown when stop() is called immediately after start().
                    // In this case the output file is not properly constructed ans should be deleted.
                    Log.e(TAG, "RuntimeException: stop() is called immediately after start()");
                    //noinspection ResultOfMethodCallIgnored
                    audiofile.delete();
                }
                releaseMediaRecorder();
                isRecordStarted = false;
            } else {
                if (prepareAudioRecorder(context, seed, phoneNumber)) {
                    recorder.start();
                    isRecordStarted = true;
                    onRecordingStarted(context, callRecord, audiofile);
                    Log.e(TAG, "record start");
                } else {
                    releaseMediaRecorder();
                }
                //new MediaPrepareTask().execute(null, null, null);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            releaseMediaRecorder();
        } catch (RuntimeException e) {
            e.printStackTrace();
            releaseMediaRecorder();
        } catch (Exception e) {
            e.printStackTrace();
            releaseMediaRecorder();
        }
    }

    private void stopRecord(Context context) {
        try {
            if (recorder != null && isRecordStarted) {
                releaseMediaRecorder();
                isRecordStarted = false;
                onRecordingFinished(context, callRecord, audiofile);
                Log.e(TAG, "record stop");
            }
        } catch (Exception e) {
            releaseMediaRecorder();
            e.printStackTrace();
        }
    }

    private boolean prepareAudioRecorder(Context context, String seed, String phoneNumber) {
        try {
            String file_name = PrefsHelper.readPrefString(context, CallRecord.PREF_FILE_NAME);
            String dir_path = PrefsHelper.readPrefString(context, CallRecord.PREF_DIR_PATH);
            String dir_name = PrefsHelper.readPrefString(context, CallRecord.PREF_DIR_NAME);
            boolean show_seed = PrefsHelper.readPrefBool(context, CallRecord.PREF_SHOW_SEED);
            boolean show_phone_number = PrefsHelper.readPrefBool(context, CallRecord.PREF_SHOW_PHONE_NUMBER);
            int output_format = PrefsHelper.readPrefInt(context, CallRecord.PREF_OUTPUT_FORMAT);
            int audio_source = PrefsHelper.readPrefInt(context, CallRecord.PREF_AUDIO_SOURCE);
            int audio_encoder = PrefsHelper.readPrefInt(context, CallRecord.PREF_AUDIO_ENCODER);

            File sampleDir = new File(dir_path + "/" + dir_name);

            if (!sampleDir.exists()) {
                sampleDir.mkdirs();
            }

            StringBuilder fileNameBuilder = new StringBuilder();
            fileNameBuilder.append(file_name);
            //fileNameBuilder.append("_");

            if (show_seed) {
                fileNameBuilder.append(seed);
                fileNameBuilder.append("_");
            }

            if (show_phone_number) {
                fileNameBuilder.append(phoneNumber);
                fileNameBuilder.append("_");
            }


            file_name = fileNameBuilder.toString();

            String suffix = "";
            switch (output_format) {
                case MediaRecorder.OutputFormat.AMR_NB: {
                    suffix = ".amr";
                    break;
                }
                case MediaRecorder.OutputFormat.AMR_WB: {
                    suffix = ".amr";
                    break;
                }
                case MediaRecorder.OutputFormat.MPEG_4: {
                    suffix = ".mp3";
                    break;
                }
                case MediaRecorder.OutputFormat.THREE_GPP: {
                    suffix = ".3gp";
                    break;
                }
                default: {
                    suffix = ".amr";
                    break;
                }
            }

            //audiofile = File.createTempFile(file_name, suffix, sampleDir);
            audiofile = new File(sampleDir, file_name +new SimpleDateFormat("ddMMyyyyHHmmss", Locale.US).format(new Date())+ suffix);
            //File imageFile = File.createTempFile(FileName, SUFFIX, Dir);
            //File imageFile = new File(Dir, FileName + SUFFIX);
            recorder = new MediaRecorder();
            recorder.setAudioSource(audio_source);
            recorder.setOutputFormat(output_format);
            recorder.setAudioEncoder(audio_encoder);
            recorder.setOutputFile(audiofile.getAbsolutePath());
            recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mediaRecorder, int i, int i1) {

                }
            });

            try {
                recorder.prepare();
            } catch (IllegalStateException e) {
                Log.e(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
                releaseMediaRecorder();
                return false;
            } catch (IOException e) {
                Log.e(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
                releaseMediaRecorder();
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void releaseMediaRecorder() {
        if (recorder != null) {
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }

    /*
    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            if (prepareAudioRecorder(, "", "")) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                recorder.start();
                Log.e(TAG, "record start");
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            isRecordStarted = true;
            onRecordingStarted(, callRecord, audiofile);
        }
    }
    */

}
