package nl.maartenvisscher.thermodroid;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.thermostatapp.util.HeatingSystem;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private ThermostatFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFragment = (ThermostatFragment)
                getFragmentManager().findFragmentById(R.id.thermostat_fragment);
        if (HeatingSystem.BASE_ADDRESS.equals("")) {
            HeatingSystem.BASE_ADDRESS = "http://wwwis.win.tue.nl/2id40-ws/37";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_set_server:
                new SelectorDialog().setFragment(mFragment).show(getFragmentManager(),
                        "SelectorDialog");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class SelectorDialog extends DialogFragment {

        private final String[] SERVERS = {"http://wwwis.win.tue.nl/2id40-ws/37",
                "http://pcwin889.win.tue.nl/2id40-ws/37"};
        private int mWhich = 0;
        private ThermostatFragment mFragment;

        private SelectorDialog setFragment(ThermostatFragment fragment) {
            mFragment = fragment;
            return this;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int which = Arrays.asList(SERVERS).indexOf(HeatingSystem.BASE_ADDRESS);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Select server").setSingleChoiceItems(SERVERS, which, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mWhich = which;
                }
            }).setPositiveButton("Set", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    HeatingSystem.BASE_ADDRESS = SERVERS[mWhich];
                    HeatingSystem.WEEK_PROGRAM_ADDRESS = HeatingSystem.BASE_ADDRESS +
                            "/weekProgram";
                    mFragment.connect();
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            return builder.create();
        }
    }
}
