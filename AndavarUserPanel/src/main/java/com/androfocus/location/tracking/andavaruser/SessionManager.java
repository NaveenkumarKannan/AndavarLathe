package com.androfocus.location.tracking.andavaruser;

/**
 * Created by SANKAR on 4/2/2018.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "PREF";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "supID";

    // Email address (make variable public to access from outside)
    public static final String KEY_PWD= "pwd";

    public static final String KEY_ID = "id";

    public static final String Latitude= "latitude";

    public static final String Longitude = "longitude";

    public static final String Addr = "addr";

    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void create_location(String latitude,String longitude,String addr){
        editor.putString(Latitude, latitude);

        editor.putString(Longitude, longitude);

        editor.putString(Addr, addr);

        // commit changes
        editor.commit();
    }
    public HashMap<String, String> get_location(){
        HashMap<String, String> location = new HashMap<String, String>();
        // user name
        location.put(Latitude, pref.getString(Latitude, null));

        // user email id
        location.put(Longitude, pref.getString(Longitude, null));

        location.put(Addr, pref.getString(Addr, null));

        // return user
        return location;
    }
    /**
     * Create login session
     * */
    public void createLoginSession(String ID,String pwd){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_NAME, ID);

        // Storing email in pref
        editor.putString(KEY_PWD, pwd);

        // commit changes
        editor.commit();
    }

    public void createSession(int id){
        // Storing name in pref
        editor.putInt(KEY_ID, id);

        // commit changes
        editor.commit();
    }
    public int getPrefId(){

        return pref.getInt(KEY_ID, 0);
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }



    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));

        // user email id
        user.put(KEY_PWD, pref.getString(KEY_PWD, null));

        // return user
        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
}