package com.aykuttasil.callrecord;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.aykuttasil.callrecord.helper.PrefsHelper;
import com.aykuttasil.callrecord.service.CallRecordService;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CallJobService extends JobService {
    private static final String TAG = "CallJobService";

    protected CallRecord mCallRecord;


    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.e("CallJobService", "onStartJob");

        String file_name = PrefsHelper.readPrefString(this, CallRecord.PREF_FILE_NAME);
        String dir_path = PrefsHelper.readPrefString(this, CallRecord.PREF_DIR_PATH);
        String dir_name = PrefsHelper.readPrefString(this, CallRecord.PREF_DIR_NAME);
        boolean show_seed = PrefsHelper.readPrefBool(this, CallRecord.PREF_SHOW_SEED);
        boolean show_phone_number = PrefsHelper.readPrefBool(this, CallRecord.PREF_SHOW_PHONE_NUMBER);
        int output_format = PrefsHelper.readPrefInt(this, CallRecord.PREF_OUTPUT_FORMAT);
        int audio_source = PrefsHelper.readPrefInt(this, CallRecord.PREF_AUDIO_SOURCE);
        int audio_encoder = PrefsHelper.readPrefInt(this, CallRecord.PREF_AUDIO_ENCODER);

        mCallRecord = new CallRecord.Builder(this)
                .setRecordFileName(file_name)
                .setRecordDirName(dir_name)
                .setRecordDirPath(dir_path)
                .setAudioEncoder(audio_encoder)
                .setAudioSource(audio_source)
                .setOutputFormat(output_format)
                .setShowSeed(show_seed)
                .setShowPhoneNumber(show_phone_number)
                .build();

        Log.e(TAG, "mCallRecord.startCallReceiver()");
        mCallRecord.startCallReceiver();
        /*
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), CallRecordService.class);
        getApplicationContext().startService(intent);
        */
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.e("CallJobService", "onStopJob");

        mCallRecord.stopCallReceiver();
        return false;
    }
}
