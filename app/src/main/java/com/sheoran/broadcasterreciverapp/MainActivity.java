package com.sheoran.broadcasterreciverapp;

import androidx.appcompat.app.AppCompatActivity;

import static android.content.ContentValues.TAG;
import static android.content.Intent.ACTION_AIRPLANE_MODE_CHANGED;

import androidx.annotation.RequiresApi;


import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    Switch wifiswitch, internetswitch, airoplaneswitch;
    TextView charging;
    WifiManager wifiManager;
    Context context;
    BatteryManager batteryManager;
    MyBroadCastReceiver myBroadCastReceiver;
    private final String COMMAND_FLIGHT_MODE_1 = "settings put global airplane_mode_on";
    private final String COMMAND_FLIGHT_MODE_2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state";

    // BroadcastReceiver mMessageReceiver=null;
    //  private BroadcastReceiver mMessageReceiver;

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiswitch = findViewById(R.id.wifi);
        internetswitch = findViewById(R.id.internet);
        charging = findViewById(R.id.charging);
        airoplaneswitch = findViewById(R.id.airplane);
        myBroadCastReceiver=new MyBroadCastReceiver();

        //charging = (Switch) this.findViewById(R.id.charging);
        this.registerReceiver(this.chargingreceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        wifiManager=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        internetswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkConnection();
                if (Build.VERSION.SDK_INT<Build.VERSION_CODES.Q)
                    //wifiManager.setWifiEnabled(true);
                    internetswitch.setChecked(true);
                else
                {
                    Intent panelIntent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
                    startActivityForResult(panelIntent,1);
                }
            }
        });
//charging.setOnClickListener(new View.OnClickListener() {
//    @Override
//    public void onClick(View view) {
//    }
//});

        airoplaneswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // @SuppressWarnings("deprecation")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    // API 17 onwards.
                    if (isRooted(context)) {
                        int enabled = isFlightModeEnabled(context) ? 0 : 1;
                        // Set airplane / flight mode using "su" commands.
                        String command = COMMAND_FLIGHT_MODE_1 + " " + enabled;
                        executeCommandViaSu(context, "-c", command);
                        command = COMMAND_FLIGHT_MODE_2 + " " + enabled;
                        executeCommandViaSu(context, "-c", command);
                    } else {
                        try {
                            // No root permission, just show the Airplane / Flight mode setting screen.
                            Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Log.e(TAG, "Setting screen not found due to: " + e.fillInStackTrace());
                        }
                    }
                }


            }
        });

        wifiswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (Build.VERSION.SDK_INT<Build.VERSION_CODES.Q)
                    wifiManager.setWifiEnabled(true);
                else
                {
                    Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                    startActivityForResult(panelIntent,1);
                }
            }
        });
    }

    private static boolean isFlightModeEnabled(Context context) {
        boolean mode = false;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            // API 17 onwards
            mode = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
        } else {
            // API 16 and earlier.
            mode = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
        }
        return mode;
    }

    private boolean isRooted(Context context) {
        return false;
    }

    private void executeCommandViaSu(Context context, String option, String command) {
        boolean success = false;
        String su = "su";
        for (int i=0; i < 3; i++) {
            // "su" command executed successfully.
            if (success) {
                // Stop executing alternative su commands below.
                break;
            }
            if (i == 1) {
                su = "/system/xbin/su";
            } else if (i == 2) {
                su = "/system/bin/su";
            }
            try {
                // Execute command via "su".
                Runtime.getRuntime().exec(new String[]{su, option, command});
            } catch (IOException e) {
                success = false;
                Log.e(TAG, "su command has failed due to: " + e.fillInStackTrace());
            } finally {
                success = true;
            }
        }
    }



    private void showSnackBar(boolean isConnected) {
        // initialize color and message
        String message;
        int color;

        // check condition
        if (isConnected) {
            // when internet is connected

            message = "Connected to Internet";
            // set text color
            color = Color.WHITE;
        }
        else
        {
            // when internet
            // is disconnected
            // set message
            message = "Not Connected to Internet";

            // set text color
            color = Color.RED;
        }

        // initialize snack bar
        Snackbar snackbar = Snackbar.make(findViewById(R.id.charging), message, Snackbar.LENGTH_LONG);

        // initialize view
        View view = snackbar.getView();

        // Assign variable
//        TextView textView = view.findViewById(R.id.snackbar_text);
//
//        // set text color
//        textView.setTextColor(color);

        // show snack bar
        snackbar.show();
    }



    @Override
    protected void onResume() {
        super.onResume();
        // call method
        checkConnection();
        //MainActivity.this.registerReceiver(mMessageReceiver, new    IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
    }

    private void checkConnection() {
        // initialize intent filter
        IntentFilter intentFilter = new IntentFilter();

        // add action
        intentFilter.addAction("android.new.conn.CONNECTIVITY_CHANGE");

        // register receiver
        registerReceiver(new ConnectionReceiver(), intentFilter);

        // Initialize listener
        ConnectionReceiver.Listener = this;

        // Initialize connectivity manager
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Initialize network info
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        // get connection status
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();

        // display snack bar
        showSnackBar(isConnected);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // call method
        checkConnection();
    }



    //
    private BroadcastReceiver chargingreceiver= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = level * 100 / (float)scale;
            charging.setText(String.valueOf(batteryPct) + "%");

            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status ==
                    BatteryManager.BATTERY_STATUS_FULL;
            if (isCharging) {
//            chargingswitch.setChecked(true);
                Toast.makeText(getApplicationContext(), "Charger connected, Battery Charging..",
                        Toast.LENGTH_SHORT).show();
            }
            else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                // chargingswitch.setChecked(false);
                Toast.makeText(getApplicationContext(), "Charger disconnected",
                        Toast.LENGTH_SHORT).show();
            }

        }
    };
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int wifiStateExtra=intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_UNKNOWN);
            switch (wifiStateExtra)
            {
                case WifiManager.WIFI_STATE_ENABLED:
                    wifiswitch.setChecked(true);
                    wifiswitch.setText("wifi is on");
                    break;

                case WifiManager.WIFI_STATE_DISABLED:
                    wifiswitch.setChecked(false);
                    wifiswitch.setText("wifi is off");
                    break;
            }


        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction(ACTION_AIRPLANE_MODE_CHANGED);
        intentFilter1.addAction(Intent.ACTION_POWER_CONNECTED);
        registerReceiver(myBroadCastReceiver,intentFilter1);
        registerReceiver(broadcastReceiver,intentFilter);
        IntentFilter filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(myBroadCastReceiver);
        //MainActivity.this.unregisterReceiver(mMessageReceiver);
    }

    public void onNetworkChange(boolean isConnected) {
        showSnackBar(isConnected);

    }
    public static void setFlightMode(Context context) {
        boolean enabled = isFlightModeEnabled(context);
        Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, enabled ? 0 : 1);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", !enabled);
        context.sendBroadcast(intent);
    }


}