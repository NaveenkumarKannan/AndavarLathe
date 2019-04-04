package com.androfocus.location.tracking.andavaruser.admin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androfocus.location.tracking.andavaruser.R;
import com.androfocus.location.tracking.andavaruser.SessionManager;

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

public class Service_eng extends AppCompatActivity {

    String userId, password, type;
    Dialog myDialog;

    // Session Manager Class
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_eng_admin);

        myDialog = new Dialog(this);

        session = new SessionManager(this);
        HashMap<String, String> user = session.getUserDetails();
        userId = user.get(SessionManager.KEY_NAME);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelperAdmin.disableShiftMode(navigation);
    }
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_location:
                    intent = new Intent(Service_eng.this,TrackedLocationsActivity.class);
                    //Intent intent1 = new Intent(Service_eng.this,RecordedCallsActivity.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return true;
                case R.id.navigation_attendances:
                    intent = new Intent(Service_eng.this,PastAttendanceActivityAdmin.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return true;
                case R.id.navigation_recorded_calls:
                    ShowLoginPopup(R.layout.login_popup);
                    return true;
                case R.id.navigation_task:
                    intent = new Intent(Service_eng.this,CheckINActivityAdmin.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return true;

            }
            return false;
        }
    };
    @Override
    public void onBackPressed() {
        /*
        Intent intent = new Intent(Service_eng.this,MainAdminActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        */
        finish();
    }
    public void goBack(View view) {
        finish();
        /*
        Intent intent = new Intent(Service_eng.this,MainAdminActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        */
    }

    public void ShowLoginPopup(int view) {
        TextView txtclose;
        myDialog.setContentView(view);
        txtclose =(TextView) myDialog.findViewById(R.id.txtclose);
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final EditText etPwd;

        Button btnConfirm;

        etPwd =(EditText) myDialog.findViewById(R.id.etPwd);
        btnConfirm =(Button) myDialog.findViewById(R.id.btnConfirm);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                password = etPwd.getText().toString();

                if(password.trim().length()>0 ){
                    type = "login";
                    BackgroundWorker backgroundWorker = new BackgroundWorker();
                    backgroundWorker.execute();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Enter the Email id",Toast.LENGTH_LONG).show();
                }

            }
        });

        myDialog.show();

    }
    public class BackgroundWorker extends AsyncTask<String,Void,String> {

        android.app.AlertDialog alertDialog;

        @Override
        protected String doInBackground(String... params) {



            try {
                String post_data = null;
                String login_url = null;

                if(type.equals("login")) {
                    login_url = "http://andavarinfo.com/AandavarLathe/Admin/login.php";
                    post_data = URLEncoder.encode("uid","UTF-8")+"="+ URLEncoder.encode(userId,"UTF-8")
                            +"&"+ URLEncoder.encode("passwd","UTF-8")+"="+ URLEncoder.encode(password,"UTF-8")
                    ;
                }

                URL url = new URL(login_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
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
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
                String result="";
                String line="";
                while((line = bufferedReader.readLine())!= null) {
                    result += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

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

            String f = result;
            Log.e("Login", result);
            if(type.equals("login")){

                //builder.setTitle("Login Status");

                if(userId.trim().length() > 0 && password.trim().length() > 0){

                    if(result.charAt(0)=='L')
                    {
                        //      f ="Login Success! Welcome!!!";
                        myDialog.dismiss();

                        Intent intent = new Intent(Service_eng.this,RecordedCallsActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        Toast.makeText(Service_eng.this,"You have successfully logged in" ,Toast.LENGTH_LONG ).show();

                    }else {
                        Toast.makeText(Service_eng.this,"Password is Incorrect" ,Toast.LENGTH_LONG ).show();
                        /*
                        f = "User Id or Password is Incorrect";
                        builder.setNeutralButton("Try Again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                android.support.v7.app.AlertDialog alert1 = builder.create();
                                alert1.cancel();
                            }
                        });
                        */
                    }

                }
                else{
                    // user didn't entered username or password
                    // Show alert asking him to enter the details
                    f="Please enter username and password";
                    Toast.makeText(Service_eng.this,f ,Toast.LENGTH_LONG ).show();

                }

            }

        }


    }

}
