package nl.maartenvisscher.thermodroid;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.thermostatapp.util.CorruptWeekProgramException;
import org.thermostatapp.util.HeatingSystem;
import org.thermostatapp.util.InvalidInputValueException;
import org.thermostatapp.util.WeekProgram;

import java.net.ConnectException;

/**
 * A placeholder fragment containing a simple view.
 */
public class WeekProgramFragment extends Fragment {
    private static final String TAG = "WeekProgramFragment";

    private View mView;
    private LinearLayout mMain;
    private RecyclerView mRecyclerView;
    private WeekProgramAdapter mAdapter;
    private RelativeLayout mLoading;
    private RelativeLayout mMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_week_program, container, false);
        mMain = (LinearLayout) mView.findViewById(R.id.main);
        mLoading = (RelativeLayout) mView.findViewById(R.id.loading);
        mMessage = (RelativeLayout) mView.findViewById(R.id.message);
        EditText dayTemperature = (EditText) mView.findViewById(R.id.day_temperature_view);
        EditText nightTemperature = (EditText) mView.findViewById(R.id.night_temperature_view);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.week_program_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mView.getContext()));
        final LinearLayout focusableLayout = (LinearLayout)
                mView.findViewById(R.id.focusable_layout);

        setVisibleView(mLoading);

        mMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });

        dayTemperature.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    setTemperatureFromInput(true, v);
                    focusableLayout.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(focusableLayout.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        nightTemperature.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    setTemperatureFromInput(false, v);
                    focusableLayout.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(focusableLayout.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        connect();
    }

    public void restoreWeekProgram() {
        if (mAdapter == null) {
            return; // TODO do in a better/cleaner way
        }
        mAdapter.updateWeekProgram(new WeekProgram());
        mAdapter.upload();
    }

    public void deleteWeekProgram() {
        if (mAdapter == null) {
            return;
        }
        mAdapter.clearWeekProgram();
    }

    private void setTemperatureFromInput(boolean day, TextView input) {

        temperature = Float.parseFloat(mTargetInput.getText().toString());
        temperature = (float) Math.round(temperature * 10f) / 10f;
        if (temperature < 5.0f) {
            temperature = 5.0f;
            Toast.makeText(getActivity(), getString(R.string.temperature_too_low),
                    Toast.LENGTH_SHORT).show();
        }
        if (temperature > 30.0f) {
            temperature = 30.0f;
            Toast.makeText(getActivity(), getString(R.string.temperature_too_high),
                    Toast.LENGTH_SHORT).show();
        }
        setTemperature(temperature);

    }

    private void setTemperature(boolean day, float temperature) {
        new Thread(new TemperatureUploader(day, temperature)).start();
    }

    private void connect() {
        new Thread(new WeekProgramDownloader()).start();
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

    private class TemperatureUploader implements Runnable {

        private boolean mDay;
        private float mTemperature;

        TemperatureUploader(boolean day, float temperature) {
            mDay = day;
            mTemperature = temperature;
        }

        @Override
        public void run() {
            String attribute = (mDay) ? "dayTemperature" : "nightTemperature";
            try {
                HeatingSystem.put(attribute, String.valueOf(mTemperature));
            } catch (InvalidInputValueException e) {
                // Todo maybe?
            }

        }
    }

    private class WeekProgramDownloader implements Runnable {
        @Override
        public void run() {
            WeekProgram weekProgram;
            try {
                weekProgram = HeatingSystem.getWeekProgram();
            } catch (ConnectException e) {
                weekProgram = null;
            } catch (CorruptWeekProgramException e) {
                weekProgram = new WeekProgram();
                HeatingSystem.setWeekProgram(weekProgram);
            }
            final WeekProgram weekProgramFinal = weekProgram;

            if (weekProgramFinal != null) {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter = new WeekProgramAdapter(weekProgramFinal);
                        mRecyclerView.setAdapter(mAdapter);
                        if (mMain.getVisibility() != View.VISIBLE) {
                            setVisibleView(mMain);
                        }
                    }
                });
            } else {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        showConnectionMessage();
                    }
                });
            }
        }
    }
}
