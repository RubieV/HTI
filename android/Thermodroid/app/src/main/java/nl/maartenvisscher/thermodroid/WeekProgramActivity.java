package nl.maartenvisscher.thermodroid;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class WeekProgramActivity extends AppCompatActivity {
    WeekProgramFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_program);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mFragment = (WeekProgramFragment) getFragmentManager().findFragmentById(
                R.id.week_program_fragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_week_program, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                final List<Day> days = new ArrayList<>();
                DaySelectorDialog dialog = new DaySelectorDialog();
                dialog.setDays(days);
                dialog.setOnClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mFragment.addDays(days);
                    }
                });
                dialog.show(getFragmentManager(), "DaySelectorDialog");
                return true;
            case R.id.action_restore:
                new ResetWeekProgramDialogFragment(mFragment).show(mFragment.getFragmentManager(), "");
                return true;
            case R.id.action_delete:
                new DeleteWeekProgramDialogFragment(mFragment).show(mFragment.getFragmentManager(), "");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



}

@SuppressLint("ValidFragment")
class ResetWeekProgramDialogFragment extends DialogFragment  {
    private WeekProgramFragment _fragment;

    ResetWeekProgramDialogFragment(WeekProgramFragment fragment)
    {
        _fragment = fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.rest_week_program)
                .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        _fragment.restoreWeekProgram();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}

@SuppressLint("ValidFragment")
class DeleteWeekProgramDialogFragment extends DialogFragment  {
    private WeekProgramFragment _fragment;

    DeleteWeekProgramDialogFragment(WeekProgramFragment fragment)
    {
        _fragment = fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_week_program)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        _fragment.deleteWeekProgram();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
