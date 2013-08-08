package test.androidapp;

import ioio.lib.util.android.IOIOActivity;
import test.androidapp.InductionController.Gui;
import test.androidapp.InductionService.InductionBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
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
    RadioButton[] plateSelector = new RadioButton[4];
    SeekBar slider;
    int currentPlate = 0;

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
                inductionController.startStopProgram(currentPlate,
                        programButton.isChecked());
                programRunning[currentPlate] = programButton.isChecked();
            }
        });

        plateSelector[0] = (RadioButton) findViewById(R.id.radioButtonLeftFront);
        plateSelector[1] = (RadioButton) findViewById(R.id.radioButtonLeftBack);
        plateSelector[2] = (RadioButton) findViewById(R.id.radioButtonRightBack);
        plateSelector[3] = (RadioButton) findViewById(R.id.radioButtonRightFront);
        plateSelector[currentPlate].setChecked(true);

        OnCheckedChangeListener list = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                Log.w(LOG_TAG, "onCheckedChanged = " + isChecked);
                if (isChecked) {
                    for (int i = 0; i < plateSelector.length; i++) {
                        if (buttonView != plateSelector[i]) {
                            plateSelector[i].setChecked(false);
                        } else {
                            currentPlate = i;
                            slider.setProgress(powerLevels[currentPlate]);
                        }
                    }
                }
            }
        };
        for (int i = 0; i < plateSelector.length; i++) {
            plateSelector[i].setOnCheckedChangeListener(list);
        }

        slider = (SeekBar) findViewById(R.id.powerSlider);
        slider.setMax(11);
        slider.setProgress(powerLevels[currentPlate]);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                Log.w(LOG_TAG, "progress = " + progress + " fromUser="
                        + fromUser);
                if (fromUser) {
                    powerLevels[currentPlate] = progress;
                 inductionController.setPowerLevels(powerLevels);
                }
            }
        });
        Log.w(LOG_TAG, "onCreat - donee");
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
    public void setCurrentPowerLevels(int[] powerLevels) {
        this.powerLevels = powerLevels;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                slider.setProgress(MainActivity.this.powerLevels[currentPlate]);
                programButton.setChecked(programRunning[currentPlate]);
            }
        });
    }

    @Override
    public void setCurrentTargetPowerLevels(int[] powerLevels) {
        // TODO could we show this in a nice user friendly way?
        // Lets ignore for now.
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
                for (int i = 0; i < hot.length; i++) {
                    plateSelector[i].setBackgroundColor(hot[i] ? Color.RED : Color.GREEN);
                }
            }
        });
    }

    @Override
    public void setPotPresent(final boolean[] potPresent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < potPresent.length; i++) {
                    plateSelector[i].setTextColor(potPresent[i] ? Color.YELLOW: Color.RED);
                }
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