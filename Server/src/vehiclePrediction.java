public class vehiclePrediction {
    double timestep;
    double lat;
    double longtitude;
    double throughput;
    double rssi;

    public
    vehiclePrediction(double time, double lati, double longi, double throuputi,double rssii){
        timestep = time;
        lat = lati;
        longtitude = longi;
        throughput = throuputi;
        rssi = rssii;
    }
    double getTime(){
        return timestep;
    }
    double getLat(){
        return lat;
    }
    double getLong(){
        return longtitude;
    }
    double getThroughput(){
        return throughput;
    }
    double getRssi(){
        return rssi;
    }
}
