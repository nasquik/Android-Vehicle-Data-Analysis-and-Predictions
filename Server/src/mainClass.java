import org.eclipse.paho.client.mqttv3.MqttException;
import org.jfree.ui.RefineryUtilities;
import org.xml.sax.SAXException;

import javax.print.PrintException;
import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class mainClass {
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, PrintException, SQLException, ClassNotFoundException, MqttException, InterruptedException {
        //initialize the class method
        xmlParser parser = new xmlParser();

        parser.reader("./Data/vehicle_26.xml","./Data/vehicle_26.csv");
        List<String[]> vehicle_26_data = parser.csvParser("./Data/vehicle_26.csv");

        parser.reader("./Data/vehicle_27.xml","./Data/vehicle_27.csv");
        List<String[]> vehicle_27_data = parser.csvParser("./Data/vehicle_27.csv");

        parser.reader("./Data/all_vehicles.xml","./Data/all_vehicles.csv");
        List<String[]> all_vehicles_data = parser.csvParser("./Data/all_vehicles.csv");


        double lat_end = 37.9685400;
        double long_end = 23.7752500;

        heatMaps mapCreator = new heatMaps();
        mapCreator.mapGridGradientCreator();
        mapCreator.RSSIHeatMapCreator(all_vehicles_data);
        mapCreator.throughputHeatMapCreator(all_vehicles_data);
        double [] predicted_RSSI_throughput = mapCreator.getPredictions(lat_end, long_end);
        //System.out.println(predicted_RSSI_throughput[0] + " " + predicted_RSSI_throughput[1]);

        //dummy for accessing the list elements for a certain vehicle id
        JDBC test = new JDBC();
        test.openConnection();



//        charts chart = new charts();
//        chart.pack();
//        RefineryUtilities.centerFrameOnScreen(chart);
//        chart.setVisible(true);


        //server
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the mqtt port: ");
        int port = scanner.nextInt();
        Mqtt mqtt = new Mqtt();
        mqtt.connect(port,mapCreator);

        List<ArrayList<String>> vehicle26Info = test.getDB(26);
        List<ArrayList<String>> vehicle27Info = test.getDB(27);

        for(int i =0 ; i < vehicle27Info.size();i++){
            for(int j=0; j < vehicle27Info.get(0).size();j++){
                System.out.print(vehicle27Info.get(i).get(j)+ " ");
            }
            System.out.println();
        }

        charts chartMaker = new charts();
        chartMaker.createAllCharts(vehicle26Info, vehicle27Info);
    }
}

