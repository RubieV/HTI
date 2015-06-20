package nl.maartenvisscher.thermodroid;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.thermostatapp.util.CorruptWeekProgramException;
import org.thermostatapp.util.HeatingSystem;
import org.thermostatapp.util.WeekProgram;

import java.net.ConnectException;

/**
 * A placeholder fragment containing a simple view.
 */
public class WeekProgramFragment extends Fragment {
    private static final String TAG = "WeekProgramFragment";

    private View mView;
    private RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_week_program, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.week_program_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mView.getContext()));
        return mView;
    }

    @Override
    public void onStart() {
        new Thread(new WeekProgramDownloader()).start();
        super.onStart();
    }

    public void restoreWeekProgram() {
        WeekProgramAdapter adapter = new WeekProgramAdapter(new WeekProgram());
        adapter.upload();
        mRecyclerView.setAdapter(adapter);
    }

    public void deleteWeekProgram() {
        // TODO make empty week program
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
                        mRecyclerView.setAdapter(new WeekProgramAdapter(weekProgramFinal));
                    }
                });
            } else {
                // TODO: show error message.
            }
        }
    }
}
