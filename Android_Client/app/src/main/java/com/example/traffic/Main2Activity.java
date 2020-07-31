package com.example.traffic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.File;

import Fragments.ConnectionFragment;
import Fragments.ConnectionSetUpFragment;
import Fragments.LogInFragment;
import Fragments.MyMapFragment;
import Fragments.TabAdapter;

public class Main2Activity extends AppCompatActivity implements ConnectionFragment.ConnectionExitListener {

    public String getTopic_req() {
        return topic_req;
    }

    public MqttAndroidClient getClient() {
        return client;
    }

    public String getTopic_ori() {
        return topic_ori;
    }


    private String mqtt = "main2";
    private String ipAddress;
    private String port;
    private String username;
    private String password;
    private String vehicleId;
    private MqttAndroidClient client;
    private String serverURI;
    private String topic_req;
    private String topic_pre;
    private String topic_ori;

    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private MyMapFragment myMapFragment;
    private ConnectionFragment connectionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent logInFragment = getIntent();
        if (logInFragment.getExtras().getString("ip_address") != null)
            ipAddress = logInFragment.getExtras().getString("ip_address");
        if (logInFragment.getExtras().getString("port") != null)
            port = logInFragment.getExtras().getString("port");
        if (logInFragment.getExtras().getString("username") != null)
            username = logInFragment.getExtras().getString("username");
        if (logInFragment.getExtras().getString("vehicle_id") != null)
            vehicleId = logInFragment.getExtras().getString("vehicle_id");
        if (logInFragment.getExtras().getString("password") != null)
            password = logInFragment.getExtras().getString("password");

        serverURI = "tcp://" + ipAddress + ":" + port;
        topic_req = "android/" + vehicleId;
        topic_pre = topic_req + "_s_prediction";
        topic_ori = vehicleId + "_original";


        client = new MqttAndroidClient(getApplicationContext(), serverURI, vehicleId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setUserName(username);
        options.setPassword(password.toCharArray());

        try {
            client.connect(options);
            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    Log.i(mqtt, "Connection complete");
                    viewPager = findViewById(R.id.viewPager);
                    tabLayout = findViewById(R.id.tabLayout);

                    adapter = new TabAdapter(getSupportFragmentManager());

                    myMapFragment = new MyMapFragment();
                    connectionFragment = new ConnectionFragment();


                    adapter.addFragment(connectionFragment, "Connection");
                    adapter.addFragment(myMapFragment, "Map");


                    viewPager.setAdapter(adapter);
                    tabLayout.setupWithViewPager(viewPager);

                    try {
                        client.subscribe(topic_req, 1, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                String msg = "Subscribed to " + topic_req;
                                Log.i(mqtt, msg);
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                String msg = "Subscribing to " + topic_req + " failed";
                                Log.e(mqtt, msg);
                            }
                        });
                        client.subscribe(topic_pre, 1, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                String msg = "Subscribed to " + topic_pre;
                                Log.i(mqtt, msg);
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                String msg = "Subscribing to " + topic_pre + " failed";
                                Log.e(mqtt, msg);
                            }
                        });
                        client.subscribe(topic_ori, 1, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                String msg = "Subscribed to " + topic_ori;
                                Log.i(mqtt, msg);
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                String msg = "Subscribing to " + topic_ori + " failed";
                                Log.e(mqtt, msg);
                            }
                        });

                    } catch (MqttException e) {
                        e.printStackTrace();
                    }




                }

                @Override
                public void connectionLost(Throwable cause) {
                    Log.i(mqtt,"Disconnected");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    if (topic.equals(topic_req)) {
                        if (message.toString().equals("request")) {
                            return;
                        }
                        String msg = "New message arrived in " + topic;
                        Log.i(mqtt, msg);

                        File file = connectionFragment.csvRead(message);
                        connectionFragment.sentData(file);
                    } else if (topic.equals(topic_pre)) {
                        String msg = "New message arrived in " + topic;
                        Log.i(mqtt, msg);

                        myMapFragment.updateMap(message);
                    } else if (topic.equals(topic_ori)) {
                        String msg = "New message arrived in " + topic;
                        Log.i(mqtt, msg);

                        myMapFragment.updateMapOriginal(message);
                    }


                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.i(mqtt, "Message delivered");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

    public void onExit(){
        if (client.isConnected()) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("Closing ")
                .setMessage("Are you sure you want to close this app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }
}
