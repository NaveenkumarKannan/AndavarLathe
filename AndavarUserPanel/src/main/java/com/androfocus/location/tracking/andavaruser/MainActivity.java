package com.androfocus.location.tracking.andavaruser;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.androfocus.location.tracking.SlideSideMenuTransitionLayout;
import com.androfocus.location.tracking.andavaruser.admin.MainAdminActivity;
import com.androfocus.location.tracking.andavaruser.receiver.NetworkStateChangeReceiver;
import com.androfocus.location.tracking.andavaruser.callrecord.CallRecord;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.androfocus.location.tracking.andavaruser.receiver.NetworkStateChangeReceiver.IS_NETWORK_AVAILABLE;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{

    private static final String TAG = "MainLocationService";

    private boolean currentlyProcessingLocation = false;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;

    private static final long INTERVAL = 1000;
    private static final long FASTEST_INTERVAL = 1000;
    Double latitude, longitude;
    String currentAddr;

    private SlideSideMenuTransitionLayout mSlideSideMenu;
    private Toolbar mToolbar;
    private ActionBar toolbar;
    Dialog myDialog;
    AssignWorkAdapter assignWorkAdapter;
    ListView listView;
    String type;


    TextView tvHome, tvCheckIN, tvExpenses, tvAttendances, tvLocationTrack, tvProfile, tvCollection, tvLogout,
            tvCheckInCount,tvExpensesSumAmount,tvDayPresent,
            tvNoWork,tvCompanyName,tvAdmin
            ;

    /*
    private LocationManager locationManager;
    private LocationListener listener;
    Double latitude, longitude;
    Location location;
    public Criteria criteria;
    public String bestProvider;
    */

    private int PICK_IMAGE = 100,CAPTURE_IMAGE = 101;
    final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100,MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101, CAMERA_PERMISSION_REQ = 102;

    int LocationRequestCode = 200;
    Bitmap bitmap = null;
    File videoFile;
    String bitmapPath;
    TextView tvPhoto,tvVideo;
    //VideoView videoView;
    //ImageView imageView;
    SessionManager session;
    String userId,batteryPercentage,date;
    ProgressDialog loading;

    private BroadcastReceiver mBatInfoReceiver= new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            batteryPercentage = String.valueOf(level) + "%";
            unregisterReceiver(mBatInfoReceiver);
            Log.e("Battery",batteryPercentage);
        }
    };

    CallRecord callRecord;
    static final int REQUEST_VIDEO_CAPTURE = 1;

    private static final long ALARM_INTERVAL = 60000*3;
    AlarmManager alarms;
    PendingIntent recurringLl24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        IntentFilter intentFilter = new IntentFilter(NetworkStateChangeReceiver.NETWORK_AVAILABLE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isNetworkAvailable = intent.getBooleanExtra(IS_NETWORK_AVAILABLE, false);
                String networkStatus = isNetworkAvailable ? "connected" : "disconnected";

                Snackbar.make(findViewById(R.id.slide_side_menu), "Network Status: " + networkStatus, Snackbar.LENGTH_LONG).show();
            }
        }, intentFilter);

        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else
            connected = false;

        if(connected == false)
        {
            Intent intent = new Intent(MainActivity.this,Internet_Connection.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finish();
        }

        listView = (ListView) findViewById(R.id.lvAssignedWork);
        tvHome = findViewById(R.id.tvHome);
        tvCheckIN = findViewById(R.id.tvCheckIN);
        tvExpenses = findViewById(R.id.tvExpenses);
        tvAttendances = findViewById(R.id.tvAttendances);
        tvLocationTrack = findViewById(R.id.tvLocationTrack);
        tvProfile = findViewById(R.id.tvProfile);
        tvCollection = findViewById(R.id.tvCollection);
        tvLogout = findViewById(R.id.tvLogout);
        tvDayPresent = findViewById(R.id.tvDayPresent);
        tvCheckInCount = findViewById(R.id.tvCheckInCount);
        tvExpensesSumAmount = findViewById(R.id.tvExpensesSumAmount);
        tvNoWork = findViewById(R.id.tvNoWork);
        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvAdmin = findViewById(R.id.tvAdmin);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.e("listView","pos"+position );
                final int pos=position;
                final TextView tvDetail,tvTitle,tvWorkType,tvCollectionAmt,tvWorkId,tvCompanyName,tvCompanyAddr;
                tvTitle = (TextView) view.findViewById(R.id.tvTitle);
                tvDetail = (TextView) view.findViewById(R.id.tvDetail);
                tvCollectionAmt = (TextView) view.findViewById(R.id.tvCollectionAmt);
                tvWorkType = (TextView) view.findViewById(R.id.tvWorkType);
                tvWorkId = view.findViewById(R.id.tvWorkId);
                tvCompanyName = view.findViewById(R.id.tvCompanyName);
                tvCompanyAddr = view.findViewById(R.id.tvCompanyAddr);

                final String Name,Details,workType,collectionAmt,workId,companyName,companyAddr;
                Name = tvTitle.getText().toString();
                Details = tvDetail.getText().toString();
                workType = tvWorkType.getText().toString();
                collectionAmt = tvCollectionAmt.getText().toString();
                workId = tvWorkId.getText().toString();
                companyName = tvCompanyName.getText().toString();
                companyAddr = tvCompanyAddr.getText().toString();

                ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
                imageView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.imageView:

                                Log.e("imageview", "clicked");
                                if(workType.equals("Collection")){
                                    Log.e("menu", "collection");
                                    PopupMenu popup = new PopupMenu(getApplicationContext(), view);
                                    popup.getMenuInflater().inflate(R.menu.assign_menu1,
                                            popup.getMenu());
                                    popup.show();
                                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem item) {
                                            //loading = ProgressDialog.show(MainActivity.this, "Processing...","Please Wait...",true,true);
                                            switch (item.getItemId()) {
                                                case R.id.assign_convert_to_collection:

                                                    int diaview = R.layout.collection_popup;
                                                    TextView txtclose;
                                                    myDialog.setContentView(diaview);
                                                    txtclose =(TextView) myDialog.findViewById(R.id.txtclose);
                                                    txtclose.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            myDialog.dismiss();
                                                        }
                                                    });
                                                    myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                                                    final EditText tvName, tvDetails, tvAmount;
                                                    final TextView tvDate;
                                                    Button btnCollection;

                                                    tvName =(EditText) myDialog.findViewById(R.id.tvName);
                                                    tvDetails =(EditText) myDialog.findViewById(R.id.tvDetails);
                                                    tvDate =(TextView) myDialog.findViewById(R.id.tvDate);
                                                    tvAmount=(EditText) myDialog.findViewById(R.id.tvAmount);
                                                    btnCollection =(Button) myDialog.findViewById(R.id.btnCollection);

                                                    tvName.setText(Name);
                                                    tvDetails.setText(Details);
                                                    tvAmount.setText(collectionAmt);

                                                    Date c = Calendar.getInstance().getTime();
                                                    System.out.println("Current time => " + c);

                                                    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                                                    String formattedDate = df.format(c);
                                                    tvDate.setText(formattedDate);
                                                    Log.e("Date",formattedDate );
                                                    tvDate.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            int day, month, year;
                                                            Calendar calendar = Calendar.getInstance();
                                                            day = calendar.get(Calendar.DAY_OF_MONTH);
                                                            month = calendar.get(Calendar.MONTH);
                                                            year = calendar.get(Calendar.YEAR);

                                                            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                                                                @Override
                                                                public void onDateSet(DatePicker datePicker, int iYear, int iMonth, int iDay) {
                                                                    NumberFormat numberFormat = new DecimalFormat("00");
                                                                    String day,month,year;
                                                                    day = String.valueOf(numberFormat.format(iDay));
                                                                    month = String.valueOf(numberFormat.format(iMonth+1));
                                                                    year = String.valueOf(iYear);
                                                                    date = day+"-"+month+"-"+year;
                                                                    tvDate.setText(date);
                                                                }
                                                            },year,month,day);
                                                            datePickerDialog.show();

                                                        }
                                                    });
                                                    btnCollection.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            String Name, Details, Date, Amount;
                                                            Name = tvName.getText().toString();
                                                            Details = tvDetails.getText().toString();
                                                            Date = tvDate.getText().toString();
                                                            Amount = tvAmount.getText().toString();

                                                            if(Name.trim().length()>0 && Details.trim().length()>0
                                                                    && Date.trim().length()>0 && Amount.trim().length()>0){
                                                                type = "Collection";
                                                                loading = ProgressDialog.show(MainActivity.this, "Processing...","Please Wait...",true,true);
                                                                BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                                                                backgroundWorker.execute(Name, Details, Date, Amount);
                                                            }
                                                            else{
                                                                Toast.makeText(getApplicationContext(),"Enter all the Collection details",Toast.LENGTH_LONG).show();
                                                            }

                                                        }
                                                    });

                                                    myDialog.show();

                                                    break;
                                                case R.id.assign_mark_as_done:
                                                    type = "collection-Assign";
                                                    BackgroundWorkerJson backgroundWorker1 = new BackgroundWorkerJson();
                                                    backgroundWorker1.execute(Name,Details,collectionAmt);

                                                    assignWorkAdapter.list.remove(pos);
                                                    break;

                                                default:
                                                    break;
                                            }

                                            return true;
                                        }
                                    });
                                }else if(workType.equals("Work")){
                                    Log.e("menu", "work");
                                    PopupMenu popup = new PopupMenu(getApplicationContext(), view);
                                    popup.getMenuInflater().inflate(R.menu.assign_menu,
                                            popup.getMenu());
                                    popup.show();
                                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem item) {
                                            switch (item.getItemId()) {
                                                case R.id.assign_convert_to_checkIn:

                                                    if(currentAddr == null){
                                                        //    Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                                                        //         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        //  startActivityForResult(intent, 2);
                                                        startTracking();
                                                        Toast.makeText(MainActivity.this,"Location not found. Wait for a minute and try again." , Toast.LENGTH_LONG).show();
                                                    }
                                                    else {
                                                        int diaview = R.layout.check_in_popup;
                                                        TextView txtclose;
                                                        myDialog.setContentView(diaview);
                                                        txtclose =(TextView) myDialog.findViewById(R.id.txtclose);
                                                        txtclose.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                myDialog.dismiss();
                                                            }
                                                        });
                                                        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                                                        final EditText tvName, tvDetails;
                                                        final TextView tvLocation;

                                                        TextView tvCompanyName,tvCompanyAddr,tvTitle;
                                                        LinearLayout llPopup;
                                                        llPopup = myDialog.findViewById(R.id.llPopup);
                                                        llPopup.setVisibility(View.VISIBLE);

                                                        tvTitle = myDialog.findViewById(R.id.tvTitle);
                                                        tvCompanyName = myDialog.findViewById(R.id.tvCompanyName);
                                                        tvCompanyAddr = myDialog.findViewById(R.id.tvCompanyAddr);

                                                        tvTitle.setText(Name);
                                                        tvCompanyName.setText(companyName);
                                                        tvCompanyAddr.setText(companyAddr);

                                                        Button btnCheckIn;

                                                        tvName =(EditText) myDialog.findViewById(R.id.tvName);
                                                        tvDetails =(EditText) myDialog.findViewById(R.id.tvDetails);
                                                        tvLocation =(TextView) myDialog.findViewById(R.id.tvLocation);
                                                        tvPhoto =(TextView) myDialog.findViewById(R.id.tvPhoto);
                                                        tvVideo = (TextView) myDialog.findViewById(R.id.tvVideo);
                                                        //videoView = myDialog.findViewById(R.id.videoView);
                                                        btnCheckIn =(Button) myDialog.findViewById(R.id.btnCheckIn);

                                                        //imageView =(ImageView) myDialog.findViewById(R.id.image_view);

                                                        tvLocation.setText(currentAddr);

                                                        tvPhoto.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                openCameraPhoto();
                                                                //dispatchTakeVideoIntent();
                /*
                AlertDialog.Builder notifyLocationServices = new AlertDialog.Builder(MainActivity.this);
                notifyLocationServices.setTitle("Choose the option");
                notifyLocationServices.setMessage("Select");
                notifyLocationServices.setPositiveButton("Galary", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openGallery();
                    }
                });
                notifyLocationServices.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openCameraPhoto();
                    }
                });
                notifyLocationServices.show();
                */
                                                            }
                                                        });
                                                        tvVideo.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                //openCameraPhoto();
                                                                dispatchTakeVideoIntent(0);
                /*
                AlertDialog.Builder notifyLocationServices = new AlertDialog.Builder(MainActivity.this);
                notifyLocationServices.setTitle("Choose the option");
                notifyLocationServices.setMessage("Select");
                notifyLocationServices.setPositiveButton("Galary", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openGallery();
                    }
                });
                notifyLocationServices.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openCameraPhoto();
                    }
                });
                notifyLocationServices.show();
                */
                                                            }
                                                        });

                                                        if(bitmap!=null){
                                                            tvPhoto.setText(bitmapPath);
                                                            Log.e("Popup","not null" );
                                                            //imageView.setImageBitmap(bitmap);
                                                        }
                                                        else
                                                            Log.e("Popup "," null" );

                                                        btnCheckIn.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                String Name, Details, Location;
                                                                Name = tvName.getText().toString();
                                                                Details = tvDetails.getText().toString();
                                                                //Details = "Details";
                                                                Location = tvLocation.getText().toString();


                                                                if(Name.trim().length()>0 && Details.trim().length()>0
                                                                        && Location.trim().length()>0 ){
                                                                    if(bitmap!=null){
                                                                        loading = ProgressDialog.show(MainActivity.this, "Processing...","Please Wait...",true,true);
                                                                        type = "checkIn-Assign";
                                                                        String status = "finished";
                                                                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
                                                                        byte[] byteArray = byteArrayOutputStream .toByteArray();
                                                                        String photo = Base64.encodeToString(byteArray, Base64.DEFAULT);
                                                                        BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                                                                        backgroundWorker.execute(Name, Location,Details,status,photo,workId);
                                                                    }else {
                                                                        Toast.makeText(getApplicationContext(),"Select or Capture an Image",Toast.LENGTH_LONG).show();
                                                                    }

                                                                }
                                                                else{
                                                                    Toast.makeText(getApplicationContext(),"Enter all the check in details",Toast.LENGTH_LONG).show();
                                                                }

                                                            }
                                                        });

                                                        myDialog.show();
                                                    }
                                                    break;

                                                default:
                                                    break;
                                            }

                                            return true;
                                        }
                                    });
                                }


                                break;

                            default:
                                break;
                        }
                    }
                });
            }
        });


        tvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(this,MainActivity.class);
                mSlideSideMenu.toggle();
            }
        });
        tvCheckIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CheckINActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        tvExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ExpensesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        tvAttendances.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AttendanceActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        tvLocationTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LocationTrackActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        tvProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        tvCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CollectionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "Logout";
                BackgroundWorkerJson backgroundWorker1 = new BackgroundWorkerJson();
                backgroundWorker1.execute();

            }
        });
        tvAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainAdminActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        myDialog = new Dialog(this);
        // Grab the widget
        mSlideSideMenu = (SlideSideMenuTransitionLayout) findViewById(R.id.slide_side_menu);
        //       ImageView mimageView = (ImageView) findViewById(R.id.image_view);
        //     ImageView mimageView1 = (ImageView) findViewById(R.id.image_view1);

        // Setup the toolbar
        mToolbar = (Toolbar) findViewById(R.id.base_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextColor(Color.WHITE);

        // Wire SideMenu with Toolbar
        mToolbar.setNavigationIcon(R.drawable.ic_menu);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlideSideMenu.toggle();
            }
        });

        toolbar = getSupportActionBar();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if(checkAndRequestPermissions()) {
            // carry on the normal flow, as the case of  permissions  granted.


            if(connected){

                //By using Broadcast
                //Intent ll24 = new Intent(this, AlarmReceiver.class);
                //PendingIntent recurringLl24 = PendingIntent.getBroadcast(this, 0, ll24, PendingIntent.FLAG_CANCEL_CURRENT);
                //By using service
                Intent ll24 = new Intent(MainActivity.this, ServiceAlarm.class);
                recurringLl24 = PendingIntent.getService(MainActivity.this, 0, ll24, PendingIntent.FLAG_CANCEL_CURRENT);
                alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                Log.e("Alarm", "Started...");
                long currentTimeMillis = System.currentTimeMillis();
                //alarms.setRepeating(AlarmManager.RTC_WAKEUP, currentTimeMillis, ALARM_INTERVAL, recurringLl24); // Log repetition

                session = new SessionManager(getApplicationContext());
                session.createSession(0);
                //Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();

                HashMap<String, String> user = session.getUserDetails();
                userId = user.get(SessionManager.KEY_NAME);

                assignWorkAdapter = new AssignWorkAdapter(this, R.layout.assign_work_row_layout);


                loading = ProgressDialog.show(this, "Processing...","Please Wait...",true,true);

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        type = "assign";
                        BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                        backgroundWorker.execute();
                    }
                });

                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();


        /*
        Bitmap mbitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.walk)).getBitmap();
        Bitmap imageRounded = Bitmap.createBitmap(mbitmap.getWidth(), mbitmap.getHeight(), mbitmap.getConfig());
        Canvas canvas = new Canvas(imageRounded);
        Paint mpaint = new Paint();
        mpaint.setAntiAlias(true);
        mpaint.setShader(new BitmapShader(mbitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas.drawCircle(360, 360, 300, mpaint);
        //canvas.drawRoundRect((new RectF(0, 0, mbitmap.getWidth(), mbitmap.getHeight())), 100, 100, mpaint);// Round Image Corner 100 100 100 100
        mimageView.setImageBitmap(imageRounded);
        mimageView1.setImageBitmap(imageRounded);
        */

        /*
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                MainActivity.this.location = location;

                longitude = location.getLongitude();
                latitude = location.getLatitude();
                String latlng = "geo:" + latitude + "," + longitude;
                Log.e("Location", latlng);

                Geocoder geoCoder = new Geocoder(MainActivity.this, Locale.getDefault());
                StringBuilder builder = new StringBuilder();
                try {
                    List<Address> address = geoCoder.getFromLocation(longitude, latitude, 1);

                    String finalAddress;
                    finalAddress = address.get(0).getAddressLine(0)
//                            +"\n"+address.get(0).getLocality()
  //                          +"\n"+address.get(0).getAdminArea()
    //                        +"\n"+address.get(0).getCountryName()
      //                      +"\n"+address.get(0).getPostalCode()
                    ;

                    Log.e("Location", "lat "+latitude+"\n"+"lon"+longitude+"\nFinal Address: "+finalAddress);
                } catch (IOException e) {
                    // Handle IOException
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    // Handle NullPointerException
                    e.printStackTrace();
                }
                locationManager.removeUpdates(listener);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };
        */
                getLocation();

                //get Battery Percentage
                this.registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                callRecordStart();
            }
        }
    }

    private void callRecordStart() {

        int source;
        //int source = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
        if(android.os.Build.VERSION.SDK_INT >= 23)
            Log.e("callRecordStart()", "Mic");
        else {
            source = MediaRecorder.AudioSource.VOICE_CALL;
            Log.e("callRecordStart()", "voice call");
        }
        source = MediaRecorder.AudioSource.MIC;
        callRecord = new CallRecord.Builder(this)
//                .setRecordFileName("Record_" + new SimpleDateFormat("ddMMyyyyHHmmss", Locale.US).format(new Date()))
                //              .setRecordDirName("CallRecord")
                .setRecordFileName("")
                .setRecordDirName("AandavarLatheUser")
                .setRecordDirPath(Environment.getExternalStorageDirectory().getPath())
                .setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//                .setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//                .setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                .setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                .setAudioSource(source)
                .setShowSeed(true)
                .build();

        //callRecord.changeReceiver(new MyCallRecordReceiver(callRecord));
        //callRecord.startCallReceiver();
        callRecord.startCallRecordService();

        callRecord.enableSaveFile();
    }

    public static boolean isLocationEnabled(Context context)
    {
        int locationMode = 0;
        String locationProviders;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            try
            {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }
        else
        {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        stopLocationUpdates();
    }

    protected void getLocation() {
        if (isLocationEnabled(MainActivity.this))
        {
            /*
            Log.e("TAG", "Location is enabled");
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

            //You can still do this if you like, you might get lucky:
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                            ,10);
                }
                return;
            }
            Log.e("Provider", bestProvider);
            Location location = locationManager.getLastKnownLocation(bestProvider);
            locationManager.requestLocationUpdates(bestProvider, 0, 0, listener);
            if (location != null) {
                Log.e("TAG", "GPS is on");
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                String time = DateFormat.getTimeInstance().format(location.getTime());
                String latlng =  "Latitude: " + latitude + "\nLongitude: " + longitude+"\nTime: "+time;
                Log.e("GEO Location", latlng);
            }
            else{
                Log.e("TAG", "Location is null");
                //This is what you need:
                //locationManager.requestLocationUpdates(bestProvider, 1000, 0, listener);
            }
            */
        }
        else
        {
            //prompt user to enable location....
            AlertDialog.Builder notifyLocationServices = new AlertDialog.Builder(MainActivity.this);
            notifyLocationServices.setTitle("Switch on Location Services");
            notifyLocationServices.setMessage("Location Services must be turned on to complete this action. Also please take note that if on a very weak network connection,  such as 'E' Mobile Data or 'Very weak Wifi-Connections' it may take even 15 mins to load. If on a very weak network connection as stated above, location returned to application may be null or nothing and cause the application to crash.");
            notifyLocationServices.setPositiveButton("Ok, Open Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent openLocationSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    MainActivity.this.startActivity(openLocationSettings);
                    finish();
                }
            });
            notifyLocationServices.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            notifyLocationServices.show();
        }
        //WRITE_EXTERNAL_STORAGE Permission
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            if( Environment.getExternalStorageDirectory().canWrite()) {
                //   Toast.makeText(getApplicationContext(), "The path is writable", Toast.LENGTH_LONG).show();
            }
            else {
                //   Toast.makeText(getApplicationContext(), "The path is not writable and asking permission", Toast.LENGTH_LONG).show();

                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                        //         Toast.makeText(getApplicationContext(), "Grant the permission otherwise the app doesn't work", Toast.LENGTH_LONG).show();
                    } else {
                        // No explanation needed; request the permission
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                } else {
                    // Permission has already been granted
                }

            }
        }
        else {
            //  Toast.makeText(getApplicationContext(), "MEDIA_MOUNTED not equal", Toast.LENGTH_LONG).show();
        }

        //READ_EXTERNAL_STORAGE Permission
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            if( Environment.getExternalStorageDirectory().canRead()) {
                //   Toast.makeText(getApplicationContext(), "The path is writable", Toast.LENGTH_LONG).show();
            }
            else {
                //   Toast.makeText(getApplicationContext(), "The path is not writable and asking permission", Toast.LENGTH_LONG).show();

                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                        //         Toast.makeText(getApplicationContext(), "Grant the permission otherwise the app doesn't work", Toast.LENGTH_LONG).show();
                    } else {
                        // No explanation needed; request the permission
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                } else {
                    // Permission has already been granted
                }

            }
        }
        else {
            //  Toast.makeText(getApplicationContext(), "MEDIA_MOUNTED not equal", Toast.LENGTH_LONG).show();
        }

        //Camera Permission
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //         Toast.makeText(getApplicationContext(), "Grant the permission otherwise the app doesn't work", Toast.LENGTH_LONG).show();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_REQ);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        /*
        if(currentAddr == null){
            Bundle bundle = getIntent().getExtras();
            currentAddr = bundle.getString("Addr");
        }
        */
        /*
        if(currentAddr == null){
            Intent intent = new Intent(MainActivity.this,MapsActivity.class);
            //         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivityForResult(intent, 2);
        }
        */
        if (!currentlyProcessingLocation) {
            Log.e(TAG, "about to start tracking....");
            currentlyProcessingLocation = true;
            startTracking();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();

//        unregisterReceiver(mBatInfoReceiver);
        //  locationManager.removeUpdates(listener);

    }

    private Boolean exit = false;
    @Override
    public void onBackPressed() {
        if (mSlideSideMenu != null && mSlideSideMenu.closeSideMenu()) {
            // Closed the side menu, override the default back pressed behavior
            return;
        }else {

        }
        if (exit) {
            stopLocationUpdates();
            finish(); // finish activity
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

        //super.onBackPressed();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_shop:
                    //toolbar.setTitle("Expenses");
                    ShowExpensesPopup(R.layout.expenses_popup);
                    return true;
                case R.id.navigation_gifts:

                    if(currentAddr == null){
                        //    Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                        //         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        //  startActivityForResult(intent, 2);
                        startTracking();
                        Toast.makeText(MainActivity.this,"Location not found. Wait for a minute and try again." , Toast.LENGTH_LONG).show();
                    }
                    else {
                        //toolbar.setTitle("Check IN");
                        ShowCheckInPopup(R.layout.check_in_popup);
                    }


                    return true;
                case 1/*R.id.navigation_cart*/:
                    //toolbar.setTitle("Collection");
                    ShowCollectionPopup(R.layout.collection_popup);
                    return true;
            }
            return false;
        }
    };


    public void ShowExpensesPopup(int view) {


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

        final EditText tvName, tvDetails, tvAmount;
        final TextView  tvDate;
        Button btnExepneses;

        tvName =(EditText) myDialog.findViewById(R.id.tvName);
        tvDetails =(EditText) myDialog.findViewById(R.id.tvDetails);
        tvDate =(TextView) myDialog.findViewById(R.id.tvDate);
        tvAmount =(EditText) myDialog.findViewById(R.id.tvAmount);
        btnExepneses =(Button) myDialog.findViewById(R.id.btnExepneses);

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c);
        tvDate.setText(formattedDate);
        Log.e("Date",formattedDate );
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int day, month, year;
                Calendar calendar = Calendar.getInstance();
                day = calendar.get(Calendar.DAY_OF_MONTH);
                month = calendar.get(Calendar.MONTH);
                year = calendar.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int iYear, int iMonth, int iDay) {
                        NumberFormat numberFormat = new DecimalFormat("00");
                        String day,month,year;
                        day = String.valueOf(numberFormat.format(iDay));
                        month = String.valueOf(numberFormat.format(iMonth+1));
                        year = String.valueOf(iYear);
                        date = day+"-"+month+"-"+year;
                        tvDate.setText(date);
                    }
                },year,month,day);
                datePickerDialog.show();

            }
        });
        btnExepneses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Name, Details, Date, Amount;
                Name = tvName.getText().toString();
                Details = tvDetails.getText().toString();
                Date = tvDate.getText().toString();
                Amount = tvAmount.getText().toString();

                if(Name.trim().length()>0 && Details.trim().length()>0
                        && Date.trim().length()>0 && Amount.trim().length()>0){
                    type = "Expenses";
                    loading = ProgressDialog.show(MainActivity.this, "Processing...","Please Wait...",true,true);
                    BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                    backgroundWorker.execute(Name, Details, Date, Amount);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Enter all the Expenses details",Toast.LENGTH_LONG).show();
                }

            }
        });

        myDialog.show();
    }
    public void ShowCheckInPopup(int view) {
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

        final EditText tvName, tvDetails;
        final TextView tvLocation;

        Button btnCheckIn;

        tvName =(EditText) myDialog.findViewById(R.id.tvName);
        tvDetails =(EditText) myDialog.findViewById(R.id.tvDetails);
        tvLocation =(TextView) myDialog.findViewById(R.id.tvLocation);
        tvPhoto =(TextView) myDialog.findViewById(R.id.tvPhoto);
        tvVideo = (TextView) myDialog.findViewById(R.id.tvVideo);
        //videoView = myDialog.findViewById(R.id.videoView);
        btnCheckIn =(Button) myDialog.findViewById(R.id.btnCheckIn);

        //imageView =(ImageView) myDialog.findViewById(R.id.image_view);

        tvLocation.setText(currentAddr);

        tvPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCameraPhoto();
                //dispatchTakeVideoIntent();
                /*
                AlertDialog.Builder notifyLocationServices = new AlertDialog.Builder(MainActivity.this);
                notifyLocationServices.setTitle("Choose the option");
                notifyLocationServices.setMessage("Select");
                notifyLocationServices.setPositiveButton("Galary", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openGallery();
                    }
                });
                notifyLocationServices.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openCameraPhoto();
                    }
                });
                notifyLocationServices.show();
                */
            }
        });
        tvVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //openCameraPhoto();
                dispatchTakeVideoIntent(0);
                /*
                AlertDialog.Builder notifyLocationServices = new AlertDialog.Builder(MainActivity.this);
                notifyLocationServices.setTitle("Choose the option");
                notifyLocationServices.setMessage("Select");
                notifyLocationServices.setPositiveButton("Galary", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openGallery();
                    }
                });
                notifyLocationServices.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openCameraPhoto();
                    }
                });
                notifyLocationServices.show();
                */
            }
        });

        if(bitmap!=null){
            tvPhoto.setText(bitmapPath);
            Log.e("Popup","not null" );
            //imageView.setImageBitmap(bitmap);
        }
        else
            Log.e("Popup "," null" );

        btnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Name, Details, Location;
                Name = tvName.getText().toString();
                Details = tvDetails.getText().toString();
                //Details = "Details";
                Location = tvLocation.getText().toString();


                if(Name.trim().length()>0 && Details.trim().length()>0
                        && Location.trim().length()>0 ){
                    if(bitmap!=null){
                        loading = ProgressDialog.show(MainActivity.this, "Processing...","Please Wait...",true,true);
                        type = "checkIn";
                        String status = "pending";
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream .toByteArray();
                        String photo = Base64.encodeToString(byteArray, Base64.DEFAULT);
                        BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                        backgroundWorker.execute(Name, Location,Details,status,photo);
                    }else {
                        Toast.makeText(getApplicationContext(),"Select or Capture an Image",Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(),"Enter all the check in details",Toast.LENGTH_LONG).show();
                }

            }
        });

        myDialog.show();

    }
    public void ShowCollectionPopup(int view) {
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

        final EditText tvName, tvDetails, tvAmount;
        final TextView tvDate;
        Button btnCollection;

        tvName =(EditText) myDialog.findViewById(R.id.tvName);
        tvDetails =(EditText) myDialog.findViewById(R.id.tvDetails);
        tvDate =(TextView) myDialog.findViewById(R.id.tvDate);
        tvAmount=(EditText) myDialog.findViewById(R.id.tvAmount);
        btnCollection =(Button) myDialog.findViewById(R.id.btnCollection);

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c);
        tvDate.setText(formattedDate);
        Log.e("Date",formattedDate );
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int day, month, year;
                Calendar calendar = Calendar.getInstance();
                day = calendar.get(Calendar.DAY_OF_MONTH);
                month = calendar.get(Calendar.MONTH);
                year = calendar.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int iYear, int iMonth, int iDay) {
                        NumberFormat numberFormat = new DecimalFormat("00");
                        String day,month,year;
                        day = String.valueOf(numberFormat.format(iDay));
                        month = String.valueOf(numberFormat.format(iMonth+1));
                        year = String.valueOf(iYear);
                        date = day+"-"+month+"-"+year;
                        tvDate.setText(date);
                    }
                },year,month,day);
                datePickerDialog.show();

            }
        });
        btnCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Name, Details, Date, Amount;
                Name = tvName.getText().toString();
                Details = tvDetails.getText().toString();
                Date = tvDate.getText().toString();
                Amount = tvAmount.getText().toString();

                if(Name.trim().length()>0 && Details.trim().length()>0
                        && Date.trim().length()>0 && Amount.trim().length()>0){
                    type = "Collection";
                    loading = ProgressDialog.show(MainActivity.this, "Processing...","Please Wait...",true,true);
                    BackgroundWorkerJson backgroundWorker = new BackgroundWorkerJson();
                    backgroundWorker.execute(Name, Details, Date, Amount);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Enter all the Collection details",Toast.LENGTH_LONG).show();
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
        String json_string;
        JSONArray jsonArray;
        JSONObject jsonObject;

        @Override
        protected String doInBackground(String... params) {

            try {
                String post_data = null;
                URL url;
                HttpURLConnection httpURLConnection = null;
                if(type.equals("checkIn")){
                    url = new URL("http://andavarinfo.com/AandavarLathe/check_in.php");
                    httpURLConnection = (HttpURLConnection) url.openConnection();

                    Log.e(type,type );
                    String Name, Location, photo, Details,status;
                    Name = params[0];
                    Location = params[1];
                    Details = params[2];
                    status = params[3];
                    photo = params[4];

                    String boundaryString = "----SomeRandomText";
                    File logFileToUpload = videoFile;//new File(fileUrl);

                    // Indicate that we want to write to the HTTP request body
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);

                    OutputStream outputStreamToRequestBody = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter =
                            new BufferedWriter(new OutputStreamWriter(outputStreamToRequestBody));

                    //Include value from the myFileDescription text area in the post data
                    bufferedWriter.write("\n\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"myFileDescription\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write( userId+"_"+Name+"_"+new SimpleDateFormat("ddMMyyyyHHmmss", Locale.US).format(new Date())+".mp4");

                    //Include value from the myFileDescription text area in the post data
                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"user_id\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(userId);

                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"status\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(status);

                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"batteryPercentage\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(batteryPercentage);

                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"place_work_name\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(Name);

                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"location_address\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(Location);

                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"photo\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(photo);

                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"details\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(Details);

                    //Include value from the myFileDescription text area in the post data
                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"lattitude\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(String.valueOf(latitude));

                    //Include value from the myFileDescription text area in the post data
                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"longitutde\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(String.valueOf(longitude));

                    //Include the section to describe the file
                    bufferedWriter.write("\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data;"
                            + "name=\"myFile\";"
                            + "filename=\""+ logFileToUpload.getName() +"\""
                            + "\nContent-Type: text/plain\n\n");
                    bufferedWriter.flush();

                    // Write the actual file contents
                    FileInputStream inputStreamToLogFile = new FileInputStream(logFileToUpload);

                    int bytesRead;
                    byte[] dataBuffer = new byte[1024];
                    while((bytesRead = inputStreamToLogFile.read(dataBuffer)) != -1) {
                        outputStreamToRequestBody.write(dataBuffer, 0, bytesRead);
                    }

                    outputStreamToRequestBody.flush();

                    // Mark the end of the multipart http request
                    bufferedWriter.write("\n--" + boundaryString + "--\n");
                    bufferedWriter.flush();

                    // Close the streams
                    outputStreamToRequestBody.close();
                    bufferedWriter.close();
                }else if(type.equals("checkIn-Assign")){
                    url = new URL("http://andavarinfo.com/AandavarLathe/check_in.php");
                    httpURLConnection = (HttpURLConnection) url.openConnection();

                    Log.e(type,type );
                    String Name, Location, photo, Details,status,workId;
                    Name = params[0];
                    Location = params[1];
                    Details = params[2];
                    status = params[3];
                    photo = params[4];
                    workId = params[5];

                    String boundaryString = "----SomeRandomText";
                    File logFileToUpload = videoFile;//new File(fileUrl);

                    // Indicate that we want to write to the HTTP request body
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);

                    OutputStream outputStreamToRequestBody = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter =
                            new BufferedWriter(new OutputStreamWriter(outputStreamToRequestBody));

                    //Include value from the myFileDescription text area in the post data
                    bufferedWriter.write("\n\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"myFileDescription\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write( userId+"_"+Name+"_"+new SimpleDateFormat("ddMMyyyyHHmmss", Locale.US).format(new Date())+".mp4");

                    //Include value from the myFileDescription text area in the post data
                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"user_id\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(userId);

                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"status\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(status);

                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"batteryPercentage\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(batteryPercentage);

                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"place_work_name\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(Name);

                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"location_address\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(Location);

                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"photo\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(photo);

                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"details\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(Details);

                    //Include value from the myFileDescription text area in the post data
                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"lattitude\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(String.valueOf(latitude));

                    //Include value from the myFileDescription text area in the post data
                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"longitutde\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(String.valueOf(longitude));

                    bufferedWriter.write( "\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data; name=\"work_id\"");
                    bufferedWriter.write("\n\n");
                    bufferedWriter.write(String.valueOf(workId));

                    //Include the section to describe the file
                    bufferedWriter.write("\n--" + boundaryString + "\n");
                    bufferedWriter.write("Content-Disposition: form-data;"
                            + "name=\"myFile\";"
                            + "filename=\""+ logFileToUpload.getName() +"\""
                            + "\nContent-Type: text/plain\n\n");
                    bufferedWriter.flush();

                    // Write the actual file contents
                    FileInputStream inputStreamToLogFile = new FileInputStream(logFileToUpload);

                    int bytesRead;
                    byte[] dataBuffer = new byte[1024];
                    while((bytesRead = inputStreamToLogFile.read(dataBuffer)) != -1) {
                        outputStreamToRequestBody.write(dataBuffer, 0, bytesRead);
                    }

                    outputStreamToRequestBody.flush();

                    // Mark the end of the multipart http request
                    bufferedWriter.write("\n--" + boundaryString + "--\n");
                    bufferedWriter.flush();

                    // Close the streams
                    outputStreamToRequestBody.close();
                    bufferedWriter.close();
                }else if(type.equals("collection-Assign")){
                    url = new URL("http://andavarinfo.com/AandavarLathe/removeAssignWork.php");
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    Log.e(type,type );
                    String Name, Details,collectionAmt;
                    Name = params[0];
                    Details = params[1];
                    collectionAmt=params[2];

                    post_data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(Name, "UTF-8")
                            +"&"+URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
                            +"&"+URLEncoder.encode("details", "UTF-8") + "=" + URLEncoder.encode(Details, "UTF-8")
                            +"&"+URLEncoder.encode("collectionAmt", "UTF-8") + "=" + URLEncoder.encode(collectionAmt, "UTF-8")
                    ;
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();
                }else if(type.equals("Expenses")){

                    url = new URL("http://andavarinfo.com/AandavarLathe/expenses.php");
                    httpURLConnection = (HttpURLConnection) url.openConnection();

                    Log.e(type,type );
                    String Name, Details, Date, Amount;
                    Name = params[0];
                    Details = params[1];
                    Date = params[2];
                    Amount = params[3];

                    post_data = URLEncoder.encode("expense_name", "UTF-8") + "=" + URLEncoder.encode(Name, "UTF-8")
                            +"&"+URLEncoder.encode("description", "UTF-8") + "=" + URLEncoder.encode(Details, "UTF-8")
                            +"&"+URLEncoder.encode("expense_date", "UTF-8") + "=" + URLEncoder.encode(Date, "UTF-8")
                            +"&"+URLEncoder.encode("expense_amount", "UTF-8") + "=" + URLEncoder.encode(Amount, "UTF-8")
                            +"&"+URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
                    ;
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();

                }else if(type.equals("Collection")){
                    //idolUrl = "http://andavarinfo.com/AandavarLathe/collection.php";

                    url = new URL("http://andavarinfo.com/AandavarLathe/collection.php");
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    Log.e(type,type );
                    String Name, Details, Date, Amount;
                    Name = params[0];
                    Details = params[1];
                    Date = params[2];
                    Amount = params[3];

                    post_data = URLEncoder.encode("Name", "UTF-8") + "=" + URLEncoder.encode(Name, "UTF-8")
                            +"&"+URLEncoder.encode("Details", "UTF-8") + "=" + URLEncoder.encode(Details, "UTF-8")
                            +"&"+URLEncoder.encode("Date", "UTF-8") + "=" + URLEncoder.encode(Date, "UTF-8")
                            +"&"+URLEncoder.encode("Amount", "UTF-8") + "=" + URLEncoder.encode(Amount, "UTF-8")
                            +"&"+URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
                    ;
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();
                }else if(type.equals("assign")){
                    //idolUrl = "http://andavarinfo.com/AandavarLathe/getAssignWork.php";
                    url = new URL("http://andavarinfo.com/AandavarLathe/getAssignWork.php");
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    Log.e(type,type );
                    post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
                    ;
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();
                }else if(type.equals("Logout")){
                    //idolUrl = "http://andavarinfo.com/AandavarLathe/LoginSetStatus.php";
                    url = new URL("http://andavarinfo.com/AandavarLathe/LoginSetStatus.php");
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    Log.e(type,type );
                    post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8")
                    ;
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();
                }

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
            myDialog.dismiss();

            if(type.equals("checkIn")){
                Log.e(type,result );
                Toast.makeText(getApplicationContext(),result ,Toast.LENGTH_LONG ).show();
                loading.dismiss();

                Intent intent = new Intent(MainActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }else if(type.equals("collection-Assign")){
                Toast.makeText(getApplicationContext(),result ,Toast.LENGTH_LONG ).show();

                Intent intent = new Intent(MainActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }else if(type.equals("checkIn-Assign")){
                Log.e(type,result );
                assignWorkAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(),result ,Toast.LENGTH_LONG ).show();
                loading.dismiss();

                Intent intent = new Intent(MainActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }else if(type.equals("Expenses")){
                Log.e(type,result );
                Toast.makeText(getApplicationContext(),result ,Toast.LENGTH_LONG ).show();
                loading.dismiss();

                Intent intent = new Intent(MainActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }else if(type.equals("Collection")){
                Log.e(type,result );
                Toast.makeText(getApplicationContext(),result ,Toast.LENGTH_LONG ).show();
                loading.dismiss();

                Intent intent = new Intent(MainActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }else if(type.equals("Logout")){
                session.logoutUser();
                Toast.makeText(getApplicationContext(),"You have logged out successfully" ,Toast.LENGTH_LONG ).show();

            }else if(type.equals("assign")){
                Log.e(type,result );
                json_string = result;

                if(json_string != null)
                {
                    try {
                        jsonObject = new JSONObject(json_string);
                        jsonArray = jsonObject.getJSONArray("getAssignWork");

                        String work_title = null,details = null,deadline = null,fLetter,work_type = null,collection_amt = null
                                ,companyName=null,companyAddr=null,work_id=null;
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jo = jsonArray.getJSONObject(i);

                            work_title =  jo.getString("work_title");
                            details = jo.getString("details");
                            deadline = jo.getString("deadline");
                            fLetter = String.valueOf(userId.charAt(0));

                            work_type = jo.getString("work_type");
                            collection_amt = jo.getString("collection_amt");
                            companyName = jo.getString("companyName");
                            companyAddr = jo.getString("companyAddr");
                            work_id = jo.getString("work_id");

                            AssignWorkData assignWorkData = new AssignWorkData( work_title,details,deadline,fLetter,work_type,collection_amt,companyName,companyAddr,work_id);
                            assignWorkAdapter.add(assignWorkData);
                        }
                        loading.dismiss();
                        if(work_title==null&&deadline==null&&details==null){
                            tvNoWork.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.GONE);
                        }else
                            listView.setAdapter(assignWorkAdapter);

                        jsonArray = jsonObject.getJSONArray("getCount");
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jo = jsonArray.getJSONObject(i);
                            String check_in_count,sum_of_expense_amount,day_present,company_name;

                            check_in_count =  jo.getString("check_in_count");
                            sum_of_expense_amount = jo.getString("sum_of_expense_amount");
                            if(sum_of_expense_amount.equals("null")){
                                sum_of_expense_amount = "0";
                            }
                            day_present = jo.getString("day_present");
                            company_name = jo.getString("company_name");
                            tvCheckInCount.setText(check_in_count);
                            tvExpensesSumAmount.setText(sum_of_expense_amount);
                            tvDayPresent.setText(day_present);
                            tvCompanyName.setText(company_name);
                        }
                        String admin_id,enable;
                        admin_id = jsonObject.getString("admin_id");
                        if(admin_id.equals("1")){
                            Intent intent = new Intent(MainActivity.this, MainAdminActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }else if(admin_id.equals("2")){
                            tvAdmin.setVisibility(View.VISIBLE);
                        }else
                            tvAdmin.setVisibility(View.GONE);

                        enable = jsonObject.getString("enable");
                        if(enable.equals("No")){
                            type = "Logout";
                            BackgroundWorkerJson backgroundWorker1 = new BackgroundWorkerJson();
                            backgroundWorker1.execute();
                            Toast.makeText(MainActivity.this,"Please contact the admin to enable you to login...",Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else {
                    Log.e("JSON","null" );
                }
            }
        }
    }

    private void dispatchTakeVideoIntent(int i) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AndavarUser/videos";
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }

        File video_file = new File(file,"video"+".mp4");

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, i);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            takeVideoIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.androfocus.location.tracking.andavaruser", video_file);
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        }else {
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(video_file));
        }
        /*
        //From developer.android.com
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
        */
        startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
    }

    public void openCameraPhoto() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AndavarUser/images";
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }

        File imgae_file = new File(file,"temp_image.jpg");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.androfocus.location.tracking.andavaruser", imgae_file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        }else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imgae_file));
        }
        startActivityForResult(intent,CAPTURE_IMAGE);
    }

    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //imageView.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.ganesh));

        if(resultCode == RESULT_OK && requestCode == REQUEST_VIDEO_CAPTURE){
            Toast.makeText(this,"Video Captured Successfully" , Toast.LENGTH_LONG).show();

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AndavarUser/videos";
            File file = new File(path);
            if(!file.exists()){
                file.mkdirs();
            }

            videoFile = new File(file,"video"+".mp4");
            tvVideo.setText(path+"/video.mp4");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                Uri contentUri= data.getData();
                //Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.androfocus.location.tracking.andavaruser", videoFile);
                //videoView.setVideoURI(contentUri);
                //videoView.setVisibility(View.VISIBLE);
            }else {
                Uri contentUri= data.getData();
                //Uri contentUri = Uri.fromFile(videoFile);
                //videoView.setVisibility(View.VISIBLE);
                //videoView.setVideoURI(contentUri);
            }
        }
        if(resultCode == RESULT_OK && requestCode == CAPTURE_IMAGE){
            String path,externalPath,captureImagefilePath;
            captureImagefilePath="/AndavarUser/images/temp_image.jpg";
            externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            bitmapPath = "/AndavarUser/images/temp_image.jpg";
            path = externalPath + captureImagefilePath;

            bitmap = BitmapFactory.decodeFile(path);
            FileOutputStream out = null;
            try {
                File file;
                file =new File(externalPath+"/AndavarUser/images","temp_image.jpg");
                out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 25, out); // bmp is your Bitmap instance
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            bitmap = BitmapFactory.decodeFile(externalPath+bitmapPath);
            Log.e("Bitmap",path );
            if(bitmap!=null){
                tvPhoto.setText(bitmapPath);
                Log.e("Camera","not null" );
            }
            else
                Log.e("Camera"," null" );
            //imageView.setImageBitmap(bitmap);
        }
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            //found mistake=> if(requestCode == RESULT_OK && requestCode == PICK_IMAGE)
            Uri imageUri = data.getData();
            // try1=>fail - Because of mistake
            String[] projection={MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(imageUri,projection,null,null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(projection[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();

            bitmapPath = filePath;
            bitmap = BitmapFactory.decodeFile(filePath);
            Log.e("Bitmap",filePath );
            if(bitmap!=null){
                tvPhoto.setText(bitmapPath);
                Log.e("Galary","not null" );
            }
            else
                Log.e("Galery"," null" );
            //imageView.setImageBitmap(bitmap);
            //bitmapPhoto = decodeSampledBitmapFromPath(filePath, getPx(219), getPx(283));

            /*
            try {
                //try3=>fail - Because of mistake
                //InputStream inputStream = getContentResolver().openInputStream(imageUri);
                //bitmap = BitmapFactory.decodeStream(inputStream);

                //try2=>fail - Because of mistake
                //bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
        }

        if (requestCode == 2) {
            Log.e("Result", "Called");


            currentAddr = data.getStringExtra("Addr");
            latitude = Double.valueOf(data.getStringExtra("lat"));
            longitude = Double.valueOf(data.getStringExtra("lng"));
        }

    }

    private void startTracking() {
        Log.e(TAG, "startTracking");

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {

            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {
            Log.e(TAG, "unable to connect to google play services.");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.e(TAG, "position: " + location.getLatitude() + ", " + location.getLongitude() + " accuracy: " + location.getAccuracy());

            Geocoder geoCoder = new Geocoder(MainActivity.this, Locale.getDefault());
            StringBuilder builder = new StringBuilder();
            try {
                Double latitude,longitude;
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                List<Address> address = geoCoder.getFromLocation(latitude, longitude, 1);
                int maxLines = address.get(0).getMaxAddressLineIndex();
                for (int i=0; i<maxLines; i++) {
                    String addressStr = address.get(0).getAddressLine(i);
                    builder.append(addressStr);
                    builder.append(" ");
                }

                String finalAddress;
                //finalAddress= builder.toString(); //This is the complete address.
                finalAddress = address.get(0).getAddressLine(0)
                //        +"\n"+ address.get(0).getLocality()+"\n"+
                //      address.get(0).getAdminArea()+"\n"+
                //    address.get(0).getCountryName()+"\n"+
                //  address.get(0).getPostalCode()
                ;
                Log.e("Location", "Latitude: " + latitude + "\nLongitude: " + longitude+"\nFinal Address: "+finalAddress);

                //t.setText("Latitude: " + latitude + "\nLongitude: " + longitude+"\nFinal Address: "+finalAddress); //This will display the final address.

                //stopLocationUpdates();
                this.latitude = latitude;
                this.longitude = longitude;
                currentAddr = finalAddress;

                session.create_location(String.valueOf(latitude),String.valueOf(longitude) , finalAddress);
            } catch (IOException e) {
                // Handle IOException
            } catch (NullPointerException e) {
                // Handle NullPointerException
            }

        } else {
            Log.e(TAG, "NO POSITION FOUND");
        }
    }

    private void stopLocationUpdates() {
        /*
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        */
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
        Log.e(TAG, "stopLocationUpdates");
    }

    /**
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.e(TAG, "onConnected");

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(INTERVAL); // milliseconds
        locationRequest.setFastestInterval(FASTEST_INTERVAL); // the fastest rate in milliseconds at which your app can handle location updates
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        startLocationUpdates();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
        stopLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    protected void onStop() {
        super.onStop();

        googleApiClient.disconnect();
        Log.e(TAG, "isConnected ...............: " + googleApiClient.isConnected());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (googleApiClient.isConnected()) {
            startLocationUpdates();
            Log.e(TAG, "Location update resumed .....................");
        }
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
        Log.e(TAG, "startLocationUpdates");
    }

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    private  boolean checkAndRequestPermissions() {
        int camera = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        int storage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int loc = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int loc2 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int storage2= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.CAMERA);
        }
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (loc2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (loc != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
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
        Log.e(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            &&perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            &&perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "All permissions are granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.e(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                                ) {
                            showDialogOK("Storage, Camera and Location Services Permission required for this app",
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
            case 10:
                getLocation();
                break;
            default:
                break;
        }

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case CAMERA_PERMISSION_REQ: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            case 10:
                getLocation();
                break;
            default:
                break;
            // other 'case' lines to check for other
            // permissions this app might request.
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }
}
