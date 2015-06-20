package nl.maartenvisscher.thermodroid;

import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.thermostatapp.util.HeatingSystem;
import org.thermostatapp.util.Switch;
import org.thermostatapp.util.WeekProgram;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for all days in the week program.
 */
public class WeekProgramAdapter extends RecyclerView.Adapter<WeekProgramAdapter.ViewHolder> {

    private ArrayList<DayProgramAdapter> mDayPrograms;

    public WeekProgramAdapter(WeekProgram weekProgram) {
        mDayPrograms = new ArrayList<>();
        for (String dayName : WeekProgram.valid_days) {
            Day day = stringToDay(dayName);
            List<Switch> switches = weekProgram.data.get(dayName);
            if (day == null || switches == null) {
                continue;
            }
            boolean dayExists = false;
            for (DayProgramAdapter dayProgram : mDayPrograms) {
                if (dayProgram.equals(switches)) {
                    dayProgram.addDay(day);
                    dayExists = true;
                    break;
                }
            }
            if (!dayExists) {
                mDayPrograms.add(new DayProgramAdapter(day, switches));
            }
        }
    }

    private Day stringToDay(String day) {
        for (int i = 0; i < WeekProgram.valid_days.length; i += 1) {
            if (WeekProgram.valid_days[i].equals(day)) {
                return Day.values()[i];
            }
        }
        return null;
    }

    private String daysToString(Resources res, Day[] days) {
        String result = dayToString(res, days[0]);
        for (int i = 1; i < days.length; i += 1) {
            result += ", " + dayToString(res, days[i]);
        }
        return result;
    }

    private String dayToString(Resources res, Day day) {
        return res.getStringArray(R.array.week_days)[day.getIndex()];
    }

    @Override
    public WeekProgramAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.week_program_item,
                parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(WeekProgramAdapter.ViewHolder holder, int position) {
        Resources res = holder.mTextView.getResources();
        DayProgramAdapter dayProgram = mDayPrograms.get(position);

        holder.mTextView.setText(daysToString(res, dayProgram.getDays()));
        holder.mRecyclerView.setAdapter(dayProgram);
    }

    @Override
    public int getItemCount() {
        return mDayPrograms.size();
    }

    public void upload() {
        // TODO: get all switches and start upload thread
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        RecyclerView mRecyclerView;

        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.days);
            mRecyclerView = (RecyclerView) v.findViewById(R.id.day_program_recycler_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));
        }
    }

    private class WeekProgramUploader implements Runnable {
        @Override
        public void run() {
            HeatingSystem.setWeekProgram(new WeekProgram()); // Temporary
        }
    }
}