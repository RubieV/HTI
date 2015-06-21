package nl.maartenvisscher.thermodroid;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
    private Fragment mFragment;

    public WeekProgramAdapter(WeekProgram weekProgram, Fragment fragment) {
        mFragment = fragment;
        setWeekProgram(weekProgram);
    }

    public void clearWeekProgram() {
        notifyItemRangeRemoved(0, mDayPrograms.size());
        mDayPrograms = new ArrayList<>();
        mDayPrograms.add(new DayProgramAdapter(Day.values(), this));
        notifyItemRangeInserted(0, mDayPrograms.size());
        upload();
    }

    public void updateWeekProgram(WeekProgram weekProgram) {
        notifyItemRangeRemoved(0, mDayPrograms.size());
        setWeekProgram(weekProgram);
        notifyItemRangeInserted(0, mDayPrograms.size());
        upload();
    }

    public void addDays(List<Day> days) {
        for (Day day : days) {
            for (int i = 0; i < mDayPrograms.size(); i += 1) {
                DayProgramAdapter dayProg = mDayPrograms.get(i);
                List<Day> dayProgDays = dayProg.getDays();
                if (dayProgDays.remove(day)) {
                    if (dayProgDays.size() == 0) {
                        mDayPrograms.remove(i);
                        notifyItemRemoved(i);
                        i -= 1;
                    } else {
                        notifyItemChanged(i);
                    }
                }
            }
        }
        mDayPrograms.add(new DayProgramAdapter(days.toArray(new Day[days.size()]),
                this));
        notifyItemInserted(mDayPrograms.size() - 1);
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
                mDayPrograms.add(new DayProgramAdapter(day, switches, this));
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

    private void setDayProgramDays(int dayProgramIndex, List<Day> newDays) {
        DayProgramAdapter dayProgram = mDayPrograms.get(dayProgramIndex);
        List<Day> originalDays = dayProgram.getDays();
        List<Day> addedDays = new ArrayList<>();
        List<Day> removedDays = new ArrayList<>();
        for (Day day : newDays) {
            if (!originalDays.contains(day)) {
                addedDays.add(day);
            }
        }
        for (Day day : originalDays) {
            if (!newDays.contains(day)) {
                removedDays.add(day);
            }
        }
        for (Day day : addedDays) {
            for (int i = 0; i < mDayPrograms.size(); i += 1) {
                DayProgramAdapter dayProg = mDayPrograms.get(i);
                List<Day> dayProgDays = dayProg.getDays();
                if (dayProgDays.remove(day)) {
                    if (dayProgDays.size() == 0) {
                        mDayPrograms.remove(i);
                        notifyItemRemoved(i);
                        i -= 1;
                    } else {
                        notifyItemChanged(i);
                    }
                }

            }
        }
        if (removedDays.size() > 0) {
            mDayPrograms.add(new DayProgramAdapter(removedDays.toArray(new Day[removedDays.size()]),
                    this));
            notifyItemInserted(mDayPrograms.size() - 1);
        }
        int newDayProgramIndex = mDayPrograms.indexOf(dayProgram);
        if (newDays.size() == 0) {
            mDayPrograms.remove(newDayProgramIndex);
            notifyItemRemoved(newDayProgramIndex);
        } else {
            dayProgram.setDays(newDays);
            notifyItemChanged(newDayProgramIndex);
        }
        upload();
    }

    @Override
    public WeekProgramAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.week_program_item,
                parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(WeekProgramAdapter.ViewHolder holder, final int position) {
        Context c = holder.mView.getContext();
        Resources r = holder.mView.getResources();
        DayProgramAdapter dayProgram = mDayPrograms.get(position);

        String[] dayTexts = r.getStringArray(R.array.week_days);
        List<Day> days = dayProgram.getDays();
        String dayText = dayTexts[days.get(0).getIndex()];
        for (int i = 1; i < days.size(); i += 1) {
            dayText += ", " + dayTexts[days.get(i).getIndex()];
        }
        holder.mDaysText.setText(dayText);
        holder.mRecyclerView.setAdapter(dayProgram);
        final List<Day> dialogDays = new ArrayList<>(days);
        holder.mDaysEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DaySelectorDialog(dialogDays, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setDayProgramDays(position, dialogDays);
                    }
                }).show(mFragment.getFragmentManager(), "DaySelectorDialog");
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDayPrograms.size();
    }

    public void upload() {
        WeekProgram weekProgram = new WeekProgram();
        for (DayProgramAdapter dayProgram : mDayPrograms) {
            ArrayList<Switch> switches = new ArrayList<>(dayProgram.getSwitches());
            int switchesActive = dayProgram.getSwitchesActive();
            for (Day day : dayProgram.getDays()) {
                weekProgram.setSwitches(WeekProgram.valid_days[day.getIndex()], switches,
                        switchesActive);
            }
        }
        new Thread(new WeekProgramUploader(weekProgram)).start();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView mDaysText;
        ImageButton mDaysEdit;
        RecyclerView mRecyclerView;

        public ViewHolder(View v) {
            super(v);
            mView = v;
            mDaysText = (TextView) v.findViewById(R.id.text_days);
            mDaysEdit = (ImageButton) v.findViewById(R.id.btn_set_days);
            mRecyclerView = (RecyclerView) v.findViewById(R.id.day_program_recycler_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));
        }
    }

    private class WeekProgramUploader implements Runnable {
        WeekProgram mWeekProgram;

        WeekProgramUploader(WeekProgram weekProgram) {
            mWeekProgram = weekProgram;
        }

        @Override
        public void run() {
            HeatingSystem.setWeekProgram(mWeekProgram);
        }
    }
}