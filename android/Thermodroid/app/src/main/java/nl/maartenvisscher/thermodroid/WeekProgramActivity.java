package nl.maartenvisscher.thermodroid;

        import android.annotation.SuppressLint;
        import android.app.Activity;
        import android.app.Dialog;
        import android.app.DialogFragment;
        import android.content.DialogInterface;
        import android.os.Bundle;
        import android.support.v7.app.AlertDialog;
        import android.support.v7.app.AppCompatActivity;
        import android.view.Menu;
        import android.view.MenuInflater;
        import android.view.MenuItem;

        import org.thermostatapp.util.WeekProgram;


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
                .setPositiveButton("Restore week proram to default", new DialogInterface.OnClickListener() {
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
        builder.setMessage(R.string.rest_week_program)
                .setPositiveButton("Delete all", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        _fragment.deleteWeekProgram();
                    }
                })
                .setNegativeButton("Keep week program", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}