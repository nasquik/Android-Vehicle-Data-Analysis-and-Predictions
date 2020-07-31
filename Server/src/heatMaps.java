import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.lang.*;
import org.tc33.jheatchart.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;

public class heatMaps
{
    double min_lat = 37.9668800;
    double min_long = 23.7647600;
    double max_lat = 37.9686200;
    double max_long = 23.7753900;
    double lat_margin = (max_lat - min_lat) / 4;
    double long_margin = (max_long - min_long) / 10;

    double[][] RSSI;
    double[][] throughput;

    void mapGridGradientCreator() throws IOException {

        // create grid

        JFrame frame = new JFrame();

        GridsCanvas gridC = new GridsCanvas(1339, 275, 4, 10);
        gridC.setPreferredSize(new Dimension(1339,275));

        frame.setUndecorated(true);
        frame.add(gridC);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        BufferedImage grid = new BufferedImage(1339, 275, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = grid.createGraphics();

        frame.paint(graphics2D);

        graphics2D.dispose();
        frame.dispose();

        // combine grid with given map

        File gridFile = new File("./Data/grid.png");
        File mapFile = new File("./Data/Map.png");
        gridFile.deleteOnExit();
        ImageIO.write(grid,"PNG", gridFile);
        BufferedImage map = ImageIO.read(mapFile);

        float alpha = 0.5f;
        int compositeRule = AlphaComposite.SRC_OVER;
        AlphaComposite ac;

        int imgW = map.getWidth();
        int imgH = map.getHeight();

        BufferedImage overlay = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
        graphics2D = overlay.createGraphics();

        ac = AlphaComposite.getInstance(compositeRule, alpha);

        graphics2D.drawImage(map,0,0,null);
        graphics2D.setComposite(ac);
        graphics2D.drawImage(grid,0,0,null);
        graphics2D.setComposite(ac);

        graphics2D.dispose();

        File mapGridFile = new File("./Data/mapGrid.png");
        mapGridFile.deleteOnExit();
        ImageIO.write(overlay, "PNG", mapGridFile);

        // create gradient legend

        frame = new JFrame();

        GradientPaintLegend legend = new GradientPaintLegend();
        legend.setPreferredSize(new Dimension(50, 275));

        frame.setUndecorated(true);
        frame.add(legend);
        frame.setSize(new Dimension(50, 275));
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);

        BufferedImage gradient = new BufferedImage(50, 275, BufferedImage.TYPE_INT_RGB);
        graphics2D = gradient.createGraphics();
        frame.paint(graphics2D);

        graphics2D.dispose();
        frame.dispose();

        File gradientFile = new File("./Data/gradient.png");
        gradientFile.deleteOnExit();
        ImageIO.write(gradient,"PNG", gradientFile);

        // combine legend with map and grid

        BufferedImage mapGrid = ImageIO.read(mapGridFile);
        gradient = ImageIO.read(gradientFile);

        int imW = mapGrid.getWidth() + gradient.getWidth();
        int imH = mapGrid.getHeight();

        BufferedImage mapGridGradient = new BufferedImage(imW, imH, BufferedImage.TYPE_INT_ARGB);

        graphics2D = mapGridGradient.createGraphics();
        graphics2D.drawImage(mapGrid,0,0,null);
        graphics2D.drawImage(gradient,mapGrid.getWidth() , 0,null);

        graphics2D.dispose();

        File mapGridGradientFile = new File("./Data/mapGridGradient.png");
        mapGridGradientFile.deleteOnExit();
        ImageIO.write(mapGridGradient, "PNG", mapGridGradientFile);
    }

    void RSSIHeatMapCreator(List<String[]> all_vehicles_data) throws IOException {

        double[][] map = new double[4][10];
        int[][] count = new int[4][10];
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 10; j++){
                count[i][j] = 0;
                map[i][j] = 0.0;
            }
        }
        String[] line;
        int RSSI;
        double latitude;
        double longitude;

        for (String[] all_vehicles_datum : all_vehicles_data) {

            line = all_vehicles_datum;
            RSSI = Integer.parseInt(line[6].trim());
            latitude = Double.parseDouble(line[2].trim());
            longitude = Double.parseDouble(line[3].trim());

            if (latitude < min_lat || latitude > max_lat || longitude < min_long || longitude > max_long)
                continue;
            else {
                int x = (int) Math.floor((latitude - min_lat) / lat_margin);
                int y = (int) Math.floor((longitude - min_long) / long_margin);

                map[3 - x][y] += RSSI;
                count[3 - x][y]++;
            }
        }

        this.RSSI = new double[4][10];

        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 10; j++){

                if(map[i][j] == 0.0 || count[i][j] == 0){
                    map[i][j] = 0.0;
                }
                else {
                    map[i][j] = map[i][j] / count[i][j];
                }

                this.RSSI[i][j] = map[i][j];
            }
        }

        System.out.println("RSSI Heatmap:");
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 10; j++){
                System.out.print(this.RSSI[i][j] + " ");
            }
            System.out.println();
        }

        System.out.println();

        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 10; j++){

                if(map[i][j] == 0.0){
                    map[i][j] = 0.0;
                }
                else {
                    map[i][j] = ((map[i][j] - 20) / 80) * 100;
                }
            }
        }

        HeatChart RSSI_map = new HeatChart(map);

        RSSI_map.setHighValueColour(Color.GREEN);
        RSSI_map.setLowValueColour(Color.RED);
        RSSI_map.setColourScale(HeatChart.SCALE_LINEAR);

        Dimension dim = new Dimension();
        dim.width = 134;
        dim.height =69;
        RSSI_map.setCellSize(dim);
        RSSI_map.setShowXAxisValues(false);
        RSSI_map.setShowYAxisValues(false);
        RSSI_map.setAxisThickness(0);
        RSSI_map.setChartMargin(0);

        File RSSImapFile = new File("./Data/RSSI-heat-chart.png");
        RSSImapFile.deleteOnExit();
        RSSI_map.saveToFile(RSSImapFile);

        File mapGridGradientFile = new File("./Data/mapGridGradient.png");
        mapGridGradientFile.deleteOnExit();
        BufferedImage mapI = ImageIO.read(mapGridGradientFile);
        BufferedImage heatChart = ImageIO.read(RSSImapFile);

        float alpha = 0.5f;
        int compositeRule = AlphaComposite.SRC_OVER;
        AlphaComposite ac;
        int imgW = mapI.getWidth();
        int imgH = mapI.getHeight();
        BufferedImage overlay = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = overlay.createGraphics();
        ac = AlphaComposite.getInstance(compositeRule, alpha);
        g.drawImage(mapI,0,0,null);
        g.setComposite(ac);
        g.drawImage(heatChart,0,0,null);
        g.setComposite(ac);
        ImageIO.write(overlay, "PNG", new File("./Data/mapGridRSSI.png"));
        g.dispose();
    }

    void throughputHeatMapCreator(List<String[]> all_vehicles_data) throws IOException {

        double[][] map = new double[4][10];
        int[][] count = new int[4][10];

        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 10; j++){
                count[i][j] = 0;
                map[i][j] = 0.0;
            }
        }

        String[] line;
        double throughput;
        double latitude;
        double longitude;

        for (String[] all_vehicles_datum : all_vehicles_data) {

            line = all_vehicles_datum;
            throughput = Double.parseDouble(line[7].trim());
            latitude = Double.parseDouble(line[2].trim());
            longitude = Double.parseDouble(line[3].trim());

            if (latitude < min_lat || latitude > max_lat || longitude < min_long || longitude > max_long)
                continue;
            else {
                int x = (int) Math.floor((latitude - min_lat) / lat_margin);
                int y = (int) Math.floor((longitude - min_long) / long_margin);

                map[3 - x][y] += throughput;
                count[3 - x][y]++;
            }
        }

        this.throughput = new double[4][10];

        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 10; j++){

                if(map[i][j] == 0.0 || count[i][j] == 0){
                    map[i][j] = 0.0;
                }
                else {
                    map[i][j] = map[i][j] / count[i][j];
                }

                this.throughput[i][j] = map[i][j];
            }
        }

        System.out.println("Throughput Heatmap:");
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 10; j++){
                System.out.print(this.throughput[i][j] + " ");
            }
            System.out.println();
        }

        System.out.println();

        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 10; j++){

                if(map[i][j] == 0.0){
                    map[i][j] = 0.0;
                }
                else {
                    map[i][j] = ((map[i][j] - 10) / 40) * 100;
                }
            }
        }

        HeatChart throughput_map = new HeatChart(map);

        throughput_map.setHighValueColour(Color.GREEN);
        throughput_map.setLowValueColour(Color.RED);
        throughput_map.setColourScale(HeatChart.SCALE_LINEAR);

        Dimension dim = new Dimension();
        dim.width = 134;
        dim.height =69;
        throughput_map.setCellSize(dim);
        throughput_map.setShowXAxisValues(false);
        throughput_map.setShowYAxisValues(false);
        throughput_map.setAxisThickness(0);
        throughput_map.setChartMargin(0);

        File throughputMapFile = new File("./Data/throughput-heat-chart.png");
        throughputMapFile.deleteOnExit();
        throughput_map.saveToFile(throughputMapFile);

        File mapGridGradientFile = new File("./Data/mapGridGradient.png");
        mapGridGradientFile.deleteOnExit();
        BufferedImage mapI = ImageIO.read(mapGridGradientFile);
        BufferedImage heatChart = ImageIO.read(throughputMapFile);

        float alpha = 0.5f;
        int compositeRule = AlphaComposite.SRC_OVER;
        AlphaComposite ac;
        int imgW = mapI.getWidth();
        int imgH = mapI.getHeight();
        BufferedImage overlay = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = overlay.createGraphics();
        ac = AlphaComposite.getInstance(compositeRule, alpha);
        g.drawImage(mapI,0,0,null);
        g.setComposite(ac);
        g.drawImage(heatChart,0,0,null);
        g.setComposite(ac);
        ImageIO.write(overlay, "PNG", new File("./Data/mapGridThroughput.png"));
        g.dispose();
    }

    double[] getPredictions(double lat_end, double long_end){

        double[] predictions = new double[2];
        if (lat_end < min_lat || lat_end > max_lat || long_end < min_long || long_end > max_long){

            predictions[0] = -1.0;
            predictions[1] = -1.0;
        }
        else {

            int x = (int) Math.floor((lat_end - min_lat) / lat_margin);
            int y = (int) Math.floor((long_end - min_long) / long_margin);

            predictions[0] = this.RSSI[3 - x][y];
            predictions[1] = this.throughput[3 - x][y];
        }

        return predictions;
    }
}

class GridsCanvas extends JPanel {

    int width, height;
    int rows;
    int cols;

    GridsCanvas(int w, int h, int r, int c) {
        setSize(width = w, height = h);
        rows = r;
        cols = c;
    }
    public void paint(Graphics g) {

        int i;
        width = getSize().width;
        height = getSize().height;

        // draw the rows

        int rowHt = (int)Math.round((double)height / (rows));
        for (i = 0; i < rows; i++)
            g.drawLine(0, i * rowHt, width, i * rowHt);

        // draw the columns

        int rowWid = (int)Math.round((double)width / (cols));
        for (i = 0; i < cols; i++)
            g.drawLine(i * rowWid, 0, i * rowWid, height);
    }
}

class GradientPaintLegend extends JComponent {
    @Override
    public void paint(Graphics g) {

        // paint red to green gradient with min/max indications

        Graphics2D g2 = (Graphics2D) g;
        GradientPaint redToGreen = new GradientPaint(0, 0, Color.GREEN,50, 275, Color.RED);
        g2.setPaint(redToGreen);
        g2.fill(new Rectangle2D.Double(0, 0, 50, 275));
        g2.setPaint(Color.BLACK);
        g2.drawString("Max", 15, 15);
        g2.drawString("Min", 15, 265);
    }
}

