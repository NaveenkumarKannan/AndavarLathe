package com.androfocus.location.tracking.andavaruser;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.androfocus.location.tracking.andavaruser.callrecord.service.CallRecordService;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MJobScheduler extends JobService {
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.e("CallJobService", "onStartJob");
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), CallRecordService.class);
        getApplicationContext().startService(intent);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.e("CallJobService", "onStopJob");
        return false;
    }
}
