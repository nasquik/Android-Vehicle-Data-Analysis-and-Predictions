import javax.imageio.ImageIO;
import javax.swing.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.PlotOrientation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.abs;

public class charts extends JFrame {

    private static final long serialVersionUID = 1L;

    vehicle vehicle26 = new vehicle();
    vehicle vehicle27 = new vehicle();

    datasets datasets = new datasets();

    public void createAllCharts(List<ArrayList<String>> vehicle26Info, List<ArrayList<String>> vehicle27Info) throws IOException {

        datasets.meanErrorRSSI = new DefaultCategoryDataset();
        datasets.meanErrorThroughput = new DefaultCategoryDataset();
        datasets.meanErrorDistance = new DefaultCategoryDataset();

        // Create Datasets
        createDatasets(vehicle26Info, 26);
        createDatasets(vehicle27Info, 27);

        // create overall category
        datasets.meanErrorRSSI.addValue((vehicle26.meanErrorRSSI + vehicle27.meanErrorRSSI) / (vehicle26.rtCount + vehicle27.rtCount), "Overall", "RSSI Error");
        datasets.meanErrorThroughput.addValue((vehicle26.meanErrorThroughput + vehicle27.meanErrorThroughput) / (vehicle26.rtCount + vehicle27.rtCount), "Overall", "Throughput Error");
        datasets.meanErrorDistance.addValue((vehicle26.meanErrorDistance + vehicle27.meanErrorDistance) / (vehicle26.count + vehicle27.count), "Overall", "Distance Error");

        createRSSI();
        createThroughput();
        createDistance();

        // create line charts

        createLineCharts(26);
        createLineCharts(27);
    }

    public void createLineCharts(int name) throws IOException {

        vehicle currentVehicle;
        if(name == 26)
            currentVehicle = vehicle26;
        else
            currentVehicle = vehicle27;

        // create RSSI line chart

        JFreeChart chartRSSI = ChartFactory.createLineChart(
                "RSSI Error Timeline - " + name,
                "Timestamp",
                "Error Value",
                currentVehicle.rssiErrorTimeline
        );

        ChartPanel panelRSSI = new ChartPanel(chartRSSI);
        setContentPane(panelRSSI);
        setUndecorated(true);
        setAlwaysOnTop(true);
        pack();
        setSize(600, 400);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        BufferedImage rssi = new BufferedImage(600, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = rssi.createGraphics();

        paint(graphics2D);

        graphics2D.dispose();
        dispose();

        File rssiFile = new File("./Data/" + name + "-rssi-error-timeline.png");
        ImageIO.write(rssi, "PNG", rssiFile);

        // create Throughput Error Timeline

        JFreeChart chartThroughput = ChartFactory.createLineChart(
                "Throughput Error Timeline - " + name,
                "Timestamp",
                "Value",
                currentVehicle.throughputErrorTimeline
        );

        ChartPanel panelThroughput = new ChartPanel(chartThroughput);
        setContentPane(panelThroughput);
        setUndecorated(true);
        setAlwaysOnTop(true);
        pack();
        setSize(600, 400);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        BufferedImage throughput = new BufferedImage(600, 400, BufferedImage.TYPE_INT_RGB);
        graphics2D = throughput.createGraphics();

        paint(graphics2D);

        graphics2D.dispose();
        dispose();

        File throughputFile = new File("./Data/" + name + "-throughput-error-timeline.png");
        ImageIO.write(throughput, "PNG", throughputFile);

        // Create Distance line chart

        JFreeChart chartDifference = ChartFactory.createLineChart(
                "Distance Difference - " + name, // Chart title
                "Timestamp", // X-Axis Label
                "Value", // Y-Axis Label
                currentVehicle.distanceErrorTimeline
        );

        ChartPanel panelDifference = new ChartPanel(chartDifference);
        setContentPane(panelDifference);
        setUndecorated(true);
        setAlwaysOnTop(true);
        pack();
        setSize(600, 400);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        BufferedImage difference = new BufferedImage(600, 400, BufferedImage.TYPE_INT_RGB);
        graphics2D = difference.createGraphics();

        paint(graphics2D);

        graphics2D.dispose();
        dispose();

        File differenceFile = new File("./Data/" + name + "-distance-error-timeline.png");
        ImageIO.write(difference, "PNG", differenceFile);
    }

    private void createRSSI() throws IOException {

        JFreeChart barChartRSSI = ChartFactory.createBarChart(
                "Mean Errors RSSI",
                "Category",
                "Error",
                datasets.meanErrorRSSI,
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(barChartRSSI);
        chartPanel.setPreferredSize(new Dimension(600 , 400));
        setContentPane(chartPanel);
        setUndecorated(true);
        setAlwaysOnTop(true);
        pack();
        setSize(600, 400);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        BufferedImage rssi = new BufferedImage(600, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = rssi.createGraphics();

        paint(graphics2D);

        graphics2D.dispose();
        dispose();

        File rssiFile = new File("./Data/rssi-errors.png");
        ImageIO.write(rssi, "PNG", rssiFile);
    }

    private void createThroughput() throws IOException {

        JFreeChart barChartThroughput = ChartFactory.createBarChart(
                "Mean Errors Throughput",
                "Category",
                "Error",
                datasets.meanErrorThroughput,
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(barChartThroughput);
        chartPanel.setPreferredSize(new Dimension(600 , 400));
        setContentPane(chartPanel);
        setUndecorated(true);
        setAlwaysOnTop(true);
        pack();
        setSize(600, 400);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        BufferedImage throughput = new BufferedImage(600, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = throughput.createGraphics();

        paint(graphics2D);

        graphics2D.dispose();
        dispose();

        File throughputFile = new File("./Data/throughput-errors.png");
        ImageIO.write(throughput, "PNG", throughputFile);
    }

    private void createDistance() throws IOException {

        JFreeChart barChartDistance = ChartFactory.createBarChart(
                "Mean Errors Distance",
                "Category",
                "Error",
                datasets.meanErrorDistance,
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel= new ChartPanel(barChartDistance);
        chartPanel.setPreferredSize(new Dimension(600 , 400));
        setContentPane(chartPanel);
        setUndecorated(true);
        setAlwaysOnTop(true);
        pack();
        setSize(600, 400);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        BufferedImage distance = new BufferedImage(600, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = distance.createGraphics();

        paint(graphics2D);

        graphics2D.dispose();
        dispose();

        File distanceFile = new File("./Data/distance-errors.png");
        ImageIO.write(distance, "PNG", distanceFile);
    }

    private void createDatasets(List<ArrayList<String>> vehicleInfo, int name){

        vehicle currentVehicle;

        if(name == 26)
            currentVehicle = vehicle26;
        else
            currentVehicle = vehicle27;

        currentVehicle.rssiErrorTimeline = new DefaultCategoryDataset();
        currentVehicle.throughputErrorTimeline = new DefaultCategoryDataset();
        currentVehicle.distanceErrorTimeline = new DefaultCategoryDataset();

        for (ArrayList<String> line : vehicleInfo){

            String timestep = line.get(1);

            currentVehicle.count++;

            // create RSSI dataset
            double predRSSI = Double.parseDouble(line.get(8));
            double realRSSI = Double.parseDouble(line.get(6));
            double rssiDiff;

            if(predRSSI == -1.0){
                currentVehicle.rssiErrorTimeline.addValue(0.0, "RSSI Error", timestep);
            }
            else{
                currentVehicle.rtCount++;
                rssiDiff = abs(predRSSI - realRSSI);
                currentVehicle.meanErrorRSSI += rssiDiff;
                currentVehicle.rssiErrorTimeline.addValue(rssiDiff, "RSSI Error", timestep);
            }


            // create Throughput dataset
            double predThroughput = Double.parseDouble(line.get(9));
            double realThroughput = Double.parseDouble(line.get(7));
            double throughputDiff;

            if(predThroughput == -1.0){
                currentVehicle.throughputErrorTimeline.addValue(0.0, "Throughput Error", timestep);
            }
            else{
                throughputDiff = abs(predThroughput - realThroughput);
                currentVehicle.meanErrorThroughput += throughputDiff;
                currentVehicle.throughputErrorTimeline.addValue(throughputDiff, "Throughput Error", timestep);
            }

            // create Distance dataset
            double realLat = Double.parseDouble(line.get(2));
            double realLong = Double.parseDouble(line.get(3));
            double predLat = Double.parseDouble(line.get(4));
            double predLong = Double.parseDouble(line.get(5));

            double theta;
            double dist = 0.0;

            if ((realLat != predLat) && (realLong != predLong)) {

                theta = realLong - predLong;
                dist = Math.sin(Math.toRadians(realLat)) * Math.sin(Math.toRadians(predLat)) + Math.cos(Math.toRadians(realLat)) * Math.cos(Math.toRadians(predLat)) * Math.cos(Math.toRadians(theta));
                dist = Math.acos(dist);
                dist = Math.toDegrees(dist);
                dist = dist * 60 * 1.1515;
                dist = dist * 1.609344;
                dist = dist * 1000;
            }

            currentVehicle.meanErrorDistance += dist;
            currentVehicle.distanceErrorTimeline.addValue(dist, "Distance Error", timestep);
        }

        System.out.println("Saving Vehicle " + name + "'s data...");
        datasets.meanErrorRSSI.addValue(currentVehicle.meanErrorRSSI / currentVehicle.rtCount, "Vehicle" + name , "RSSI Error");
        datasets.meanErrorThroughput.addValue( currentVehicle.meanErrorThroughput / currentVehicle.rtCount, "Vehicle" + name , "Throughput Error");
        datasets.meanErrorDistance.addValue( currentVehicle.meanErrorDistance / currentVehicle.count, "Vehicle" + name , "Distance Error");
    }
}

class vehicle{

    int count = 0;
    int rtCount = 0;
    double meanErrorRSSI = 0.0;
    double meanErrorThroughput = 0.0;
    double meanErrorDistance = 0.0;

    DefaultCategoryDataset rssiErrorTimeline;
    DefaultCategoryDataset throughputErrorTimeline;
    DefaultCategoryDataset distanceErrorTimeline;
}

class datasets{
    DefaultCategoryDataset meanErrorRSSI;
    DefaultCategoryDataset meanErrorThroughput;
    DefaultCategoryDataset meanErrorDistance;
}