package nl.maartenvisscher.thermodroid;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.thermostatapp.util.HeatingSystem;
import org.thermostatapp.util.InvalidInputValueException;

import java.net.ConnectException;

/**
 * The thermostat circle showing current and target temperature.
 */
public class ThermostatFragment extends Fragment {
    public static final String TAG = "ThermostatFragment";
    public static final float DELTA = 0.5f;

    private float mTargetTemperature = 0;
    private boolean mLocked;
    private volatile boolean mInterrupt;
    private Thread mDataThread;
    private View mView;
    private TextView mTime;
    private TextView mCurrentTemp;
    private EditText mTargetInput;
    private FrameLayout mMain;
    private RelativeLayout mLoading;
    private RelativeLayout mMessage;
    private Button mTempUpButton;
    private Button mTempDownButton;
    private Button mLockButton;

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
        mTargetInput = (EditText) mView.findViewById(R.id.target_temp);
        mMain = (FrameLayout) mView.findViewById(R.id.main);
        mLoading = (RelativeLayout) mView.findViewById(R.id.loading);
        mMessage = (RelativeLayout) mView.findViewById(R.id.message);
        FloatingActionButton weekProgramButton = (FloatingActionButton)
                mView.findViewById(R.id.weekProgramButton);
        mTempUpButton = (Button) mView.findViewById(R.id.btn_temp_up);
        mTempDownButton = (Button) mView.findViewById(R.id.btn_temp_down);
        mLockButton = (Button) mView.findViewById(R.id.btn_lock);
        final LinearLayout focusableLayout = (LinearLayout)
                mView.findViewById(R.id.focusable_layout);

        setVisibleView(mLoading);

        mMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });
        weekProgramButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), WeekProgramActivity.class);
                startActivity(intent);
            }
        });
        mTempUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseTemperature();
            }
        });
        mTempDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseTemperature();
            }
        });
        mTargetInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    setTemperatureFromInput();
                    focusableLayout.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(focusableLayout.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        mLockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLocked();
            }
        });
        return mView;
    }

    @Override
    public void onStart() {
        connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mInterrupt = true;
        super.onStop();
    }

    private void showData(String currentTemp, String day, String time) {
        mCurrentTemp.setText(getString(R.string.current_temp, currentTemp));
        mTime.setText(getString(R.string.time, day, time));

        if (mMain.getVisibility() != View.VISIBLE) {
            setVisibleView(mMain);
        }
    }

    private void showTargetTemperature() {
        mTargetInput.setText(String.valueOf(mTargetTemperature));
        if (mTargetTemperature < 5.0f + DELTA) {
            mTempDownButton.setEnabled(false);
        } else {
            mTempDownButton.setEnabled(true);
        }
        if (mTargetTemperature > 30.0f - DELTA) {
            mTempUpButton.setEnabled(false);
        } else {
            mTempUpButton.setEnabled(true);
        }
    }

    private void showConnectionMessage() {
        setVisibleView(mMessage);
    }

    private void increaseTemperature() {
        setTemperature(mTargetTemperature + DELTA);
    }

    private void decreaseTemperature() {
        setTemperature(mTargetTemperature - DELTA);
    }

    private void setTemperatureFromInput() {
        float temperature;
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

    private void setTemperature(float temperature) {
        mTargetTemperature = temperature;
        new Thread(new TargetTemperatureRunnable(mTargetTemperature)).start();
        showTargetTemperature();
    }

    private void toggleLocked() {
        setLocked(!mLocked);
    }

    private void setLocked(boolean locked) {
        mLocked = locked;
        new Thread(new LockedRunnable(locked)).start();
        showLockedState();
    }

    private void showLockedState() {
        if (mLocked) {
            mTempUpButton.setEnabled(false);
            mTempDownButton.setEnabled(false);
            mTargetInput.setEnabled(false);
            mLockButton.setText(getString(R.string.set_unlocked));
            mLockButton.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_lock_open_black_24dp, 0, 0, 0);
            if (Build.VERSION.SDK_INT >= 17) {
                mLockButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_lock_open_black_24dp, 0, 0, 0);
            }
        } else {
            showTargetTemperature();
            mTargetInput.setEnabled(true);
            mLockButton.setText(getString(R.string.set_locked));
            mLockButton.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_lock_black_24dp, 0, 0, 0);
            if (Build.VERSION.SDK_INT >= 17) {
                mLockButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_lock_black_24dp, 0, 0, 0);
            }
        }
    }

    private void setVisibleView(View visibleView) {
        if (visibleView != mMain) mMain.setVisibility(View.GONE);
        if (visibleView != mLoading) mLoading.setVisibility(View.GONE);
        if (visibleView != mMessage) mMessage.setVisibility(View.GONE);
        visibleView.setVisibility(View.VISIBLE);
    }

    private void connect() {
        setVisibleView(mLoading); // TODO, low priority: do not show loading screen when restarted (only when started for first time)
        mInterrupt = false;
        mDataThread = new Thread(new DataRunnable());
        mDataThread.start();
    }

    private class TargetTemperatureRunnable implements Runnable {
        private float mTemperature;

        TargetTemperatureRunnable(float temperature) {
            mTemperature = temperature;
        }

        @Override
        public void run() {
            try {
                HeatingSystem.put("currentTemperature", String.valueOf(mTemperature));
            } catch (InvalidInputValueException e) {
                // Todo show message
            }
        }
    }

    private class LockedRunnable implements Runnable {
        private boolean mIsLocked;

        LockedRunnable(boolean locked) {
            mIsLocked = locked;
        }

        @Override
        public void run() {
            try {
                String locked = (mIsLocked) ? "off" : "on";
                HeatingSystem.put("weekProgramState", locked);
            } catch (InvalidInputValueException e) {
                // Todo show message
            }
        }
    }

    private class DataRunnable implements Runnable {
        @Override
        public void run() {
            boolean firstRun = true;
            while (!mInterrupt) {
                try {
                    final boolean firstRunFinal = firstRun;
                    if (firstRun) {
                        String targetTemperature = HeatingSystem.get("targetTemperature");
                        if (targetTemperature == null) throw new ConnectException("null");
                        String weekProgramState = HeatingSystem.get("weekProgramState");
                        if (weekProgramState == null) throw new ConnectException("null");
                        mTargetTemperature = Float.parseFloat(targetTemperature);
                        mLocked = !weekProgramState.equals("on");
                    }
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
                            if (firstRunFinal) {
                                showTargetTemperature();
                                showLockedState();
                            }
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
                    break; // TODO: instead of breaking, automatically try to reconnect (just remove this break, but leave it for now)
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
                firstRun = false;
            }
        }
    }
}