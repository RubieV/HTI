package nl.maartenvisscher.thermodroid;

import android.app.Activity;
import android.os.Bundle;

import org.thermostatapp.util.HeatingSystem;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HeatingSystem.BASE_ADDRESS = "http://wwwis.win.tue.nl/2id40-ws/37";
        HeatingSystem.WEEK_PROGRAM_ADDRESS = HeatingSystem.BASE_ADDRESS + "/weekProgram";
    }
}
