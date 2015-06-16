package nl.maartenvisscher.thermodroid;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.thermostatapp.util.HeatingSystem;

/**
 * The thermostat circle showing current and target temperature.
 */
public class ThermostatFragment extends Fragment {

    private View view;

    public ThermostatFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Log.v("ThermostatFragment", "I'm running my cute little thread :)");
                    if (view != null) {
                        try {
                            final String currentTemperature = HeatingSystem.get("currentTemperature");
                            view.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (view.findViewById(R.id.currentTempLoading).isShown()) {
                                        view.findViewById(R.id.currentTempLoading).setVisibility(View.GONE);
                                        view.findViewById(R.id.currentTemp).setVisibility(View.VISIBLE);
                                    }
                                    TextView currentTemp = (TextView) view.findViewById(R.id.currentTemp);
                                    currentTemp.setText(currentTemperature + " \u00B0C");
                                }
                            });
                        } catch (Exception e) {
                            Log.e("ThermostatFragment", "Error from getdata " + e);
                        }
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_thermostat, container, false);
        Spinner spinner = (Spinner) view.findViewById(R.id.targetTemp);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                R.array.target_temperatures, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return view;
    }
}
