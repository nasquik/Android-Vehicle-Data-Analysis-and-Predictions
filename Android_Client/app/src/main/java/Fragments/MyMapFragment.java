package Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.traffic.Main2Activity;
import com.example.traffic.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.File;


public class MyMapFragment extends Fragment implements OnMapReadyCallback {

    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;

    private int start = 0;

    public MyMapFragment() {
        // Required empty public constructor
    }

    public void updateMapOriginal(MqttMessage msg) {
        TextView rssi_text = mView.findViewById(R.id.rssi_r);
        TextView throughput_text = mView.findViewById(R.id.throughput_r);
        TextView lat_t = mView.findViewById(R.id.Lat_r);
        TextView lon_t = mView.findViewById(R.id.Lon_r);

        Log.e("info", "update map");
        String delims = ",";
        String tokens[] = msg.toString().split(delims);
        Log.e("info",""+tokens.length);

        double lon = Double.parseDouble(tokens[2]);
        Log.e("info", ""+lon);

        double lat = Double.parseDouble(tokens[3]);
        Log.e("info", ""+lat);

        double rssi = Double.parseDouble(tokens[6]);
        Log.e("info", ""+rssi);

        double throughput = Double.parseDouble(tokens[7].substring(0, tokens[7].length()-1));
        Log.e("info", ""+throughput);

        String message = lon + " " + lat;
        Log.e("info", "done");

        Log.i("info", message);



        rssi_text.setText("RSSI R "+ rssi);
        throughput_text.setText("Throughput R "+ String.format("%.2f", throughput));
        lat_t.setText("Lat P " + String.format("%.8f", lat));
        lon_t.setText("Lon P " + String.format("%.8f", lon));


        mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lon,lat))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .title("Current"));
    }


    public void updateMap(MqttMessage msg) {
        if (start == 0) {
            start++;
            return;
        }
        TextView rssi_text = mView.findViewById(R.id.rssi_p);
        TextView throughput_text = mView.findViewById(R.id.throughput_p);
        TextView lat_t = mView.findViewById(R.id.Lat_p);
        TextView lon_t = mView.findViewById(R.id.Lon_p);
        Log.e("info", "update map");
        String delims = ",";
        String tokens[] = msg.toString().split(delims);
        Log.e("info",""+tokens.length);
        int id = Integer.parseInt(tokens[0].substring(1));
        Log.e("info", ""+id);

        double lon = Double.parseDouble(tokens[1]);
        Log.e("info", ""+lon);

        double lat = Double.parseDouble(tokens[2]);
        Log.e("info", ""+lat);

        double rssi = Double.parseDouble(tokens[3]);
        Log.e("info", ""+rssi);

        double throughput = Double.parseDouble(tokens[4].substring(0, tokens[4].length()-1));
        Log.e("info", ""+throughput);


        String message = "" + id + " " + lon + " " + lat + " " + rssi + " " + throughput;
        Log.e("info", "done");

        Log.i("info", message);

        rssi_text.setText("RSSI P "+ String.format("%.2f", rssi));
        throughput_text.setText("Throughput P " + String.format("%.2f", throughput));
        lat_t.setText("Lat P " + String.format("%.8f", lat));
        lon_t.setText("Lon P " + String.format("%.8f", lon));


        mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lon,lat))
                            .title("Prediction"));

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.map_fragment, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMapView = mView.findViewById(R.id.map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

    }
}
