package nl.maartenvisscher.thermodroid;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * The thermostat circle showing current and target temperature.
 */
public class ThermostatFragment extends Fragment {

    public ThermostatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_thermostat, container, false);
    }
}
