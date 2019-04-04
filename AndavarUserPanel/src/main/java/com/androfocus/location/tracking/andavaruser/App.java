package com.androfocus.location.tracking.andavaruser;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;

import com.androfocus.location.tracking.andavaruser.receiver.NetworkStateChangeReceiver;
import com.facebook.drawee.backends.pipeline.Fresco;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

public class App extends Application {
  private static final String WIFI_STATE_CHANGE_ACTION = "android.net.wifi.WIFI_STATE_CHANGED";

  @Override
  public void onCreate() {
    super.onCreate();
    registerForNetworkChangeEvents(this);
    Fresco.initialize(this);
  }

  public static void registerForNetworkChangeEvents(final Context context) {
    NetworkStateChangeReceiver networkStateChangeReceiver = new NetworkStateChangeReceiver();
    context.registerReceiver(networkStateChangeReceiver, new IntentFilter(CONNECTIVITY_ACTION));
    context.registerReceiver(networkStateChangeReceiver, new IntentFilter(WIFI_STATE_CHANGE_ACTION));
  }
}
