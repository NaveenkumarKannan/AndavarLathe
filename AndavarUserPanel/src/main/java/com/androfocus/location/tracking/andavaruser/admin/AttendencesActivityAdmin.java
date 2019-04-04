package com.androfocus.location.tracking.andavaruser.admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.androfocus.location.tracking.andavaruser.R;

public class AttendencesActivityAdmin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendences_admin);
    }

    public void goBack(View view) {
        finish();
    }

    public void intentToProfile(View view) {
        startActivity(new Intent(this,ProfileAdminActivity.class));
    }

    public void intentToUser(View view) {
        startActivity(new Intent(this,UserDetailsActivity.class));
    }
}
