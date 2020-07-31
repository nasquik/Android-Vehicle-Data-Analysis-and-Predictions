import org.eclipse.paho.client.mqttv3.*;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException; import java.net.UnknownHostException; import java.nio.file.Files; import java.nio.file.Paths; import java.lang.Math;
import java.sql.SQLException;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.lang.Math;




public class Mqtt {
    MqttClient client;
    public void connect(int Port,heatMaps heatmaps) throws MqttException, SQLException, ClassNotFoundException, InterruptedException {
        String ip = "";
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = socket.getLocalAddress().getHostAddress();
        }
        catch(SocketException | UnknownHostException e) {

        }
        System.out.println("Server trying to connect to ip "+ ip );
        String connect = "tcp://"+ip+":"+Port;
        client = new MqttClient(connect,"server");

        MqttConnectOptions mqOptions = new MqttConnectOptions();
        mqOptions.setCleanSession(true);
        mqOptions.setUserName("dave");
        mqOptions.setPassword("123456".toCharArray());
        client.connect(mqOptions);
        String topic = "android/+";
        System.out.println("Subscribed to topic: "+ topic);
        client.subscribe(topic);
        String id = client.getServerURI();
        System.out.println("Servers id: "+id+"\n");

        //create a hashmap to save the predictions from the previous timestep
        //for each vechicle for the next step
        java.util.HashMap<String, vehiclePrediction> map = new java.util.HashMap<String, vehiclePrediction>();
        //create the database
        JDBC dummy = new JDBC();
        dummy.openConnection();
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                System.out.println("Connected: " + s + " " +b);
            }

            @Override
            public void connectionLost(Throwable throwable) {
                System.out.println("Connection Lost Exiting Mqtt!");
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                System.out.println("Received from topic: " + s);
                String pruned = s.substring(8);
                //System.out.println(pruned);
                //Means its a response from the android client with the coordinates
                if (countLines(mqttMessage.toString())<=1 && !mqttMessage.toString().equals("request")&& mqttMessage.toString().charAt(0) != '['){
                    //parse the line for the information
                    //create a tokenizer
                    String token;
                    String array[] = new String[8];
                    System.out.println("Received: "+ mqttMessage.toString());
                    StringTokenizer st1 = new StringTokenizer(mqttMessage.toString(),", ");
                    for (int i = 1; st1.hasMoreTokens(); i++)
                    {
                        token = st1.nextToken();
                        if (i == 1) {
                            array[i - 1] = token.substring(1);

                        }
                        else if(i == 8){
                            array[i-1] = token.substring(0,token.length()-1);

                        }
                        else{
                            array[i-1] = token;

                        }
                    }

                    //do the predictions
                    double predictedLat = predictLat(Double.parseDouble(array[2]),Double.parseDouble(array[5]),Double.parseDouble(array[4]));
                    double predictedLong = predictLong(Double.parseDouble(array[3]),Double.parseDouble(array[5]),Double.parseDouble(array[4]),Double.parseDouble(array[2]),predictedLat);

                    //predict rssi
                    //predict throughput
                    String id = array[1];


                    //Integer.getInteger(array[0])
                    double predictedNet [] = new double[2];
                    predictedNet = heatmaps.getPredictions(predictedLat,predictedLong);
                    vehiclePrediction newPrediction = new vehiclePrediction(Double.parseDouble(array[0]),predictedLat,predictedLong,predictedNet[0],predictedNet[1]);
                    String predictMsg = "[" + id + ", "+ predictedLat +", "+ predictedLong +", "+ Double.toString(predictedNet[0]) +", "+ Double.toString(predictedNet[1])+"]";

                    MqttMessage message = new MqttMessage(predictMsg.getBytes());



                    client.publish(s+"_prediction",message);


                    //check if the map already has a prediction for this vehicle
                    if(map.containsKey(id)){
                        //the previous prediction should be coherent with the current timestep
                        //create an array of string to insert to the database
                        String[] databaseArray = new String[12];
                        databaseArray[0] = array[0];
                        databaseArray[1] = array[1];
                        databaseArray[2] = array[2];
                        databaseArray[3] = array[3];
                        databaseArray[6] = array[6];
                        databaseArray[7] = array[7];
                        databaseArray[4] = Double.toString(predictedLat);
                        databaseArray[5] = Double.toString(predictedLong);
                        databaseArray[8] = Double.toString(predictedNet[0]);
                        databaseArray[9] = Double.toString(predictedNet[1]);
                        //insert entry into the database
                        dummy.insertEntry(databaseArray);

                    }
                    map.put(id,newPrediction);
                }
                //send the corresponding csv to android client
                int id1 = mqttMessage.getId();

                if (mqttMessage.toString().equals("request")){
                    System.out.println("Received request to send file!");
                    String filePath = "./Data/vehicle_"+pruned+".csv";
                    System.out.println("Sending file: "+filePath);
                    byte[] bFile = Files.readAllBytes(Paths.get(filePath));
                        MqttMessage msg= new MqttMessage(bFile);
                        client.publish(s,msg);
                    }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                System.out.println("File sent successfully.\n");
            }
        });

        //wait for the user to exit the connection
        Scanner scanner = new Scanner(System.in);
        int port = 5;
        while(scanner.nextInt()!=3){
            System.out.println("Invalid keypress");
        }
        client.disconnect();
    }

    public double predictLat(double firstLat,double speed,double angle){
        //convert degrees to radians for the formula
        double formLat = Math.toRadians(firstLat);
        double R = 6.371 * Math.pow(10,6);
        double delta = speed/R;
        double endLat = Math.asin(Math.sin(formLat))*Math.cos(delta)+Math.cos(formLat)*Math.sin(delta)*Math.cos(angle);
        endLat = Math.toDegrees(endLat);
        return endLat;
    }

    public double predictLong(double firstLong, double speed, double angle,double startLat, double endLat){
        double formLong = Math.toRadians(firstLong);
        double R = 6.371 * Math.pow(10,6);
        double delta = speed/R;
        double latStart = Math.toRadians(startLat);
        double latEnd = Math.toRadians(endLat);
        double endLong = formLong + Math.atan2(Math.sin(angle)*Math.sin(delta)*Math.cos(latStart),Math.cos(delta)-Math.sin(latStart)*Math.sin(latEnd));
        endLong = Math.toDegrees(endLong);
        return endLong;

    }


    private static int countLines(String str){
        String[] lines = str.split("\r\n|\r|\n");
        return  lines.length;
    }


}
