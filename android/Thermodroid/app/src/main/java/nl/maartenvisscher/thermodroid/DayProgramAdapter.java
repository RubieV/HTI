package nl.maartenvisscher.thermodroid;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.thermostatapp.util.Switch;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for all switches on a single day.
 */
public class DayProgramAdapter extends RecyclerView.Adapter<DayProgramAdapter.ViewHolder> {

    public static final String TAG = "DayProgramAdapter";
    private List<Day> mDays;
    /**
     * Holds all periods of the day which have day temperature (i.e. not night temperature).
     */
    private List<Period> mDayProgram;
    /**
     * Read only version of the periods of the day which also includes night periods. Should not be
     * changed.
     */
    private List<DayPart> mDayLayout;
    private RecyclerView mObserver = null;

    public DayProgramAdapter(Day day, List<Switch> dayProgram) {
        mDays = new ArrayList<>();
        mDays.add(day);
        mDayProgram = switchesToPeriods(dayProgram);
        updateDayLayout();
    }

    /**
     * Checks if given day program is equal to the day program of this adapter instance.
     *
     * @param dayProgram the day program to compare with.
     * @return true if equal, false otherwise.
     */
    public boolean equals(List<Switch> dayProgram) {
        return false; // TODO implement this method (check if given dayProgram equals the program of this instance)
    }

    public void addDay(Day day) {
        mDays.add(day);
    }

    public Day[] getDays() {
        return mDays.toArray(new Day[mDays.size()]);
    }

    private List<Period> switchesToPeriods(List<Switch> switches) {
        List<Period> result = new ArrayList<>();
        Period current = null;
        for (Switch s : switches) {
            if (s.getState() && current == null && s.getType().equals("day")) {
                current = new Period();
                current.setBeginTime(timeStringToInt(s.getTime()));
            }
            if (s.getState() && current != null && s.getType().equals("night")) {
                current.setEndTime(timeStringToInt(s.getTime()));
                result.add(current);
                current = null;
            }
        }
        if (current != null) {
            current.setEndTime(new int[]{24, 0});
            result.add(current);
        }
        return result;
    }

    private List<Switch> periodsToSwitches(List<Period> periods) {
        return new ArrayList<>(); // TODO
    }

    private int[] timeStringToInt(String time) {
        String front = time.substring(0, 2);
        String back = time.substring(3, 5);
        return new int[]{Integer.parseInt(front), Integer.parseInt(back)};
    }

    public List<Switch> getSwitches() {
        return periodsToSwitches(mDayProgram);
    }

    private void updateDayLayout() {
        List<DayPart> dayLayout = new ArrayList<>();
        if (mDayProgram.size() == 0) {
            Period period = new Period();
            period.setBeginTime(new int[]{0, 0});
            period.setEndTime(new int[]{24, 0});
            dayLayout.add(new DayPart(period, false));
        } else {
            if (!mDayProgram.get(0).isStartOfDay()) {
                Period period = new Period(); // (Optional) first night period
                period.setBeginTime(new int[]{0, 0});
                period.setEndTime(mDayProgram.get(0).getBeginTime());
                dayLayout.add(new DayPart(period, false));
            }
            dayLayout.add(new DayPart(mDayProgram.get(0), true)); // First day period
            for (int i = 1; i < mDayProgram.size(); i += 1) {
                Period period = new Period(); // Intermediate night period
                period.setBeginTime(mDayProgram.get(i - 1).getEndTime());
                period.setEndTime(mDayProgram.get(i).getBeginTime());
                dayLayout.add(new DayPart(period, false));
                dayLayout.add(new DayPart(mDayProgram.get(i), true)); // Intermediate day period
            }
            if (!mDayProgram.get(mDayProgram.size() - 1).isEndOfDay()) {
                Period period = new Period();
                period.setBeginTime(mDayProgram.get(mDayProgram.size() - 1).getEndTime());
                period.setEndTime(new int[]{24, 0});
                dayLayout.add(new DayPart(period, false));
            }
        }
        mDayLayout = dayLayout;
        Log.d(TAG, "mDayLayout");
        for (DayPart part : dayLayout) {
            int[] begin = part.period.getBeginTime();
            int[] end = part.period.getEndTime();
            String text = begin[0] + "," + begin[1] + "\u2014" + end[0] + "," + end[1];
            if (part.dayTemperature) {
                text += ", day";
            } else {
                text += ", night";
            }
            Log.d(TAG, text);
        }
    }

    private void updateRecyclerHeight() {
        if (mObserver == null) {
            return;
        }
        Resources r = mObserver.getResources();
        int itemHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                r.getDisplayMetrics());
        mObserver.getLayoutParams().height = mDayLayout.size() * itemHeight;
    }

    @Override
    public DayProgramAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = R.layout.day_program_item_night;
        if (viewType == 1) {
            layout = R.layout.day_program_item_day;
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DayProgramAdapter.ViewHolder holder, int position) {
        Resources res = holder.mView.getResources();
        Period p = mDayLayout.get(position).period;
        String t = res.getString(R.string.week_program_period, p.getBeginText(), p.getEndText());
        holder.mTextView.setText(t);
    }

    @Override
    public int getItemViewType(int position) {
        if (mDayLayout.get(position).dayTemperature) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return mDayLayout.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView observer) {
        mObserver = observer;
        updateRecyclerHeight();
    }

    private class Period {
        private int[] beginTime;
        private int[] endTime;

        public int[] getBeginTime() {
            return beginTime;
        }

        public void setBeginTime(int[] time) {
            beginTime = time;
        }

        public int[] getEndTime() {
            return endTime;
        }

        public void setEndTime(int[] time) {
            endTime = time;
        }

        public String getBeginText() {
            return beginTime[0] + ":" + beginTime[1];
        }

        public String getEndText() {
            if (endTime[0] == 24) return "0:00";
            return endTime[0] + ":" + endTime[1];
        }

        public boolean isStartOfDay() {
            return beginTime[0] == 0 && beginTime[1] == 0;
        }

        public boolean isEndOfDay() {
            return endTime[0] == 24;
        }
    }

    private class DayPart {
        public final Period period;
        public final boolean dayTemperature;

        DayPart(Period period, boolean dayTemperature) {
            this.period = period;
            this.dayTemperature = dayTemperature;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public TextView mTextView;

        public ViewHolder(View v) {
            super(v);
            mView = v;
            mTextView = (TextView) v.findViewById(R.id.text_view);
        }
    }
}