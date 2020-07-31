package Fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.traffic.Main2Activity;
import com.example.traffic.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionFragment extends Fragment {

    private static final String mqtt = "Mqtt-Connection:";

    private EditText timeString;
    private Long time;
    private String topic;
    private String topic_or;
    private String serverURI;
    private MqttAndroidClient client;
    private AtomicBoolean valid;
    private CountDownTimer timer;
    View mView;

    public ConnectionFragment() {
        // Required empty public constructor
    }

    private ConnectionExitListener listener;

    public interface ConnectionExitListener {
        void onExit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_connection, container, false);
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        timeString = mView.findViewById(R.id.timer);


        Button exit = mView.findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onExit();

            }
        });

        Button stopTransmission = mView.findViewById(R.id.stop_transmission);
        stopTransmission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timer != null){
                    timer.cancel();
                }

            }
        });

        Button setTimer = mView.findViewById(R.id.set_timer);
        setTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("time: " + timeString.getText().toString());

                if (!timeString.getText().toString().matches("")){
                    time = Long.parseLong(timeString.getText().toString());
                } else {
                    time = (long) 0;
                }

                csvRequest();

            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout for this fragment

        valid = new AtomicBoolean(true);

        Main2Activity activity = (Main2Activity) getActivity();

        assert activity != null;

        client = activity.getClient();
        topic = activity.getTopic_req();
        topic_or = activity.getTopic_ori();

    }
    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ConnectionFragment.ConnectionExitListener) {
            listener = (ConnectionFragment.ConnectionExitListener) context;
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void csvRequest() {
        MqttMessage new_message = new MqttMessage("request".getBytes());
        try {
            Log.i(mqtt, "Publish csv request");
            client.publish(topic, new_message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public File csvRead(MqttMessage message) {
        File file = new File(getActivity().getApplicationContext().getFilesDir(), "csvFile");

        OutputStream os;
        try {
            os = new FileOutputStream(file);
            os.write(message.getPayload());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public void sentData(File file) {
        final ArrayList<String> values = new ArrayList<>();
        String row;
        try {

            BufferedReader csvReader = new BufferedReader(new FileReader(file));
            while ((row = csvReader.readLine()) != null) {
                values.add(row);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        int step;

        if (time == 0) {
            time = Long.MAX_VALUE;
            step = 1000;
        } else {
            time = time * 1000;
            step = 1000;
        }

        Log.i("values_size","" + values.size());

        timer = new CountDownTimer(time, step) {
            int index = 0;
            @Override
            public void onTick(long millisUntilFinished) {
                try {
                    if (!valid.get()) {
                        valid.set(true);
                        cancel();
                    }
                    if (index < values.size()){
                        Log.i(mqtt, values.get(index));
                        client.publish(topic+"_s", new MqttMessage(values.get(index).getBytes()));
                        client.publish(topic_or, new MqttMessage(values.get(index).getBytes()));
                        index++;
                    } else {
                        cancel();
                    }
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {
                try {
                    client.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }
}
