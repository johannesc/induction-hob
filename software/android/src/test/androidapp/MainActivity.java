package test.androidapp;

import ioio.lib.util.android.IOIOActivity;

import java.util.List;

import test.androidapp.InductionController.Gui;
import test.androidapp.InductionController.TemperatureReading;
import test.androidapp.InductionService.InductionBinder;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * This is the main activity of the HelloIOIO example application.
 *
 * It displays a toggle button on the screen, which enables control of the
 * on-board LED. This example shows a very simple usage of the IOIO, by using
 * the {@link IOIOActivity} class. For a more advanced use case, see the
 * HelloIOIOPower example.
 */
public class MainActivity extends Activity implements Gui {
    protected static final String LOG_TAG = "INDUCTION";

//    private static final int NOTIFICATION_ID = 1;
    private InductionService inductionService;

    InductionController inductionController;
    private ToggleButton programButton;

    int[] powerLevels = { 0, 0, 0, 0 };
    //RadioButton[] plateSelector = new RadioButton[4];
    //SeekBar slider;
    //int currentPlate = 0;
    private final SeekBar[] seekBars = new SeekBar[4];

    private TextView temperatureTextView;

    protected boolean[] programRunning = new boolean[4];

    private boolean bound;

    private CheckBox connectedBox;

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
        programButton = (ToggleButton) findViewById(R.id.startStopProgramButton);
        temperatureTextView = (TextView) findViewById(R.id.textViewTemperature);
        connectedBox = (CheckBox) findViewById(R.id.connectedBox);

        programButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO dont hardcode zone
                inductionController.startStopProgram(1,
                        programButton.isChecked());
                programRunning[1] = programButton.isChecked();
            }
        });

        seekBars[0] = getSeekBar(R.id.leftFront);
        seekBars[1] = getSeekBar(R.id.leftBack);
        seekBars[2] = getSeekBar(R.id.rightBack);
        seekBars[3] = getSeekBar(R.id.rightFront);

        SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                int zone;
                for (zone = 0; zone < seekBars.length; zone++) {
                    if (seekBar == seekBars[zone]) {
                        break;
                    }
                }
                Log.w(LOG_TAG, "progress = " + progress + " fromUser="
                        + fromUser);
                if (fromUser) {
                    powerLevels[zone] = progress;
                    inductionController.setPowerLevels(powerLevels);
                }
            }
        };

        for (SeekBar seekBar : seekBars) {
            seekBar.setOnSeekBarChangeListener(seekListener);
        }
        Log.w(LOG_TAG, "onCreat - donee");
    }

    private SeekBar getSeekBar(int viewId) {
        View view = findViewById(viewId);
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        return seekBar;
    }

/*    private void createNotification() {
        notificationBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Induction Hob")
                .setContentText("Click to show");

        Intent resultIntent = new Intent(this, this.getClass());

        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);

        notificationBuilder.setContentIntent(resultPendingIntent);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
*/
/*    private void removeNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void updateNotification() {
        notificationBuilder.setContentText("Click to show " + temperatureTextView.getText());
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
*/
    @Override
    protected void onStart() {
        Log.w(LOG_TAG, "onStart");
        super.onStart();
        Log.w(LOG_TAG, "super.onStart done");
        Intent intent = new Intent(this, InductionService.class);
        Log.w(LOG_TAG, "binding to service...");
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        //createNotification();
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
            inductionController.setGui(null);
            inductionController = null;
            inductionService = null;
            bound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            InductionBinder inductionBinder = (InductionBinder) service;
            inductionService = inductionBinder.getService();
            inductionController = inductionService.getInductionController();
            inductionController.setGui(MainActivity.this);
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
        this.powerLevels = powerLevels;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int zone = 0; zone < powerLevels.length; zone++) {
                    int level = powerLevels[zone];
                    seekBars[zone].setSecondaryProgress(level);
                }
                //programButton.setChecked(programRunning[currentPlate]);
            }
        });
    }

    @Override
    public void setCurrentTargetPowerLevels(int[] powerLevels) {
        for (int zone = 0; zone < powerLevels.length; zone++) {
            int level = powerLevels[zone];
            seekBars[zone].setProgress(level);
        }
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
                        tempString.append(Integer.toString(value.temperature) + "\u2103");
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
//                for (int i = 0; i < hot.length; i++) {
//                    plateSelector[i].setBackgroundColor(hot[i] ? Color.RED : Color.GREEN);
//                }
            }
        });
    }

    @Override
    public void setPotPresent(final boolean[] potPresent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                for (int i = 0; i < potPresent.length; i++) {
//                    plateSelector[i].setTextColor(potPresent[i] ? Color.YELLOW: Color.RED);
//                }
            }
        });
    }

    @Override
    public void setConnected(final boolean connected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectedBox.setChecked(connected);
            }
        });
    }

    @Override
    public void programDone(final int zone) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                programRunning[zone] = false;
                programButton.setChecked(false);
            }
        });
    }
}