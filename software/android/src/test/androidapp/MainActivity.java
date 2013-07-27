package test.androidapp;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import test.androidapp.InductionController.Gui;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
public class MainActivity extends IOIOActivity implements Gui {
    protected static final String LOG_TAG = "INDUCTION";

    private ToggleButton button_;
    InductionController inductionController = new InductionController(this);
    private ToggleButton programButton;

    int[] powerLevels = { 0, 0, 0, 0 };
    RadioButton[] plateSelector = new RadioButton[4];
    SeekBar slider;
    int currentPlate = 0;

    private TextView temperatureTextView;

    protected boolean[] programRunning = new boolean[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button_ = (ToggleButton) findViewById(R.id.button);
        programButton = (ToggleButton) findViewById(R.id.startStopProgramButton);
        temperatureTextView = (TextView) findViewById(R.id.textViewTemperature);

        button_.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                inductionController.setDebugLed(button_.isChecked());
            }
        });

        programButton.setOnClickListener(new View.OnClickListener() {
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

        button_ = (ToggleButton) findViewById(R.id.button);
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
    }

    /*
     * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
     * menu; this adds items to the action bar if it is present.
     * getMenuInflater().inflate(R.menu.activity_main, menu); return true; }
     */

    /**
     * A method to create our IOIO thread.
     * 
     * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
     */
    @Override
    protected IOIOLooper createIOIOLooper() {
        return inductionController.createLooper();
    }

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
}