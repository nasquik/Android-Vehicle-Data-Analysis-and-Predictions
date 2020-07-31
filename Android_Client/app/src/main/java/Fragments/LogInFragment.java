package Fragments;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.traffic.Main2Activity;
import com.example.traffic.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class LogInFragment extends Fragment {

    private EditText username;
    private EditText password;
    private EditText vehicleId;
    private Button button;
    private View view;
    private Intent intent;
    private String ipAddress;
    private String port;
    private TextView internetStatus;
    private Handler handler;
    private Runnable runnableCode;
    private MqttAndroidClient test_connection;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.log_in, container, false);
        handler = new Handler();

        username = view.findViewById(R.id.username);
        vehicleId = view.findViewById(R.id.vehicle_id);
        password = view.findViewById(R.id.password);
        button = view.findViewById(R.id.connect);
        internetStatus = view.findViewById(R.id.internetStatus);

        runnableCode = new Runnable() {
            @Override
            public void run() {
                if (isConnected(getContext())) {
                    internetStatus.setText(R.string.connected);
                } else {
                    internetStatus.setText(R.string.disconnected);
                }
                handler.postDelayed(this, 2000);
            }
        };
        handler.post(runnableCode);

        return view;
    }

    public void setConnectionSettings(String ipAddress, String port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    @Override
    public void onResume(){
        super.onResume();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if ((ipAddress == null || ipAddress.isEmpty())
                        || (port == null || port.isEmpty())) {
                    Toast toast = Toast.makeText(getContext(), "Set up Connection settings", Toast.LENGTH_SHORT);
                    toast.show();
                } else if (username.getText().toString().isEmpty()
                        || password.getText().toString().isEmpty()
                        || vehicleId.getText().toString().isEmpty()){
                    Toast toast = Toast.makeText(getContext(), "Set up Log in settings", Toast.LENGTH_SHORT);
                    toast.show();

                } else {
                    intent = new Intent(getActivity(), Main2Activity
                            .class);


                    intent.putExtra("username", username.getText().toString());
                    intent.putExtra("vehicle_id", vehicleId.getText().toString());
                    intent.putExtra("password", password.getText().toString());
                    intent.putExtra("ip_address", ipAddress);
                    intent.putExtra("port", port);

                    String serverURI = "tcp://" + ipAddress + ":" + port;


                    test_connection = new MqttAndroidClient(getContext(), serverURI, vehicleId.getText().toString());
                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setAutomaticReconnect(true);
                    options.setCleanSession(true);
                    options.setUserName(username.getText().toString());
                    options.setPassword(password.getText().toString().toCharArray());

                    try {
                        test_connection.connect(options).setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                try {
                                    test_connection.disconnect();
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }

                                Toast toast = Toast.makeText(getContext(), "Log in succeed", Toast.LENGTH_SHORT);
                                toast.show();
                                startActivityForResult(intent, 1);
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                                Toast toast = Toast.makeText(getContext(), "Log in failed", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }



                }


            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnableCode);

    }

    private boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            Log.w("app", "exiting");
            if (test_connection.isConnected()) {
                try{
                    test_connection.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            getActivity().finish();
        }
    }


}
