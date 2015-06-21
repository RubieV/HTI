package nl.maartenvisscher.thermodroid;

import android.content.ClipData;
import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

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
        setWeekProgram(weekProgram);
    }

    public void clearWeekProgram() {
        notifyItemRangeRemoved(0, mDayPrograms.size());
        mDayPrograms = new ArrayList<>();
        mDayPrograms.add(new DayProgramAdapter(Day.values()));
        notifyItemRangeInserted(0, mDayPrograms.size());
    }

    public void updateWeekProgram(WeekProgram weekProgram) {
        notifyItemRangeRemoved(0, mDayPrograms.size());
        setWeekProgram(weekProgram);
        notifyItemRangeInserted(0, mDayPrograms.size());
    }

    private void setWeekProgram(WeekProgram weekProgram) {
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

    @Override
    public WeekProgramAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.week_program_item,
                parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(WeekProgramAdapter.ViewHolder holder, int position) {
        Context c = holder.mView.getContext();
        Resources r = holder.mView.getResources();
        DayProgramAdapter dayProgram = mDayPrograms.get(position);

        for (Day day : dayProgram.getDays()) {
            String dayText = r.getStringArray(R.array.week_days)[day.getIndex()];
            Button btn = new Button(c);
            btn.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            btn.setText(dayText);
            btn.setOnTouchListener(new DayTouchListener(day)); // Todo: maybe change to long click.
            holder.mDayButtons.addView(btn);
        }
        holder.mRecyclerView.setAdapter(dayProgram);
        holder.mView.setOnDragListener(new MyDragListener());
    }

    @Override
    public int getItemCount() {
        return mDayPrograms.size();
    }

    public void upload() {
        // TODO: get all switches and start upload thread
    }

    // This defines your touch listener
    private class DayTouchListener implements View.OnTouchListener {
        private Day mDay;

        DayTouchListener(Day day) {
            mDay = day;
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("weekday",
                        WeekProgram.valid_days[mDay.getIndex()]);
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(data, shadowBuilder, view, 0);
                view.setVisibility(View.INVISIBLE);
                return true;
            } else {
                return false;
            }
        }
    }

    // Todo continue here
    private class MyDragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    //v.setBackgroundDrawable(enterShape);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    //v.setBackgroundDrawable(normalShape);
                    break;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign View to ViewGroup
                    View view = (View) event.getLocalState();
                    ViewGroup owner = (ViewGroup) view.getParent();
                    owner.removeView(view);
                    LinearLayout container = (LinearLayout) v.findViewById(R.id.day_buttons);
                    container.addView(view);
                    view.setVisibility(View.VISIBLE);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    //v.setBackgroundDrawable(normalShape);
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        LinearLayout mDayButtons;
        RecyclerView mRecyclerView;

        public ViewHolder(View v) {
            super(v);
            mView = v;
            mDayButtons = (LinearLayout) v.findViewById(R.id.day_buttons);
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