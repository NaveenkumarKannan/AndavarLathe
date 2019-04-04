package com.androfocus.location.tracking.andavaruser.admin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androfocus.location.tracking.andavaruser.R;
import com.androfocus.location.tracking.andavaruser.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class ProfileAdminActivity extends AppCompatActivity {

    String type;
    SessionManager session;
    String userId,pwd;
    TextView tvProfileName,tvProfileEmail, tvProfilePhNo, tvProfileDesignation,tvProfileShiftTime,
            tvCompanyName
    ;
    Dialog myDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_admin);

        session = new SessionManager(getApplicationContext());
        //Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();

        HashMap<String, String> user = session.getUserDetails();
        userId = user.get(SessionManager.KEY_NAME);
        pwd = user.get(SessionManager.KEY_PWD);

        type = "getProfile";
        BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
        backgroundWorker.execute();

        myDialog = new Dialog(this);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhNo = findViewById(R.id.tvProfilePhNo);
        tvProfileDesignation = findViewById(R.id.tvProfileDesignation);
        tvProfileShiftTime = findViewById(R.id.tvProfileShiftTime);
        tvCompanyName = findViewById(R.id.tvCompanyName);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                TextView txtclose;
                int rID = R.layout.edit_profile_popup;
                myDialog.setContentView(rID);
                txtclose =(TextView) myDialog.findViewById(R.id.txtclose);
                txtclose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myDialog.dismiss();
                    }
                });
                myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                final EditText etPhNo, etEmail;
                Button btnEditProfile;

                etPhNo =(EditText) myDialog.findViewById(R.id.etPhNo);
                etEmail =(EditText) myDialog.findViewById(R.id.etEmail);

                btnEditProfile =(Button) myDialog.findViewById(R.id.btnEditProfile);

                etEmail.setText( tvProfileEmail.getText().toString());
                etPhNo.setText( tvProfilePhNo.getText().toString());
                btnEditProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String PhNo, Email;
                        PhNo = etPhNo.getText().toString();
                        Email = etEmail.getText().toString();

                        if(PhNo.trim().length()>0
                                && Email.trim().length()>0){

                            type = "editProfile";
                            BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                            backgroundWorker.execute(PhNo, Email);
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Enter both details",Toast.LENGTH_LONG).show();
                        }

                    }
                });

                myDialog.show();
            }
        });
    }
    public void goBack(View view) {
        finish();
    }

    public void onChangePwd(View v) {
        TextView txtclose;
        int view = R.layout.change_pwd_popup;
        myDialog.setContentView(view);
        txtclose =(TextView) myDialog.findViewById(R.id.txtclose);
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final EditText etOldPwd, etNewPwd, etConfirmNewPwd;
        Button btnChangePwd;

        etOldPwd =(EditText) myDialog.findViewById(R.id.etOldPwd);
        etNewPwd =(EditText) myDialog.findViewById(R.id.etNewPwd);
        etConfirmNewPwd =(EditText) myDialog.findViewById(R.id.etConfirmNewPwd);
        btnChangePwd =(Button) myDialog.findViewById(R.id.btnChangePwd);

        btnChangePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPwd, newPwd, confirmPwd;
                oldPwd = etOldPwd.getText().toString();
                newPwd = etNewPwd.getText().toString();
                confirmPwd = etConfirmNewPwd.getText().toString();

                if(oldPwd.trim().length()>0 && newPwd.trim().length()>0
                         && confirmPwd.trim().length()>0){
                    if(pwd.equals(oldPwd)){
                        if(newPwd.equals(confirmPwd)){
                            type = "changePwd";
                            BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                            backgroundWorker.execute(newPwd, confirmPwd);
                        }else {
                            Toast.makeText(getApplicationContext(),"Your new password does not match...",Toast.LENGTH_LONG).show();
                        }
                    }else {
                        Toast.makeText(getApplicationContext(),"Your old password is incorrect...",Toast.LENGTH_LONG).show();
                    }

                }
                else{
                    Toast.makeText(getApplicationContext(),"Enter all the details",Toast.LENGTH_LONG).show();
                }

            }
        });

        myDialog.show();
    }

    public class BackgroundWorkerJson extends AsyncTask<String,Void,String> {
        String json_string;
        JSONArray jsonArray;
        JSONObject jsonObject;

        @Override
        protected String doInBackground(String... params) {

            try {
                String post_data = null;
                String idolUrl = null;
                if(type.equals("getProfile")){
                    idolUrl = "http://andavarinfo.com/AandavarLathe/getProfile.php";
                    Log.e(type,type );
                    
                    post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
                    ;
                }else if(type.equals("changePwd")){
                    String newPwd, confirmPwd;
                    newPwd = params[0];
                    confirmPwd=params[1];

                    idolUrl = "http://andavarinfo.com/AandavarLathe/changePwd.php";
                    Log.e(type,type );

                    post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
                            +"&"+URLEncoder.encode("newPwd", "UTF-8") + "=" + URLEncoder.encode(newPwd, "UTF-8")
                            +"&"+URLEncoder.encode("confirmPwd", "UTF-8") + "=" + URLEncoder.encode(confirmPwd, "UTF-8")
                    ;
                }else if(type.equals("Logout")){
                    idolUrl = "http://andavarinfo.com/AandavarLathe/LoginSetStatus.php";
                    Log.e(type,type );
                    post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
                    ;
                }else if(type.equals("editProfile")){
                    String phNo, email;
                    phNo = params[0];
                    email=params[1];

                    idolUrl = "http://andavarinfo.com/AandavarLathe/editProfile.php";
                    Log.e(type,type );

                    post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
                            +"&"+URLEncoder.encode("phNo", "UTF-8") + "=" + URLEncoder.encode(phNo, "UTF-8")
                            +"&"+URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8")
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

            if(type.equals("getProfile")){
                Log.e(type,result );
                json_string = result;

                if(json_string != null)
                {
                    try {
                        jsonObject = new JSONObject(json_string);
                        jsonArray = jsonObject.getJSONArray("getProfile");

                        String person_name = null,person_email = null,phn_no = null,Shift = null,designation = null,company_name = null;
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jo = jsonArray.getJSONObject(i);

                            person_name = jo.getString("person_name");
                            person_email = jo.getString("person_email");
                            phn_no = jo.getString("phn_no");
                            designation = jo.getString("designation");
                            Shift = jo.getString("shift_start")  +" - "+ jo.getString("shift_end");
                            company_name = jo.getString("company_name");
                            
                        }
                        tvProfileName.setText(person_name);
                        tvProfileEmail.setText(person_email);
                        tvProfileDesignation.setText(designation);
                        tvProfilePhNo.setText(phn_no);
                        tvProfileShiftTime.setText(Shift);
                        tvCompanyName.setText(company_name);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else {
                    Log.e("JSON","null" );
                }
            }else if(type.equals("changePwd")){
                Toast.makeText(getApplicationContext(),result ,Toast.LENGTH_LONG ).show();
                if(result.charAt(0)=='Y'){
                    type = "Logout";
                    BackgroundWorkerJson backgroundWorker1 = new BackgroundWorkerJson();
                    backgroundWorker1.execute();
                }

            }else if(type.equals("Logout")){
                session.logoutUser();
                Toast.makeText(getApplicationContext(),"Please Login again using your new password..." ,Toast.LENGTH_LONG ).show();
            }else if(type.equals("editProfile")){
                Toast.makeText(getApplicationContext(),result ,Toast.LENGTH_LONG ).show();
                myDialog.cancel();
                Intent intent = new Intent(ProfileAdminActivity.this,ProfileAdminActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }
}
