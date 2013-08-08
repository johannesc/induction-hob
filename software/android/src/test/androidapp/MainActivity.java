package test.androidapp;

import ioio.lib.util.android.IOIOActivity;
import test.androidapp.InductionController.Gui;
import test.androidapp.InductionService.InductionBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.w(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, InductionService.class);
        startService(intent);
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

    @Override
    protected void onStart() {
        Log.w(LOG_TAG, "onStart");
        super.onStart();
        Log.w(LOG_TAG, "super.onStart done");
        Intent intent = new Intent(this, InductionService.class);
        Log.w(LOG_TAG, "binding to sercice...");
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.w(LOG_TAG, "onStart - done");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            bound = false;
            unbindService(serviceConnection);
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        private InductionService inductionService;

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
    public void setTemperature(final int temperature) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                temperatureTextView.setText(Integer.toString(temperature) + "\u2103");
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
}