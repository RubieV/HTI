package nl.maartenvisscher.thermodroid;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import org.thermostatapp.util.HeatingSystem;

import java.net.ConnectException;

/**
 * The thermostat circle showing current and target temperature.
 */
public class ThermostatFragment extends Fragment {
    public static final String TAG = "ThermostatFragment";

    private Thread mDataThread;
    private View mView;
    private TextView mTime;
    private TextView mCurrentTemp;
    private ScrollView mMain;
    private RelativeLayout mLoading;
    private RelativeLayout mMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_thermostat, container, false);
        mTime = (TextView) mView.findViewById(R.id.time);
        mCurrentTemp = (TextView) mView.findViewById(R.id.currentTemp);
        mMain = (ScrollView) mView.findViewById(R.id.main);
        mLoading = (RelativeLayout) mView.findViewById(R.id.loading);
        mMessage = (RelativeLayout) mView.findViewById(R.id.message);

        setVisibleView(mLoading);

        mMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });

        Spinner spinner = (Spinner) mView.findViewById(R.id.targetTemp);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mView.getContext(),
                R.array.target_temperatures, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return mView;
    }

    @Override
    public void onResume() {
        connect();
        super.onResume();
    }

    @Override
    public void onPause() {
        mDataThread.interrupt();
        super.onPause();
    }

    private void showData(String currentTemp, String day, String time) {
        mCurrentTemp.setText(getString(R.string.current_temp, currentTemp));
        mTime.setText(getString(R.string.time, day, time));

        if (mMain.getVisibility() != View.VISIBLE) {
            setVisibleView(mMain);
        }
    }

    private void showConnectionMessage() {
        setVisibleView(mMessage);
    }

    private void setVisibleView(View visibleView) {
        if (visibleView != mMain) mMain.setVisibility(View.GONE);
        if (visibleView != mLoading) mLoading.setVisibility(View.GONE);
        if (visibleView != mMessage) mMessage.setVisibility(View.GONE);
        visibleView.setVisibility(View.VISIBLE);
    }

    private void connect() {
        setVisibleView(mLoading);
        mDataThread = new Thread(new DataRunnable());
        mDataThread.start();
    }

    private class DataRunnable implements Runnable {
        @Override
        public void run() {
            Log.d(TAG, "DataRunnable started");
            while (!Thread.interrupted()) {
                try {
                    final String currentTemperature = HeatingSystem.get("currentTemperature");
                    if (currentTemperature == null) throw new ConnectException("null");
                    final String day = HeatingSystem.get("day");
                    if (day == null) throw new ConnectException("null");
                    final String time = HeatingSystem.get("time");
                    if (time == null) throw new ConnectException("null");
                    mView.post(new Runnable() {
                        @Override
                        public void run() {
                            showData(currentTemperature, day, time);
                        }
                    });
                } catch (ConnectException e) {
                    Log.e(TAG, "ConnectException: " + e.getMessage());
                    mView.post(new Runnable() {
                        @Override
                        public void run() {
                            showConnectionMessage();
                        }
                    });
                    break; // TODO: instead of breaking automatically try to reconnect
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            }
            Log.d(TAG, "DataRunnable interrupted");
        }
    }
}