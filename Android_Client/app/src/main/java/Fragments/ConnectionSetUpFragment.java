package Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.traffic.R;

public class ConnectionSetUpFragment extends Fragment {
    private ConnectionSetUpListener listener;
    private EditText ipAddress;
    private EditText port;

    public interface ConnectionSetUpListener {
        void onInputConnectionSent(String ipAddress, String port);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.connection_setup, container, false);
        ipAddress = ret.findViewById(R.id.ip_address);
        port = ret.findViewById(R.id.port);

        return ret;
    }

    @Override
    public void onPause() {
        super.onPause();

        listener.onInputConnectionSent(ipAddress.getText().toString(), port.getText().toString());

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ConnectionSetUpListener) {
            listener = (ConnectionSetUpListener) context;
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

}
