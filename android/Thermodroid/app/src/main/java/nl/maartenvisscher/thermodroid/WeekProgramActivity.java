package nl.maartenvisscher.thermodroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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
                mFragment.restoreWeekProgram(); // TODO first show dialog
                return true;
            case R.id.action_delete:
                mFragment.deleteWeekProgram(); // TODO first show dialog
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
