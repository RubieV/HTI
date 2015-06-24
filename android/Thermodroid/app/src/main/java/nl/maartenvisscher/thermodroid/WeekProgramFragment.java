package nl.maartenvisscher.thermodroid;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import java.util.List;

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
    private EditText mDayTemperature;
    private EditText mNightTemperature;
    private volatile boolean mViewAttachedToWindow = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_week_program, container, false);
        mMain = (LinearLayout) mView.findViewById(R.id.main);
        mLoading = (RelativeLayout) mView.findViewById(R.id.loading);
        mMessage = (RelativeLayout) mView.findViewById(R.id.message);
        mDayTemperature = (EditText) mView.findViewById(R.id.day_temperature_view);
        mNightTemperature = (EditText) mView.findViewById(R.id.night_temperature_view);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.week_program_recycler_view);
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

        mView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                mViewAttachedToWindow = true;
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
            }
        });

        mDayTemperature.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_NEXT) {
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

        mNightTemperature.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_NEXT) {
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
    public void onResume() {
        super.onResume();
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

    public void addDays(List<Day> days) {
        if (mAdapter == null) {
            return;
        }
        mAdapter.addDays(days);
    }

    private void setTemperatureFromInput(boolean day, TextView input) {
        float temperature = Float.parseFloat(input.getText().toString());
        temperature = (float) Math.round(temperature * 10f) / 10f;
        if (temperature < 5.0f) {
            temperature = 5.0f;
            Toast.makeText(getActivity(), getString(R.string.temperature_too_low),
                    Toast.LENGTH_LONG).show();
        }
        if (temperature > 30.0f) {
            temperature = 30.0f;
            Toast.makeText(getActivity(), getString(R.string.temperature_too_high),
                    Toast.LENGTH_LONG).show();
        }
        setTemperature(day, temperature);
    }

    private void setTemperature(boolean day, float temperature) {
        new Thread(new TemperatureUploader(day, temperature)).start();
    }

    private void showTemperatures(String day, String night) {
        mDayTemperature.setText(day);
        mNightTemperature.setText(night);
    }

    private void connect() {
        if (HeatingSystem.BASE_ADDRESS.equals("")) {
            showConnectionMessage();
            return;
        }
        setVisibleView(mLoading);
        new Thread(new Downloader(this)).start();
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
                Log.e(TAG, "InvalidInputValueException: " + e.getMessage());
            }
            try {
                final String temperature = HeatingSystem.get(attribute);
                if (mDay) {
                    mView.post(new Runnable() {
                        @Override
                        public void run() {
                            mDayTemperature.setText(temperature);
                        }
                    });
                } else {
                    mView.post(new Runnable() {
                        @Override
                        public void run() {
                            mNightTemperature.setText(temperature);
                        }
                    });
                }
            } catch (ConnectException e) {
                Log.e(TAG, "ConnectException: " + e.getMessage());
            }

        }
    }

    private class Downloader implements Runnable {
        private final Fragment mFragment;

        Downloader(Fragment fragment) {
            mFragment = fragment;
        }

        @Override
        public void run() {
            while (!mViewAttachedToWindow) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
            }
            try {
                final WeekProgram weekProgram = HeatingSystem.getWeekProgram();
                final String dayTemperature = HeatingSystem.get("dayTemperature");
                final String nightTemperature = HeatingSystem.get("nightTemperature");
                if (weekProgram == null || dayTemperature == null || nightTemperature == null) {
                    throw new ConnectException("null");
                }
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter = new WeekProgramAdapter(weekProgram, mFragment);
                        mRecyclerView.setAdapter(mAdapter);
                        showTemperatures(dayTemperature, nightTemperature);
                        setVisibleView(mMain);
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
            } catch (CorruptWeekProgramException e) {
                Log.e(TAG, "CorruptWeekProgramException: " + e.getMessage());
                HeatingSystem.setWeekProgram(new WeekProgram());
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
