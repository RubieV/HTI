package nl.maartenvisscher.thermodroid;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.thermostatapp.util.Switch;

import java.util.ArrayList;
import java.util.Arrays;
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
    private WeekProgramAdapter mAdapter;

    public DayProgramAdapter(Day[] days, WeekProgramAdapter adapter) {
        mDays = new ArrayList<>();
        mDays.addAll(Arrays.asList(days));
        mDayProgram = new ArrayList<>();
        mAdapter = adapter;
        updateDayLayout(false);
    }

    public DayProgramAdapter(Day day, List<Switch> dayProgram, WeekProgramAdapter adapter) {
        mDays = new ArrayList<>();
        mDays.add(day);
        mDayProgram = switchesToPeriods(dayProgram);
        mAdapter = adapter;
        updateDayLayout(false);
    }

    /**
     * Checks if given day program is equal to the day program of this adapter instance.
     *
     * @param dayProgram the day program to compare with.
     * @return true if equal, false otherwise.
     */
    public boolean equals(List<Switch> dayProgram) {
        List<Period> periods = switchesToPeriods(dayProgram);

        if (periods.size() != mDayProgram.size()) {
            return false;
        }
        for (int i = 0; i < periods.size(); i += 1) {
            int[] begin = mDayProgram.get(i).getBeginTime();
            int[] end = mDayProgram.get(i).getEndTime();
            int[] otherBegin = periods.get(i).getBeginTime();
            int[] otherEnd = periods.get(i).getEndTime();
            if (begin[0] != otherBegin[0] || begin[1] != otherBegin[1]
                    || end[0] != otherEnd[0] || end[1] != otherEnd[1]) {
                return false;
            }
        }
        return true;
    }

    public void addDay(Day day) {
        mDays.add(day);
    }

    public List<Day> getDays() {
        return mDays;
    }

    public void setDays(List<Day> days) {
        mDays = days;
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
        List<Switch> switches = new ArrayList<>();
        for (Period period : periods) {
            String beginTime = period.getBeginText();
            if (beginTime.length() == 4) {
                beginTime = "0" + beginTime;
            }
            String endTime = period.getEndText();
            if (endTime.length() == 4) {
                endTime = "0" + endTime;
            }
            switches.add(new Switch("day", true, beginTime));
            switches.add(new Switch("night", true, endTime));
        }
        while (switches.size() < 10) {
            switches.add(new Switch("day", false, "01:00"));
            switches.add(new Switch("night", false, "01:00"));
        }
        return switches;
    }

    private int[] timeStringToInt(String time) {
        String front = time.substring(0, 2);
        String back = time.substring(3, 5);
        return new int[]{Integer.parseInt(front), Integer.parseInt(back)};
    }

    public List<Switch> getSwitches() {
        return periodsToSwitches(mDayProgram);
    }

    public int getSwitchesActive() {
        return 2 * mDayProgram.size();
    }

    private void updateDayLayout(boolean hasChanged) {
        List<DayPart> dayLayout = new ArrayList<>();
        if (mDayProgram.size() == 0) {
            Period period = new Period();
            period.setBeginTime(new int[]{0, 0});
            period.setEndTime(new int[]{24, 0});
            dayLayout.add(new DayPart(period, false, 0));
        } else {
            if (!mDayProgram.get(0).isStartOfDay()) {
                Period period = new Period(); // (Optional) first night mPeriod
                period.setBeginTime(new int[]{0, 0});
                period.setEndTime(mDayProgram.get(0).getBeginTime());
                dayLayout.add(new DayPart(period, false, 0));
            }
            dayLayout.add(new DayPart(mDayProgram.get(0), true, 0)); // First day mPeriod
            for (int i = 1; i < mDayProgram.size(); i += 1) {
                Period period = new Period(); // Intermediate night mPeriod
                period.setBeginTime(mDayProgram.get(i - 1).getEndTime());
                period.setEndTime(mDayProgram.get(i).getBeginTime());
                dayLayout.add(new DayPart(period, false, i));
                dayLayout.add(new DayPart(mDayProgram.get(i), true, i)); // Intermediate day mPeriod
            }
            if (!mDayProgram.get(mDayProgram.size() - 1).isEndOfDay()) {
                Period period = new Period();
                period.setBeginTime(mDayProgram.get(mDayProgram.size() - 1).getEndTime());
                period.setEndTime(new int[]{24, 0});
                dayLayout.add(new DayPart(period, false, mDayProgram.size())); // Last night period
            }
        }
        mDayLayout = dayLayout;
        if (hasChanged) {
            upload();
        }
    }

    private void upload() {
        mAdapter.upload();
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

    private void updateRecyclerHeightAnimated() {
        if (mObserver == null) {
            return;
        }
        Resources r = mObserver.getResources();
        int itemHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                r.getDisplayMetrics());
        final int currentHeight = mObserver.getHeight();
        final int targetHeight = mDayLayout.size() * itemHeight;
        final Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolated, Transformation t) {
                mObserver.getLayoutParams().height = currentHeight + (int) (interpolated *
                        (targetHeight - currentHeight));
                mObserver.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        a.setDuration(mObserver.getItemAnimator().getMoveDuration()); // Todo: fix animation.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mObserver.startAnimation(a);

            }
        }, mObserver.getItemAnimator().getRemoveDuration());
    }

    private void remove(int position) {
        mDayProgram.remove(mDayLayout.get(position).mProgramPosition);
        int dayLayoutSizeBefore = mDayLayout.size();
        updateDayLayout(true);
        int dayLayoutSizeAfter = mDayLayout.size();
        notifyItemRangeRemoved(position, dayLayoutSizeBefore - dayLayoutSizeAfter);
        notifyItemRangeChanged(0, mDayLayout.size());
        updateRecyclerHeightAnimated();
    }

    private void add(int position) {
        DayPart night = mDayLayout.get(position);
        int offset = night.mPeriod.getDuration() / 3;
        Period day = new Period();
        day.setBeginTime(timeAddDuration(night.mPeriod.getBeginTime(), offset));
        day.setEndTime(timeAddDuration(night.mPeriod.getBeginTime(), 2 * offset));
        mDayProgram.add(night.mProgramPosition, day);
        updateDayLayout(true);
        notifyItemRangeInserted(position + 1, 2);
        notifyItemRangeChanged(0, mDayLayout.size());
        updateRecyclerHeight(); // Todo: probably add animation.
    }

    private void showTimeDialog(final int position, final boolean beginTime, Context context) {
        DayPart day = mDayLayout.get(position);
        final int[] rangeBegin;
        final int[] rangeEnd;
        int[] time;
        if (beginTime) {
            if (day.mPeriod.isStartOfDay()) {
                rangeBegin = day.mPeriod.getBeginTime();
            } else {
                rangeBegin = mDayLayout.get(position - 1).mPeriod.getBeginTime();
            }
            rangeEnd = timeAddDuration(day.mPeriod.getEndTime(), -1);
            time = day.mPeriod.getBeginTime();
        } else {
            rangeBegin = timeAddDuration(day.mPeriod.getBeginTime(), 1);
            if (day.mPeriod.isEndOfDay()) {
                rangeEnd = day.mPeriod.getEndTime();
            } else {
                rangeEnd = mDayLayout.get(position + 1).mPeriod.getEndTime();
            }
            time = day.mPeriod.getEndTime();
        }
        TimePickerDialog timePicker = new TimePickerDialog(context,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (inRange(new int[]{hourOfDay, minute}, rangeBegin, rangeEnd)) {
                            updateTime(position, beginTime, hourOfDay, minute);
                        } else {
                            Toast.makeText(view.getContext(),
                                    view.getResources().getString(R.string.time_out_of_range),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }, time[0], time[1], true);
        timePicker.show();
    }

    private boolean inRange(int[] time, int[] rangeBegin, int[] rangeEnd) {
        if (time[0] < rangeBegin[0] || (time[0] == rangeBegin[0] && time[1] < rangeBegin[1])) {
            return false;
        } else if (time[0] > rangeEnd[0] || (time[0] == rangeEnd[0] && time[1] > rangeEnd[1])) {
            return false;
        }
        return true;
    }

    private void updateTime(int position, boolean beginTime, int hourOfDay, int minute) {
        int programPosition = mDayLayout.get(position).mProgramPosition;
        Period period = mDayProgram.get(programPosition);
        if (beginTime) {
            period.setBeginTime(new int[]{hourOfDay, minute});
        } else {
            period.setEndTime(new int[]{hourOfDay, minute});
        }
        int dayLayoutSizeBefore = mDayLayout.size();
        updateDayLayout(true);
        int dayLayoutSizeAfter = mDayLayout.size();
        notifyItemChanged(position);
        int changed = (beginTime) ? position - 1 : position + 1;
        if (dayLayoutSizeAfter < dayLayoutSizeBefore) {
            notifyItemRemoved(changed);
            notifyItemRangeChanged(0, mDayLayout.size());
            updateRecyclerHeightAnimated();
        } else if (dayLayoutSizeAfter > dayLayoutSizeBefore) {
            notifyItemInserted(changed);
            notifyItemRangeChanged(0, mDayLayout.size());
            updateRecyclerHeight();
        } else {
            notifyItemChanged(changed);
        }
    }

    @Override
    public DayProgramAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout;
        boolean dayPeriod;
        if (viewType == 1) {
            layout = R.layout.day_program_item_day;
            dayPeriod = true;
        } else {
            layout = R.layout.day_program_item_night;
            dayPeriod = false;
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v, dayPeriod);
    }

    @Override
    public void onBindViewHolder(DayProgramAdapter.ViewHolder holder, final int position) {
        Resources res = holder.mView.getResources();
        DayPart part = mDayLayout.get(position);
        Period p = part.mPeriod;
        final Context c = holder.mView.getContext();

        if (holder.mDayPeriod) {
            holder.mBtnBeginTime.setText(p.getBeginText());
            holder.mBtnEndTime.setText(p.getEndText());
            holder.mBtnBeginTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTimeDialog(position, true, c);
                }
            });
            holder.mBtnEndTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTimeDialog(position, false, c);
                }
            });
            holder.mBtnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(position);
                }
            });
        } else {
            if (mDayLayout.size() == 1) {
                holder.mTextView.setText(res.getString(R.string.day_program_empty));
            } else {
                holder.mTextView.setText(res.getString(R.string.week_program_period,
                        p.getBeginText(), p.getEndText()));
            }
            if (p.getDuration() > 10 && mDayProgram.size() < 5) {
                holder.mBtnAdd.setVisibility(View.VISIBLE);
                holder.mBtnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        add(position);
                    }
                });
            } else {
                holder.mBtnAdd.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mDayLayout.get(position).mDayTemperature) {
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

    private int[] timeAddDuration(int[] time, int duration) {
        int[] result = {time[0], time[1]};
        result[1] += duration;
        if (result[1] >= 60) {
            while (result[1] >= 60) {
                result[0] += 1;
                result[1] -= 60;
            }
        } else if (result[1] < 0) {
            while (result[1] < 0) {
                result[0] -= 1;
                result[1] += 60;
            }
        }
        return result;
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
            if (beginTime[1] < 10) {
                return beginTime[0] + ":0" + beginTime[1];
            }
            return beginTime[0] + ":" + beginTime[1];
        }

        public String getEndText() {
            if (endTime[0] == 24) return "0:00";
            if (endTime[1] < 10) {
                return endTime[0] + ":0" + endTime[1];
            }
            return endTime[0] + ":" + endTime[1];
        }

        public int getDuration() {
            return 60 * (endTime[0] - beginTime[0]) + (endTime[1] - beginTime[1]);
        }

        public boolean isStartOfDay() {
            return beginTime[0] == 0 && beginTime[1] == 0;
        }

        public boolean isEndOfDay() {
            return endTime[0] == 24;
        }
    }

    private class DayPart {
        public final Period mPeriod;
        /**
         * True for day temperature, false for night temperature.
         */
        public final boolean mDayTemperature;
        /**
         * Only for parts with day temperature: corresponding position in the day program array.
         */
        public final int mProgramPosition;

        DayPart(Period period, boolean dayTemperature, int programPosition) {
            mPeriod = period;
            mDayTemperature = dayTemperature;
            mProgramPosition = programPosition;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public TextView mTextView;
        public boolean mDayPeriod;
        public ImageButton mBtnAdd;
        public Button mBtnBeginTime;
        public Button mBtnEndTime;
        public ImageButton mBtnRemove;

        public ViewHolder(View v, boolean dayPeriod) {
            super(v);
            mView = v;
            mDayPeriod = dayPeriod;
            if (dayPeriod) {
                mBtnBeginTime = (Button) v.findViewById(R.id.btn_begin_time);
                mBtnEndTime = (Button) v.findViewById(R.id.btn_end_time);
                mBtnRemove = (ImageButton) v.findViewById(R.id.btn_remove_period);
            } else {
                mTextView = (TextView) v.findViewById(R.id.text_view);
                mBtnAdd = (ImageButton) v.findViewById(R.id.btn_add_period);
            }
        }
    }
}