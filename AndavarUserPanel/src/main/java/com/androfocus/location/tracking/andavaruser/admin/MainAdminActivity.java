package com.androfocus.location.tracking.andavaruser.admin;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androfocus.location.tracking.andavaruser.Firebase.ActivitySendPushNotification;
import com.androfocus.location.tracking.andavaruser.Firebase.EndPoints;
import com.androfocus.location.tracking.andavaruser.Firebase.MyVolley;
import com.androfocus.location.tracking.andavaruser.R;
import com.androfocus.location.tracking.andavaruser.SessionManager;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainAdminActivity extends AppCompatActivity implements View.OnClickListener {

    Dialog myDialog;
    String type;
    ProgressDialog loading;

    SessionManager session;
    LinearLayout llFact1, llFact2, llFact3, llFact4, llFact5
            ,llServices,llAttendanceTarget,llCCtvLive
            ,llEssl,llEmployeeModule,llClientDetails
            ;
    private static String TAG = MainAdminActivity.class.getSimpleName();

    private Spinner spinner;
    private ProgressDialog progressDialog;
    private List<String> devices;

    String userName, workTitle, details,userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);

        session = new SessionManager(this);

        HashMap<String, String> user = session.getUserDetails();
        userId = user.get(SessionManager.KEY_NAME);

        llFact1 = findViewById(R.id.llFact1);
        llFact2 = findViewById(R.id.llFact2);
        llFact3 = findViewById(R.id.llFact3);
        llFact4 = findViewById(R.id.llFact4);
        llFact5 = findViewById(R.id.llFact5);

        llAttendanceTarget = findViewById(R.id.llAttendanceTarget);
        llCCtvLive = findViewById(R.id.llCCtvLive);
        llEssl = findViewById(R.id.llEssl);
        llServices = findViewById(R.id.llServices);
        llEmployeeModule = findViewById(R.id.llEmployeeModule);
        llClientDetails = findViewById(R.id.llClientDetails);

        llFact1.setOnClickListener(this);
        llFact2.setOnClickListener(this);
        llFact3.setOnClickListener(this);
        llFact4.setOnClickListener(this);
        llFact5.setOnClickListener(this);

        llAttendanceTarget.setOnClickListener(this);
        llCCtvLive.setOnClickListener(this);
        llEssl.setOnClickListener(this);
        llServices.setOnClickListener(this);
        llEmployeeModule.setOnClickListener(this);
        llClientDetails.setOnClickListener(this);

        if(checkAndRequestPermissions()) {

        }

        myDialog = new Dialog(this);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelperAdmin.disableShiftMode(navigation);

        devices = new ArrayList<>();
        loadRegisteredDevices();
    }
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_assign_work:
                    ShowAssignWorkPopup(R.layout.assign_work_popup_admin);
                    return true;
                case R.id.navigation_add_user:
                    ShowAddUserPopup(R.layout.add_user_popup_admin);
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        if(view == llAttendanceTarget){
            startActivity(new Intent(MainAdminActivity.this,AttendencesActivityAdmin.class));
            return;
        }else if(view == llCCtvLive){
            startActivity(new Intent(MainAdminActivity.this,CCtvLiveActivityAdmin.class));
            return;
        }else if(view == llEssl){
            startActivity(new Intent(MainAdminActivity.this,esslAdmin.class));
            return;
        }else if(view == llServices){
            startActivity(new Intent(MainAdminActivity.this,Service_eng.class));
            return;
        }else if(view == llEmployeeModule){
            startActivity(new Intent(MainAdminActivity.this,EmployeeModuleActivityAdmin.class));
            return;
        }else if(view == llClientDetails){
            startActivity(new Intent(MainAdminActivity.this,ClientDetailsActivityAdmin.class));
            return;
        }
    }

    //method to load all the devices from database
    private void loadRegisteredDevices() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Devices...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoints.URL_FETCH_DEVICES,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        JSONObject obj = null;
                        try {
                            obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                JSONArray jsonDevices = obj.getJSONArray("devices");

                                for (int i = 0; i < jsonDevices.length(); i++) {
                                    JSONObject d = jsonDevices.getJSONObject(i);
                                    devices.add(d.getString("email"));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {

        };
        MyVolley.getInstance(this).addToRequestQueue(stringRequest);
    }
    private void sendSinglePush() {
        final String title = workTitle;
        final String message = details;
        final String image = " ";
        final String email = userName;

        progressDialog.setMessage("Assigning Task...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoints.URL_SEND_SINGLE_PUSH,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        Log.e("VolleyResponse",response );
                        Toast.makeText(MainAdminActivity.this,"The task is assigned to the user." , Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VolleyError",error.getMessage() );
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("title", title);
                params.put("message", message);

                if (!TextUtils.isEmpty(image))
                    params.put("image", image);

                params.put("email", email);
                return params;
            }
        };

        MyVolley.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    private  boolean checkAndRequestPermissions() {
        int record_audio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int process_outgoing_calls = ContextCompat.checkSelfPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS);
        int read_phone_state = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int storage2= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (record_audio != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (read_phone_state != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (process_outgoing_calls != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        }
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (storage2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.w(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.PROCESS_OUTGOING_CALLS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);

                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                            &&perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

                            &&perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Log.w(TAG, "All permissions are granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.w(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the firstAdmin time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.PROCESS_OUTGOING_CALLS)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)

                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                ) {
                            showDialogOK("Storage, Phone and MicroPhone Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    public void ShowAssignWorkPopup(int view) {
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

        final EditText etUserName,etWorkTitle,etDetails,etCompanyName,etCompanyAddr;
        final TextView tvDeadline;
        Button btnAssignWork;

        spinner = (Spinner) myDialog.findViewById(R.id.spinnerDevices);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                MainAdminActivity.this,
                //android.R.layout.simple_spinner_dropdown_item,
                R.layout.add_user_spinner_item,
                devices);

        spinner.setAdapter(arrayAdapter);
        etUserName =(EditText) myDialog.findViewById(R.id.etUserName);
        etWorkTitle =(EditText) myDialog.findViewById(R.id.etWorkTitle);
        etDetails =(EditText) myDialog.findViewById(R.id.etDetails);
        tvDeadline=(TextView) myDialog.findViewById(R.id.tvDeadline);
        btnAssignWork =(Button) myDialog.findViewById(R.id.btnAssignWork);

        etCompanyName =(EditText) myDialog.findViewById(R.id.etCompanyName);
        etCompanyAddr =(EditText) myDialog.findViewById(R.id.etCompanyAddr);

        tvDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int day, month, year;
                Calendar calendar = Calendar.getInstance();
                day = calendar.get(Calendar.DAY_OF_MONTH);
                month = calendar.get(Calendar.MONTH);
                year = calendar.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(MainAdminActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        NumberFormat numberFormat = new DecimalFormat("00");
                        String date = String.valueOf(numberFormat.format(i2))+"-"+String.valueOf(numberFormat.format(i1+1))+"-"+String.valueOf(i);
                        tvDeadline.setText(date);
                    }
                },year,month,day);
                datePickerDialog.show();

            }
        });
        btnAssignWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deadline,companyName,companyAddr;
                userName = spinner.getSelectedItem().toString();
                workTitle = etWorkTitle.getText().toString();
                details = etDetails.getText().toString();
                deadline = tvDeadline.getText().toString();
                companyName = etCompanyName.getText().toString();
                companyAddr = etCompanyAddr.getText().toString();

                if(userName.trim().length()>0 && workTitle.trim().length()>0
                        && details.trim().length()>0 && deadline.trim().length()>0
                        && companyName.trim().length()>0 && companyAddr.trim().length()>0 ){
                    type = "assignWork";
                    loading = ProgressDialog.show(MainAdminActivity.this, "Processing...","Please Wait...",true,true);
                    BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                    backgroundWorker.execute(userName, workTitle, details, deadline,companyName,companyAddr);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Enter all the User details",Toast.LENGTH_LONG).show();
                }
            }
        });

        myDialog.show();
    }
    public void ShowAddUserPopup(int view) {
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

        final EditText etUserName,etPwd,etConfirmPwd,etPhNo,etEmail,etAddr,etPanNo;
        Button btnAddUser;
        final String[] admin = new String[1];
        etUserName =(EditText) myDialog.findViewById(R.id.etUserName);
        etPwd =(EditText) myDialog.findViewById(R.id.etPwd);
        etConfirmPwd =(EditText) myDialog.findViewById(R.id.etConfirmPwd);

        etPhNo =(EditText) myDialog.findViewById(R.id.etPhNo);
        etEmail =(EditText) myDialog.findViewById(R.id.etEmail);
        etAddr =(EditText) myDialog.findViewById(R.id.etAddr);
        etPanNo =(EditText) myDialog.findViewById(R.id.etPanNo);

        Spinner spinner = (Spinner)  myDialog.findViewById(R.id.idSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.userType,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String adminType = adapterView.getItemAtPosition(i).toString();

                if(adminType.equals("Super Admin")){
                    admin[0] = "1";
                }else if(adminType.equals("Admin")){
                    admin[0] = "2";
                }else if(adminType.equals("Employee")){
                    admin[0] = "3";
                }else if(adminType.equals("Marketing Person")){
                    admin[0] = "4";
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        btnAddUser =(Button) myDialog.findViewById(R.id.btnAddUser);


        btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName, pwd, confirmPwd,phNo,email,addr,panNo;
                userName = etUserName.getText().toString();
                pwd = etPwd.getText().toString();
                confirmPwd = etConfirmPwd.getText().toString();

                phNo = etPhNo.getText().toString();
                email = etEmail.getText().toString();
                addr = etAddr.getText().toString();
                panNo = etPanNo.getText().toString();
                if(userName.trim().length()>0 && pwd.trim().length()>0
                        && confirmPwd.trim().length()>0

                        && phNo.trim().length()>0
                        && email.trim().length()>0
                        && addr.trim().length()>0
                        && panNo.trim().length()>0
                        && admin[0].trim().length()>0){
                    if(pwd.equals(confirmPwd)){
                        type = "addUser";
                        loading = ProgressDialog.show(MainAdminActivity.this, "Processing...","Please Wait...",true,true);
                        BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                        backgroundWorker.execute(userName, pwd, confirmPwd,phNo,email,addr,panNo, admin[0]);
                    }else {
                        Toast.makeText(getApplicationContext(),"Your password does not match...",Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(),"Enter all the User details",Toast.LENGTH_LONG).show();
                }

            }
        });

        myDialog.show();
    }

    public void openOptionsMenu(View view) {
        PopupMenu popup = new PopupMenu(getApplicationContext(), view);
        popup.getMenuInflater().inflate(R.menu.logout_menu,
                popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_logout:
                        type = "Logout";
                        BackgroundWorkerJson backgroundWorker1 = new BackgroundWorkerJson();
                        backgroundWorker1.execute();
                        break;

                    default:
                        break;
                }

                return true;
            }
        });
    }
    public class BackgroundWorkerJson extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                String post_data = null;
                String idolUrl = null;
                if(type.equals("addUser")){
                    String userName, pwd, confirmPwd,phNo,email,addr,panNo,admin;
                    userName = params[0];
                    pwd=params[1];
                    confirmPwd = params[2];

                    phNo = params[3];
                    email = params[4];
                    addr = params[5];
                    panNo = params[6];
                    admin = params[7];

                    idolUrl = "http://andavarinfo.com/AandavarLathe/Admin/add_user.php";
                    Log.w(type,type );

                    post_data = URLEncoder.encode("userName", "UTF-8") + "=" + URLEncoder.encode(userName, "UTF-8")
                            +"&"+URLEncoder.encode("pwd", "UTF-8") + "=" + URLEncoder.encode(pwd, "UTF-8")
                            +"&"+URLEncoder.encode("confirmPwd", "UTF-8") + "=" + URLEncoder.encode(confirmPwd, "UTF-8")

                            +"&"+URLEncoder.encode("phNo", "UTF-8") + "=" + URLEncoder.encode(phNo, "UTF-8")
                            +"&"+URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8")
                            +"&"+URLEncoder.encode("addr", "UTF-8") + "=" + URLEncoder.encode(addr, "UTF-8")
                            +"&"+URLEncoder.encode("panNo", "UTF-8") + "=" + URLEncoder.encode(panNo, "UTF-8")
                            +"&"+URLEncoder.encode("admin", "UTF-8") + "=" + URLEncoder.encode(admin, "UTF-8")
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
                }else if(type.equals("Logout")){
                    idolUrl = "http://andavarinfo.com/AandavarLathe/LoginSetStatus.php";
                    Log.e(type,type );
                    post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
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
            if(type.equals("addUser")){
                loading.dismiss();
                myDialog.dismiss();
                Toast.makeText(getApplicationContext(),result ,Toast.LENGTH_LONG ).show();
            }else if(type.equals("assignWork")){
                loading.dismiss();
                myDialog.dismiss();
                sendSinglePush();
                Toast.makeText(getApplicationContext(),result ,Toast.LENGTH_LONG ).show();

            }else if(type.equals("Logout")){
                session.logoutUser();
                Toast.makeText(getApplicationContext(),"You have logged out successfully" ,Toast.LENGTH_LONG ).show();

            }
        }
    }

}
