package com.androfocus.location.tracking.andavaruser.admin;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androfocus.location.tracking.andavaruser.R;

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

public class ClientDetailsActivityAdmin extends AppCompatActivity {
    Dialog myDialog;
    String type;
    ProgressDialog loading;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_details_admin);

        myDialog = new Dialog(this);
        
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
    public void goBack(View view) {
        finish();
    }
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_client_details:
                    
                    return true;
                case R.id.navigation_add_client:
                    ShowAddClientPopup(R.layout.add_client_popup_admin);
                    return true;
            }
            return false;
        }
    };

    public void ShowAddClientPopup(int view) {
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

        final EditText etClientName,etClientCompany,etAddr,etPhNo,etEmail;
        Button btnAssignWork;

        etClientName =(EditText) myDialog.findViewById(R.id.etClientName);
        etClientCompany =(EditText) myDialog.findViewById(R.id.etClientCompany);
        etAddr =(EditText) myDialog.findViewById(R.id.etAddr);
        btnAssignWork =(Button) myDialog.findViewById(R.id.btnAssignWork);

        etPhNo =(EditText) myDialog.findViewById(R.id.etPhNo);
        etEmail =(EditText) myDialog.findViewById(R.id.etEmail);
        
        btnAssignWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String clientName, clientCompany, addr,phNo,email;
                clientName = etClientName.getText().toString();
                clientCompany = etClientCompany.getText().toString();
                addr = etAddr.getText().toString();
                phNo = etPhNo.getText().toString();
                email = etEmail.getText().toString();

                if(clientName.trim().length()>0 && clientCompany.trim().length()>0
                        && addr.trim().length()>0
                        && phNo.trim().length()>0 && email.trim().length()>0 ){
                    type = "addClient";
                    loading = ProgressDialog.show(ClientDetailsActivityAdmin.this, "Processing...","Please Wait...",true,true);
                    BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                    backgroundWorker.execute(clientName, clientCompany, addr,phNo,email);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Enter all the Client Details",Toast.LENGTH_LONG).show();
                }

            }
        });

        myDialog.show();
    }
    public class BackgroundWorkerJson extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                String post_data = null;
                String idolUrl = null;
                if(type.equals("addClient")){
                    String clientName, clientCompany, addr,phNo,email;
                    clientName = params[0];
                    clientCompany=params[1];
                    addr = params[2];

                    phNo = params[3];
                    email = params[4];

                    idolUrl = "http://andavarinfo.com/AandavarLathe/Admin/addClient.php";
                    Log.w(type,type );

                    post_data = URLEncoder.encode("clientName", "UTF-8") + "=" + URLEncoder.encode(clientName, "UTF-8")
                            +"&"+URLEncoder.encode("clientCompany", "UTF-8") + "=" + URLEncoder.encode(clientCompany, "UTF-8")
                            +"&"+URLEncoder.encode("addr", "UTF-8") + "=" + URLEncoder.encode(addr, "UTF-8")

                            +"&"+URLEncoder.encode("phNo", "UTF-8") + "=" + URLEncoder.encode(phNo, "UTF-8")
                            +"&"+URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8")
                    ;
                }else if(type.equals("assignWork")){
                    String userName, workTitle, details, deadline,companyName,companyAddr;
                    userName = params[0];
                    workTitle=params[1];
                    details = params[2];
                    deadline = params[3];

                    companyName = params[4];
                    companyAddr = params[5];

                    idolUrl = "http://andavarinfo.com/AandavarLathe/Admin/assignWork.php";
                    Log.w(type,type );

                    post_data = URLEncoder.encode("userName", "UTF-8") + "=" + URLEncoder.encode(userName, "UTF-8")
                            +"&"+URLEncoder.encode("workTitle", "UTF-8") + "=" + URLEncoder.encode(workTitle, "UTF-8")
                            +"&"+URLEncoder.encode("details", "UTF-8") + "=" + URLEncoder.encode(details, "UTF-8")
                            +"&"+URLEncoder.encode("deadline", "UTF-8") + "=" + URLEncoder.encode(deadline, "UTF-8")

                            +"&"+URLEncoder.encode("companyName", "UTF-8") + "=" + URLEncoder.encode(companyName, "UTF-8")
                            +"&"+URLEncoder.encode("companyAddr", "UTF-8") + "=" + URLEncoder.encode(companyAddr, "UTF-8")
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
            Log.w(type,result );
            if(type.equals("addClient")){
                loading.dismiss();
                myDialog.dismiss();
                Toast.makeText(getApplicationContext(),result ,Toast.LENGTH_LONG ).show();
            }else if(type.equals("assignWork")){
                loading.dismiss();
                myDialog.dismiss();
                Toast.makeText(getApplicationContext(),result ,Toast.LENGTH_LONG ).show();

            }
        }
    }
}
