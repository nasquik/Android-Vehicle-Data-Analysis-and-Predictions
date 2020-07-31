package com.example.traffic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;


import com.google.android.material.tabs.TabLayout;

import Fragments.ConnectionSetUpFragment;
import Fragments.LogInFragment;
import Fragments.TabAdapter;

public class MainActivity extends AppCompatActivity implements ConnectionSetUpFragment.ConnectionSetUpListener {

    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private LogInFragment logFrag;
    private ConnectionSetUpFragment connectionFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        adapter = new TabAdapter(getSupportFragmentManager());

        logFrag = new LogInFragment();
        connectionFrag = new ConnectionSetUpFragment();

        adapter.addFragment(logFrag, "Log in");
        adapter.addFragment(connectionFrag, "Connection Setup");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);


    }

    @Override
    public void onInputConnectionSent(String ipAddress, String port) {
        logFrag.setConnectionSettings(ipAddress, port);
    }



}