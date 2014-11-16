package test.androidapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import test.androidapp.InductionController.TemperatureReading;
import android.util.Log;
import android.util.SparseArray;
import control.Zone;

public class ZoneController {
    public static final String LOG_TAG = "INDUCTION";
    private final Controller controller;
    private float[] targetTemperatures;
    protected final SparseArray<TemperatureReading> tempReadings =
            new SparseArray<TemperatureReading>();
    protected int currentTargetPowerLevel = 0;
    private final Set<Byte> sensors;
    private final Type type;

    public enum Type {
        TARGET_TEMP,
        MILK
    }

    public ZoneController(Type type, Controller controller, float[] targetTemperatures,
            Set<Byte> sensors) {
        this.type = type;
        this.controller = controller;
        this.sensors = sensors;
        setTargetTemperaturesInternal(targetTemperatures);
    }

    public Type getType() {
        return type;
    }

    public void start() {
    }

    public void stop() {
    }

    public Set<Byte> getUsedSensorAddresses() {
        return sensors;
    }

    public synchronized float[] getTargetTemperatures() {
        float[] targetTemperatures = new float[this.targetTemperatures.length];
        System.arraycopy(this.targetTemperatures, 0, targetTemperatures, 0, targetTemperatures.length);
        return targetTemperatures;
    }

    public synchronized SparseArray<TemperatureReading> getCurrentTemperatures() {
        return tempReadings.clone();
    }

    private synchronized void setTargetTemperaturesInternal(final float[] targetTemperatures) {
        this.targetTemperatures = new float[targetTemperatures.length];
        System.arraycopy(targetTemperatures, 0, this.targetTemperatures, 0, targetTemperatures.length);
        Arrays.sort(this.targetTemperatures);
        Log.w(LOG_TAG, "setTargetTemperatures " + Arrays.toString(this.targetTemperatures));
    }

    public synchronized void setTargetTemperatures(float[] targetTemperatures) {
        setTargetTemperaturesInternal(targetTemperatures);
        processTemperatures();
    }

    protected synchronized void update() {
        controller.setPowerLevel(currentTargetPowerLevel);
    }

    public synchronized void reportTemperatures(Map<Byte, TemperatureReading> temperatures) {
        Log.w(LOG_TAG, "reportTemperature:" + temperatures);
        for (Entry<Byte, TemperatureReading> temp : temperatures.entrySet()) {
            Byte address = temp.getKey();
            if (!sensors.contains(address)) {
                continue;
            }
            if (temp.getValue().valid) {
                if (tempReadings.get(address) != null) {
                    // Update temperature
                    tempReadings.put(address, temp.getValue());
                } else if (tempReadings.size() < targetTemperatures.length) {
                    // Add new temperature sensor
                    tempReadings.put(address, temp.getValue());
                } else {
                    Log.w(LOG_TAG, "More that " + targetTemperatures + " temp sensor, skipping this " + address);
                }
            } else {
                if (tempReadings.get(temp.getKey()) != null) {
                    Log.w(LOG_TAG, "TODO Invalid temperature reading, removing:" +
                            Integer.toHexString(temp.getKey() & 0xFF));
                    tempReadings.remove(address);
                    //TODO we have an invalid temperature, bail out!?
                }
            }
        }
        processTemperatures();
    }

    public synchronized void processTemperatures() {
        if (tempReadings.size() > 0) {
            int powerLevel = Zone.POWER_LEVEL_MAX;
            List<Integer> sortedTemps = new ArrayList<Integer>();
            for (int i = 0; i < tempReadings.size(); i++) {
                sortedTemps.add(tempReadings.valueAt(i).temperature);
            }
            Collections.sort(sortedTemps);

            // Find out target level, compare lowest temperature against lowest
            // target
            for (int i = 0; i < sortedTemps.size(); i++) {
                int level = getTargetPowerLevel(sortedTemps.get(i), targetTemperatures[i]);
                powerLevel = Math.min(powerLevel, level);
            }

            if (currentTargetPowerLevel != powerLevel) {
                currentTargetPowerLevel = powerLevel;
                update();
            }
        } else {
            Log.w(LOG_TAG, "No temperatures!");
            currentTargetPowerLevel = Zone.POWER_LEVEL_MIN;
            update();
        }
    }

    //Proportional regulation
    protected synchronized int getTargetPowerLevel(float temperature, float target) {
        float diff = target - temperature;
        int level = Math.max(Zone.POWER_LEVEL_MIN, Math.min((int)(3 + 0.7*diff), Zone.POWER_LEVEL_MAX));
        Log.w(LOG_TAG, "P control says " + level + " for [" + temperature + ", " + target + "]");
        return level;
    }
}
interface Controller {
    public void setPowerLevel(int level);
}

class MilkZoneController extends ZoneController {
    private static final float[] milkBooilerTargetTemps = {100.0f, 110.0f};

    public MilkZoneController(Controller controller, Set<Byte> sensors) {
        super(ZoneController.Type.MILK, controller, milkBooilerTargetTemps, sensors);
    }

    @Override
    public synchronized void processTemperatures() {
        if (tempReadings.size() > 0) {
            List<Integer> sortedTemps = new ArrayList<Integer>();
            for (int i = 0; i < tempReadings.size(); i++) {
                sortedTemps.add(tempReadings.valueAt(i).temperature);
            }
            Collections.sort(sortedTemps);
            int powerLevel;
            if (tempReadings.size() == 1) {
                // One sensor only, assume fluid sensor
                powerLevel = getTargetPowerLevelFluidSensor(sortedTemps.get(0));
            } else if (tempReadings.size() == 2) {
                // Two sensors, assume highest is the pot sensor
                int powerLevelFluidSensor = getTargetPowerLevelFluidSensor(sortedTemps.get(0));
                int powerLevelPotSensor = getTargetPowerLevelPotSensor(sortedTemps.get(1));
                powerLevel = Math.min(powerLevelFluidSensor, powerLevelPotSensor);
            } else {
                powerLevel = 1;
            }

            if (currentTargetPowerLevel != powerLevel) {
                currentTargetPowerLevel = powerLevel;
                update();
            }
        } else {
            Log.w(LOG_TAG, "No temperatures!");
            currentTargetPowerLevel = Zone.POWER_LEVEL_MIN;
            update();
        }
    }

    private int getTargetPowerLevelPotSensor(int temperature) {
        return getTargetPowerLevel(temperature, 110f);
        /*int result;
        if (temperature < 90) {
            result = 11;
        } else if (temperature < 95) {
            result = 10;
        } else if (temperature < 96) {
            result = 9;
        } else if (temperature < 97) {
            result = 8;
        } else if (temperature < 98) {
            result = 8;
        } else if (temperature < 99) {
            result = 6;
        } else if (temperature < 100) {
            result = 5;
        } else if (temperature < 105) {
            result = 4;
        } else if (temperature < 106) {
            result = 2;
        } else {
            result = 1;
        }
        Log.w(LOG_TAG, "Pot sensor says level " + result);
        return result;*/
    }

    private int getTargetPowerLevelFluidSensor(int temperature) {
        int result;
        if (temperature < 70) {
            result = 11;
        } else if (temperature < 80) {
            result = 10;
        } else if (temperature < 86) {
            result = 9;
        } else if (temperature < 93) {
            result = 8;
        } else if (temperature < 94) {
            result = 8;
        } else if (temperature < 95) {
            result = 6;
        } else if (temperature < 96) {
            result = 6;
        } else if (temperature < 97) {
            result = 5;
        } else {
            result = 1;
        }
        Log.w(LOG_TAG, "Fluid sensor says level " + result);
        return result;
    }
}