package MqttConnection;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.Buffer;
import java.sql.Time;
import java.util.Date;
import java.util.Timer;

import static android.app.Activity.RESULT_CANCELED;
import static android.content.Intent.getIntent;
import static android.os.SystemClock.sleep;

public class MqttConnection {


    private String serverUri, clientId, username, password, topic;

    private long time;

    private MqttAndroidClient androidClient;

    private boolean connected = true;

    private int returnCode;


    private void setReturnCode(int value) {
        returnCode = value;
    }

    private void setConnected(boolean value){
        this.connected = value;
    }

    private long getTime() {
        return this.time;
    }

    public MqttConnection(final Context context,
                          String serverUri,
                          String clientId,
                          String username,
                          String password,
                          String topic) {
        this.serverUri = serverUri;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        this.topic = topic;

        androidClient = new MqttAndroidClient(context, serverUri, clientId);

    }



    public MqttConnection(final Context context,
                          String serverUri,
                          String clientId,
                          String username,
                          String password,
                          String topic,
                          final long time) {

        this.serverUri = serverUri;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        this.topic = topic;
        this.time = time;

        androidClient = new MqttAndroidClient(context, serverUri, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setUserName(this.username);
        options.setPassword(this.password.toCharArray());



        androidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                System.out.println("connection complete");
            }

            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("connection dropped" + cause.toString());
            }

            @Override
            public void messageArrived(final String topic, final MqttMessage message) {
                System.out.println(topic);
                System.out.println(getTime());
                File csv = receiveCSV(context, message);
                try {
                    sendResponse(new BufferedReader(new FileReader(csv)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });


    }


    public int connect(){
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setUserName(this.username);
        options.setPassword(this.password.toCharArray());


        try {
            IMqttToken connection = androidClient.connect(options);
            connection.setActionCallback( new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    subscribe();

                    System.out.println("Connected!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {


                    System.out.println("Error:" + exception.toString());

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        return returnCode;
    }

    private void subscribe(){
        try {
            IMqttToken subscription = androidClient.subscribe(this.topic, 1);

            subscription.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    publish(topic ,"csv_request");

                    System.out.println("Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    System.out.println("Error:" + exception.toString());

                }
            });
        } catch (MqttException e) {
            System.out.println("errorrr");
            e.printStackTrace();
        }
    }

    public void publish(String topic, String payload) {
        IMqttDeliveryToken token;
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            token = androidClient.publish(topic + "_init", message);
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

//    public void receiveFile(){
//
//        int size = androidClient.getBufferedMessageCount();
//        androidClient.getBufferedMessage(size);
//    }




    private File receiveCSV(Context context, MqttMessage message) {

        File csvFile = new File(context.getFilesDir(), "csvFile");


        OutputStream os;
        try {
            os = new FileOutputStream(csvFile);
            os.write(message.getPayload());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return csvFile;

    }

    private void sendResponse(final BufferedReader csvReader){
        long step;
        if (getTime() == 0){
            step = 0;
        } else {
            step = 1;
        }


        new CountDownTimer(getTime(), step) {
            @Override
            public void onTick(long millisUntilFinished) {
                String row;
                try {
                    if ((row = csvReader.readLine()) != null){
                        androidClient.publish(topic, new MqttMessage(row.getBytes()));
                        sleep(1000);
                    } else {
                        cancel();
                    }
                } catch (IOException | MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {

            }
        }.start();

    }


}
//new FileReader(csvFile)
