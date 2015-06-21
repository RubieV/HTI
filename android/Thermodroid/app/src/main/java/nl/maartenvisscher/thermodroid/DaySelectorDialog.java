package nl.maartenvisscher.thermodroid;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import java.util.List;

/**
 * Created by Ruben on 21-6-2015.
 */

@SuppressLint("ValidFragment")
class DaySelectorDialog extends DialogFragment {

    private List<Day> mDays;
    private DialogInterface.OnClickListener mListener;

    DaySelectorDialog(List<Day> days, DialogInterface.OnClickListener listener) {
        mDays = days;
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        boolean[] selectedItems = new boolean[7];
        for (Day day : mDays) {
            selectedItems[day.getIndex()] = true;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick days")
                .setMultiChoiceItems(R.array.week_days, selectedItems,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    mDays.add(Day.values()[which]);
                                } else {
                                    mDays.remove(Day.values()[which]);
                                }
                            }
                        })
                .setPositiveButton("Set", mListener)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}