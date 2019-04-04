package com.androfocus.location.tracking.andavaruser.callrecord;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.androfocus.location.tracking.andavaruser.callrecord.helper.PrefsHelper;
import com.androfocus.location.tracking.andavaruser.callrecord.receiver.CallRecordReceiver;
import com.androfocus.location.tracking.andavaruser.callrecord.service.CallRecordService;

import static android.app.job.JobScheduler.RESULT_SUCCESS;
import static android.content.Context.JOB_SCHEDULER_SERVICE;

/**
 * Created by aykutasil on 20.10.2016.
 */

public class CallRecord {

    private static final String TAG = CallRecord.class.getSimpleName();

    public static final String PREF_SAVE_FILE = "PrefSaveFile";
    public static final String PREF_FILE_NAME = "PrefFileName";
    public static final String PREF_DIR_NAME = "PrefDirName";
    public static final String PREF_DIR_PATH = "PrefDirPath";
    public static final String PREF_SHOW_SEED = "PrefShowSeed";
    public static final String PREF_SHOW_PHONE_NUMBER = "PrefShowPhoneNumber";
    public static final String PREF_AUDIO_SOURCE = "PrefAudioSource";
    public static final String PREF_AUDIO_ENCODER = "PrefAudioEncoder";
    public static final String PREF_OUTPUT_FORMAT = "PrefOutputFormat";


    private Context mContext;
    private static CallRecordReceiver mCallRecordReceiver;

    int JOB_ID = 101;
    JobScheduler jobScheduler;
    JobInfo jobInfo;

    private CallRecord(Context context) {
        this.mContext = context;
    }

    public static CallRecord initReceiver(Context context) {
        CallRecord callRecord = new Builder(context).build();
        callRecord.startCallReceiver();
        return callRecord;
    }

    public static CallRecord initService(Context context) {
        CallRecord callRecord = new Builder(context).build();
        callRecord.startCallRecordService();
        return callRecord;
    }

    public void startCallReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CallRecordReceiver.ACTION_IN);
        intentFilter.addAction(CallRecordReceiver.ACTION_OUT);

        if (mCallRecordReceiver == null) {
            Log.e("mCallRecordReceiver", "null");
            mCallRecordReceiver = new CallRecordReceiver(this);
        }else {
            Log.e("mCallRecordReceiver", "not null");
            stopCallReceiver();
        }
        mContext.registerReceiver(mCallRecordReceiver, intentFilter);
    }

    public void stopCallReceiver() {
        try {
            if (mCallRecordReceiver != null) {
                mContext.unregisterReceiver(mCallRecordReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startCallRecordService() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
            Log.e(TAG, ">=N");

            ComponentName componentName = new ComponentName(mContext,CallJobService.class);
            JobInfo.Builder builder = new JobInfo.Builder(JOB_ID,componentName);

            builder.setPeriodic(1000);
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            builder.setPersisted(true);

            jobInfo = builder.build();
            jobScheduler = (JobScheduler) mContext.getSystemService(JOB_SCHEDULER_SERVICE);
            if (jobScheduler != null) {
                jobScheduler.cancelAll();
                if(jobScheduler.schedule(jobInfo)==RESULT_SUCCESS){
                    Log.e(TAG, "jobScheduler scheduled");
                }else
                    Log.e(TAG, "jobScheduler not scheduled");
            }
        }else{
            Log.e(TAG, "<N");
            Intent intent = new Intent();
            intent.setClass(mContext, CallRecordService.class);
            mContext.startService(intent);
        }
        Log.e(TAG, "startService()");
    }

    public void enableSaveFile() {
        PrefsHelper.writePrefBool(mContext, PREF_SAVE_FILE, true);
        Log.e("CallRecord", "Save file enabled");
    }

    public void disableSaveFile() {
        Log.e("CallRecord", "Save file disabled");
        PrefsHelper.writePrefBool(mContext, PREF_SAVE_FILE, false);
    }

    public boolean getStateSaveFile() {
        return PrefsHelper.readPrefBool(mContext, PREF_SAVE_FILE);
    }

    public void changeRecordFileName(String newFileName) {
        if (newFileName == null || newFileName.isEmpty()) {
            try {
                throw new Exception("newFileName can not be empty or null");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        PrefsHelper.writePrefString(mContext, PREF_FILE_NAME, newFileName);
        Log.e("CallRecord", "New file name: " + newFileName);
    }

    public String getRecordFileName() {
        return PrefsHelper.readPrefString(mContext, PREF_FILE_NAME);
    }

    public void changeRecordDirName(String newDirName) {
        if (newDirName == null || newDirName.isEmpty()) {
            try {
                throw new Exception("newDirName can not be empty or null");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        PrefsHelper.writePrefString(mContext, PREF_DIR_NAME, newDirName);
        Log.e("CallRecord", "New dir name: " + newDirName);
    }

    public String getRecordDirName() {
        return PrefsHelper.readPrefString(mContext, PREF_DIR_NAME);
    }

    public void changeRecordDirPath(String newDirPath) {
        if (newDirPath == null || newDirPath.isEmpty()) {
            try {
                throw new Exception("newDirPath can not be empty or null");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        PrefsHelper.writePrefString(mContext, PREF_DIR_PATH, newDirPath);
        Log.e("CallRecord", "New dir path: " + newDirPath);
    }

    public String getRecordDirPath() {
        return PrefsHelper.readPrefString(mContext, PREF_DIR_PATH);
    }

    public void changeReceiver(CallRecordReceiver receiver) {
        mCallRecordReceiver = receiver;
    }

    //


    public static class Builder {
        private Context mContext;

        public Builder(Context context) {
            this.mContext = context;
            PrefsHelper.writePrefString(mContext, PREF_FILE_NAME, "Record");
            PrefsHelper.writePrefString(mContext, PREF_DIR_NAME, "CallRecord");
            PrefsHelper.writePrefString(mContext, PREF_DIR_PATH, Environment.getExternalStorageDirectory().getPath());
            PrefsHelper.writePrefInt(mContext, PREF_AUDIO_SOURCE, MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            PrefsHelper.writePrefInt(mContext, PREF_AUDIO_ENCODER, MediaRecorder.AudioEncoder.AMR_NB);
            PrefsHelper.writePrefInt(mContext, PREF_OUTPUT_FORMAT, MediaRecorder.OutputFormat.AMR_NB);
            PrefsHelper.writePrefBool(mContext, PREF_SHOW_SEED, true);
            PrefsHelper.writePrefBool(mContext, PREF_SHOW_PHONE_NUMBER, true);
        }

        public CallRecord build() {
            CallRecord callRecord = new CallRecord(mContext);
            callRecord.enableSaveFile();
            return callRecord;
        }

        public Builder setRecordFileName(String recordFileName) {
            PrefsHelper.writePrefString(mContext, PREF_FILE_NAME, recordFileName);
            return this;
        }

        public String getRecordFileName() {
            return PrefsHelper.readPrefString(mContext, PREF_FILE_NAME);
        }

        public Builder setRecordDirName(String recordDirName) {
            PrefsHelper.writePrefString(mContext, PREF_DIR_NAME, recordDirName);
            return this;
        }

        public String getRecordDirName() {
            return PrefsHelper.readPrefString(mContext, PREF_DIR_NAME);
        }

        public int getAudioSource() {
            return PrefsHelper.readPrefInt(mContext, PREF_AUDIO_SOURCE);
        }

        /**
         * @param audioSource
         * @return
         * @see MediaRecorder.AudioSource
         */
        public Builder setAudioSource(int audioSource) {
            PrefsHelper.writePrefInt(mContext, PREF_AUDIO_SOURCE, audioSource);
            return this;
        }

        public int getAudioEncoder() {
            return PrefsHelper.readPrefInt(mContext, PREF_AUDIO_ENCODER);
        }

        public Builder setAudioEncoder(int audioEncoder) {
            PrefsHelper.writePrefInt(mContext, PREF_AUDIO_ENCODER, audioEncoder);
            return this;
        }

        public int getOutputFormat() {
            return PrefsHelper.readPrefInt(mContext, PREF_OUTPUT_FORMAT);
        }

        public Builder setOutputFormat(int outputFormat) {
            PrefsHelper.writePrefInt(mContext, PREF_OUTPUT_FORMAT, outputFormat);
            return this;
        }

        public boolean isShowSeed() {
            return PrefsHelper.readPrefBool(mContext, PREF_SHOW_SEED);
        }

        public Builder setShowSeed(boolean showSeed) {
            PrefsHelper.writePrefBool(mContext, PREF_SHOW_SEED, showSeed);
            return this;
        }

        public boolean isShowPhoneNumber() {
            return PrefsHelper.readPrefBool(mContext, PREF_SHOW_PHONE_NUMBER);
        }

        public Builder setShowPhoneNumber(boolean showNumber) {
            PrefsHelper.writePrefBool(mContext, PREF_SHOW_PHONE_NUMBER, showNumber);
            return this;
        }

        public String getRecordDirPath() {
            return PrefsHelper.readPrefString(mContext, PREF_DIR_PATH);
        }

        public Builder setRecordDirPath(String recordDirPath) {
            PrefsHelper.writePrefString(mContext, PREF_DIR_PATH, recordDirPath);
            return this;
        }
    }

}
