package com.androfocus.location.tracking.andavaruser;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class SampleDeviceAdminReceiver extends DeviceAdminReceiver {
	String TAG = getClass().getSimpleName();
	@Override
	public void onDisabled(Context context, Intent intent) {
		// TODO Auto-generated method stub
		//Toast.makeText(context, "disabled dpm", Toast.LENGTH_SHORT).show();
		Log.e(TAG,"disabled dpm");
		Intent intent1 = new Intent(context,LoginActivity.class);
		intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(intent1);
		super.onDisabled(context, intent);
	}

	@Override
	public void onEnabled(Context context, Intent intent) {
		// TODO Auto-generated method stub
		//Toast.makeText(context, "enabled dpm", Toast.LENGTH_SHORT).show();
		Log.e(TAG,"enabled dpm");
		Intent intent1 = new Intent(context,LoginActivity.class);
		intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(intent1);
		super.onEnabled(context, intent);
	}

	@Override
	public CharSequence onDisableRequested(Context context, Intent intent) {
		// TODO Auto-generated method stub
		//Toast.makeText(context, "disable dpm request", Toast.LENGTH_SHORT).show();
		Log.e(TAG,"disable dpm request");
		Intent intent1 = new Intent(context,LoginActivity.class);
		intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(intent1);
		return super.onDisableRequested(context, intent);
	}

}
