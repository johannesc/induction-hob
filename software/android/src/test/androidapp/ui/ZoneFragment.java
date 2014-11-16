package test.androidapp.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import test.androidapp.InductionController;
import test.androidapp.InductionController.Gui;
import test.androidapp.InductionController.TemperatureReading;
import test.androidapp.MainActivity;
import test.androidapp.R;
import test.androidapp.ZoneController;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ZoneFragment extends DialogFragment implements Gui {

    private static final String ARG_ZONE = "zone";
    private static final String ARG_LEVEL = "level";
    private static final String ARG_PROGRAM = "program";
    private static final String LOG_TAG = MainActivity.LOG_TAG + ".ZoneFragment";

    private ToggleButton programButton;
    private SeekBar targetTempSeekBar;
    private SeekBar targetLevelSeekBar;
    private Spinner programType;
    private TextView targetTempText;

    private int zone;

    private ZoneFramgentListener listener;
    private InductionController inductionController;
    private List<TemperatureReading> unusedTemperatures = new ArrayList<TemperatureReading>();

    public interface ZoneFramgentListener {
        public InductionController getInductionController();
    }

    private static final int zoneToIcon[] = {R.drawable.inductionhob_left_front_selected,
        R.drawable.inductionhob_left_back_selected,
        R.drawable.inductionhob_right_back_selected,
        R.drawable.inductionhob_right_front_selected,
        };
    protected static final int PROGRAM_INDEX_MILK = 0;

    public static ZoneFragment newInstance(int zone, int level, int program) {
        ZoneFragment zoneDialog = new ZoneFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ZONE, zone);
        args.putInt(ARG_LEVEL, level);
        args.putInt(ARG_PROGRAM, program);
        zoneDialog.setArguments(args);
        return zoneDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        zone = getArguments().getInt(ARG_ZONE);
        //setStyle(DialogFragment.STYLE_NO_TITLE, getTheme());
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final android.view.ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.zone_dialog, container, false);

        targetTempText = (TextView)view.findViewById(R.id.targetTempText);
        targetTempText.setVisibility(View.INVISIBLE);
        targetTempSeekBar = (SeekBar)view.findViewById(R.id.targetTempSeek);
        targetTempSeekBar.setVisibility(View.INVISIBLE);
        programButton = (ToggleButton) view.findViewById(R.id.startStopProgramButton);
        programButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!programButton.isChecked()) {
                    inductionController.stopProgram(zone);
                    return;
                }

                final int position = programType.getSelectedItemPosition();
                if (position == PROGRAM_INDEX_MILK) {
                    if (unusedTemperatures.size() <= 2) {
                        Set<Byte> addresses = new HashSet<Byte>();
                        for (TemperatureReading reading : unusedTemperatures) {
                            addresses.add(reading.address);
                        }
                        inductionController.startMilkProgram(zone, addresses);
                    } else {
                        Log.w(LOG_TAG, "Too many temperature sensors available TODO user select");
                        // SEE AlertDialog.Builder.setMultiChoiceItems
                    }
                } else {
                    if (unusedTemperatures.size() == 1) {
                        inductionController.startProgram(ZoneController.Type.TARGET_TEMP, zone,
                                targetTempSeekBar.getProgress(), unusedTemperatures.get(0).address);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(inflater.getContext());
                        builder.setTitle("Select temperature sensor");
                        CharSequence tempSensor[] = new CharSequence[unusedTemperatures.size()];
                        int i = 0;
                        for (TemperatureReading reading : unusedTemperatures) {
                            tempSensor[i++] = "adr=" + Integer.toHexString(reading.address & 0xFF) +
                                    " temp=" + reading.temperature + "\u2103";
                        }
                        builder.setItems(tempSensor, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                inductionController.startProgram(ZoneController.Type.TARGET_TEMP,
                                        zone, targetTempSeekBar.getProgress(),
                                        unusedTemperatures.get(which).address);
                            }
                        });
                        builder.show();
                    }
                }
            }
        });
        final TextView targetTempText = (TextView)view.findViewById(R.id.targetTempText);

        View seekBarView = view.findViewById(R.id.targetLevel);
        targetLevelSeekBar = (SeekBar) seekBarView.findViewById(R.id.seekBar);
        targetLevelSeekBar.setProgress(getArguments().getInt(ARG_LEVEL));

        programType = (Spinner) view.findViewById(R.id.programSelector);
        programType.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                if (id == PROGRAM_INDEX_MILK) {
                    targetTempSeekBar.setVisibility(View.INVISIBLE);
                    targetTempText.setVisibility(View.INVISIBLE);
                } else {
                    targetTempSeekBar.setVisibility(View.VISIBLE);
                    targetTempText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        getDialog().setTitle(R.string.zoneDialogTitle);
        getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);

        targetLevelSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    inductionController.setPowerLevel(zone, progress);
                }
            }
        });

        targetTempSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                targetTempText.setText(progress + "\u2103");
                if (fromUser) {
                    Log.w(LOG_TAG, "progress=" + progress);
                    inductionController.setTargetTemperatures(zone, progress);
                }
            }
        });
        // This should be last since it will access controls fetched above
        inductionController.addGui(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, zoneToIcon[zone]);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (ZoneFramgentListener) activity;
            inductionController = listener.getInductionController();
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ZoneFramgentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.w(LOG_TAG, "onDetach()");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.w(LOG_TAG, "onDestroyView()");
        inductionController = listener.getInductionController();
        inductionController.removeGui(this);
    }

    @Override
    public void setCurrentTargetPowerLevels(final int[] powerLevels) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                targetLevelSeekBar.setProgress(powerLevels[zone]);
            }
        });
    }

    @Override
    public void setCurrentPowerLevels(int[] powerLevels) {
    }

    @Override
    public void setTemperature(List<TemperatureReading> temps) {
        Log.w(LOG_TAG, "Got temperatures");
    }

    @Override
    public void setUnusedTemps(List<TemperatureReading> unusedTemperatures) {
        synchronized (this) {
            this.unusedTemperatures = unusedTemperatures;
        }
    }

    @Override
    public void setHot(boolean[] hot) {
        //TODO change icon between blue and red?
    }

    @Override
    public void setPotPresent(boolean[] potPresent) {
        //TODO change icon to something else? Also disable stuff?
    }

    @Override
    public void setConnected(boolean connected) {
        //TODO disable controls if not powered
    }

    @Override
    public void setIsPowered(boolean powered) {
        //TODO disable controls if not powered
    }

    private void runOnUiThread(Runnable runnable) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(runnable);
        } else {
            Log.w(LOG_TAG, "null activity!");
        }
    }

    @Override
    public void programRunning(final ZoneController[] running) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ZoneController controller = running[zone];
                if (controller != null) {
                    Log.i(LOG_TAG, "Controller is running");
                    targetLevelSeekBar.setEnabled(false);
                    programButton.setChecked(true);
                    if (controller.getType() == ZoneController.Type.MILK) {
                        programType.setSelection(PROGRAM_INDEX_MILK);
                    } else {
                        programType.setSelection(PROGRAM_INDEX_MILK + 1);
                    }
                    Set<Byte> sensors = controller.getUsedSensorAddresses();
                    float[] temps = controller.getTargetTemperatures();
                    if (temps.length == 1) {
                        targetTempSeekBar.setProgress((int)temps[0]);
                    }
                } else {
                    targetLevelSeekBar.setEnabled(true);
                }
            }
        });
    }
}
