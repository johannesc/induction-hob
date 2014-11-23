package test.androidapp;

import ioio.lib.util.android.IOIOActivity;

import java.util.List;

import test.androidapp.InductionController.Gui;
import test.androidapp.InductionController.TemperatureReading;
import test.androidapp.InductionService.InductionBinder;
import test.androidapp.ui.Zone;
import test.androidapp.ui.ZoneFragment;
import test.androidapp.ui.ZoneFragment.ZoneFramgentListener;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This is the main activity of the HelloIOIO example application.
 *
 * It displays a toggle button on the screen, which enables control of the
 * on-board LED. This example shows a very simple usage of the IOIO, by using
 * the {@link IOIOActivity} class. For a more advanced use case, see the
 * HelloIOIOPower example.
 */
public class MainActivity extends Activity implements Gui, ZoneFramgentListener {
    private static final String FRAGMENT_ZONE = "dialog";

    public static final String LOG_TAG = "INDUCTION";

//    private static final int NOTIFICATION_ID = 1;
    private InductionService inductionService;

    InductionController inductionController;

    private final Zone[] zones = new Zone[4];

    private TextView temperatureTextView;

    private boolean bound;

    private ImageView connectedBox;
    private ImageView poweredBox;

    Intent serviceIntent;
    NotificationCompat.Builder notificationBuilder;
    NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.w(LOG_TAG, "onCreate:getIntent()=" + getIntent());
        serviceIntent = new Intent(this, InductionService.class);
        super.onCreate(savedInstanceState);
        startService(serviceIntent);
        setContentView(R.layout.activity_main);

        final Zone leftBackZone = (Zone) findViewById(R.id.leftBackZone);
        final Zone rightBackZone = (Zone) findViewById(R.id.rightBackZone);
        final Zone leftFrontZone = (Zone) findViewById(R.id.leftFrontZone);
        final Zone rightFrontZone = (Zone) findViewById(R.id.rightFrontZone);

        zones[0] = leftFrontZone;
        zones[1] = leftBackZone;
        zones[2] = rightBackZone;
        zones[3] = rightFrontZone;

        for (int zoneIndex = 0; zoneIndex < zones.length; zoneIndex++) {
            Zone zone = zones[zoneIndex];
            final int finalZoneIndex = zoneIndex;
            zone.setOnClickListener(new View.OnClickListener() {
               @Override
                public void onClick(View v) {
                   FragmentTransaction ft = getFragmentManager().beginTransaction();
                   Fragment prev = getFragmentManager().findFragmentByTag(FRAGMENT_ZONE);
                   if (prev != null) {
                       Log.w(LOG_TAG, "prev!=null");
                       ft.remove(prev);
                   } else {
                       Log.w(LOG_TAG, "prev=====null");
                   }
                   ft.addToBackStack(null);

                   // Create and show the dialog.
                   DialogFragment newFragment = ZoneFragment.newInstance(finalZoneIndex, 0, 0); //TODO
                   newFragment.show(ft, FRAGMENT_ZONE);
                }
            });
        }

        temperatureTextView = (TextView) findViewById(R.id.textViewTemperature);
        connectedBox = (ImageView) findViewById(R.id.connectedBox);
        poweredBox = (ImageView) findViewById(R.id.poweredBox);

        Log.w(LOG_TAG, "onCreate - done");
    }

    @Override
    protected void onStart() {
        Log.w(LOG_TAG, "onStart");
        super.onStart();
        Log.w(LOG_TAG, "super.onStart done");
        Intent intent = new Intent(this, InductionService.class);
        Log.w(LOG_TAG, "binding to service...");
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.w(LOG_TAG, "onStart - done");
    }

    @Override
    protected void onStop() {
        Log.w(LOG_TAG, "onStop");
        super.onStop();
        if (bound) {
            bound = false;
            unbindService(serviceConnection);
            if (isFinishing()) {
              // removeNotification();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.w(LOG_TAG, "onNewIntent!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            Log.w(LOG_TAG, "onDestroy - finishing - stopping servive");
            stopService(serviceIntent);
        } else {
            Log.w(LOG_TAG, "onDestroy - NOT finishing!");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w(LOG_TAG, "onPause");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.w(LOG_TAG, "onRestart!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(LOG_TAG, "onResume!");
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(LOG_TAG, "onServiceDisconnected:" + name);
            inductionController.removeAllGuis();
            inductionController = null;
            inductionService = null;
            bound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.w(LOG_TAG, "onServiceConnected:" + name);
            InductionBinder inductionBinder = (InductionBinder) service;
            inductionService = inductionBinder.getService();
            inductionController = inductionService.getInductionController();
            inductionController.addGui(MainActivity.this);
            bound = true;
        }
    };

    /*
     * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
     * menu; this adds items to the action bar if it is present.
     * getMenuInflater().inflate(R.menu.activity_main, menu); return true; }
     */

    @Override
    public void setCurrentPowerLevels(final int[] powerLevels) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int zone = 0; zone < powerLevels.length; zone++) {
                    int level = powerLevels[zone];
                    zones[zone].setCurrentLevel(level);
                }
                //programButton.setChecked(programRunning[currentPlate]);
            }
        });
    }

    @Override
    public void setCurrentTargetPowerLevels(final int[] powerLevels) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               for (int zone = 0; zone < powerLevels.length; zone++) {
                    zones[zone].setTargetPowerLevel(powerLevels[zone]);
                }
            }
        });
    }

    @Override
    public void setTemperature(final List<TemperatureReading> temperatures) {
        System.out.println("setTemperature in UI");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StringBuilder tempString = new StringBuilder();

                String sep = "";
                for (TemperatureReading value : temperatures) {
                    if (value.valid) {
                        tempString.append(sep);
                        long age = (SystemClock.elapsedRealtime() - value.reportedTime) / 1000;
                        tempString.append("(" + age + "s) ");
                        tempString.append(Integer.toHexString(value.address & 0xFF)).append(":");
                        tempString.append(Integer.toString(value.temperature)).append("\u2103");
                        sep = " ";
                    }
                }
                temperatureTextView.setText(tempString);
                //updateNotification();
                inductionService.updateNotification("Click to show " + tempString);
            }
        });
    }

    @Override
    public void setHot(final boolean[] hot) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int zone = 0; zone < hot.length; zone++) {
                    zones[zone].setHot(hot[zone]);
                }
            }
        });
    }

    @Override
    public void setPotPresent(final boolean[] potPresent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int zone = 0; zone < potPresent.length; zone++) {
                    zones[zone].setPotPresent(potPresent[zone]);
                }
            }
        });
    }

    @Override
    public void setConnected(final boolean connected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (connected) {
                    connectedBox.setImageResource(R.drawable.blue);
                } else {
                    connectedBox.setImageResource(R.drawable.empty);
                }
            }
        });
    }

    @Override
    public void setIsPowered(final boolean powered) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Zone zone : zones) {
                    zone.setPowered(powered);
                }
                if (powered) {
                    poweredBox.setImageResource(R.drawable.red);
                } else {
                    poweredBox.setImageResource(R.drawable.empty);
                }
            }
        });
    }

    @Override
    public InductionController getInductionController() {
        return inductionController;
    }

    @Override
    public void programRunning(final ZoneController[] running) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               for (int zoneIndex = 0; zoneIndex < zones.length; zoneIndex++) {
                    Zone zone = zones[zoneIndex];
                    zone.setProgramRunning(running[zoneIndex]);
                }
            }
        });
    }

    @Override
    public void setUnusedTemps(List<TemperatureReading> unusedTemperatures) {
        // TODO Auto-generated method stub
    }
}