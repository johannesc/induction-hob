package test.androidapp;

import inductionlib.InductionControl;
import inductionlib.InductionControl.Role;
import inductionlib.KeyBoardCallback;
import inductionlib.PowerCardCallback;
import ioio.lib.api.IOIO;
import ioio.lib.api.Induction;
import ioio.lib.api.TemperatureSensor;
import ioio.lib.api.TemperatureSensor.EventCallback;
import ioio.lib.api.TemperatureSensor.TemperatureDataEvent;
import ioio.lib.api.TemperatureSensor.TemperatureEvent;
import ioio.lib.api.Uart;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import control.InductionHob;

public class InductionController implements EventCallback {

    private static final int CHECK_TEMPERATURES = 42;
    private static final long MAX_TEMPERATURE_AGE = 60l*1000;
    private static final String LOG_TAG = "INDUCTION";
    private InductionHob inductionHob;

    private final List<Gui> guis = new ArrayList<Gui>();
    private final Map<Byte, TemperatureReading> temperatures =
            new LinkedHashMap<Byte, TemperatureReading>();

    private final ZoneController[] powerControllers = new ZoneController[4];

    private TemperatureSensor tempSensor;
    public boolean connected;
    LooperThread looperThread = new LooperThread();

    public enum ProgramType {
        MILK,
        TARGET_TEMP
    };

    public interface Gui {
        public void setCurrentTargetPowerLevels(int[] powerLevels);
        public void setCurrentPowerLevels(int[] powerLevels);
        public void setTemperature(List<TemperatureReading> temps);
        public void setHot(boolean[] hot);
        public void setPotPresent(boolean[] potPresent);
        public void setConnected(boolean connected);
        public void programRunning(ZoneController[] programs);
        public void setIsPowered(boolean powered);
        public void setUnusedTemps(List<TemperatureReading> unused);
    }

    public InductionController() {
    }

    public void addGui(Gui gui) {
        this.guis.add(gui);
        update();
    }

    /**
     * This is the thread on which all the IOIO activity happens. It will be run
     * every time the application is resumed and aborted when it is paused. The
     * method setup() will be called right after a connection with the IOIO has
     * been established (which might happen several times!). Then, loop() will
     * be called repetitively until the IOIO gets disconnected.
     */
    public class Looper extends BaseIOIOLooper {
        /** The on-board LED. */

        public Looper() { super(); };

        /**
         * Called every time a connection with IOIO has been established.
         * Typically used to open pins.
         *
         * @throws ConnectionLostException
         *             When IOIO connection is lost.
         *
         * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
         */
        @Override
        protected void setup() throws ConnectionLostException {
            Induction induction = ioio_.openInduction();
            inductionHob = new InductionHob(induction);

            tempSensor = ioio_.openTemperatureSensor();
            tempSensor.registerCallback(InductionController.this);

            Uart uart = ioio_.openUart(1, IOIO.INVALID_PIN, 9600, Parity.EVEN,
                    StopBits.ONE);
            PowerCardCallback powerCardCallback = new PowerCardCallbackImpl();
            KeyBoardCallback keyboardCardCallback = new KeyBoardCallbackImpl();
            new InductionControl(uart.getInputStream(), null,
                    powerCardCallback, keyboardCardCallback, Role.PASSIVE);
            looperThread.start();
        }

        /**
         * Called repetitively while the IOIO is connected.
         *
         * @throws ConnectionLostException
         *             When IOIO connection is lost.
         *
         * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
         */
        @Override
        public void loop() throws ConnectionLostException, InterruptedException {
            InductionController.this.connected = true;
            update();
            ioio_.waitForDisconnect();
            InductionController.this.connected = false;
            update();
        }

        @Override
        public void incompatible() {
            super.incompatible();
            System.out.println("incompatible!");
        }

        @Override
        public void disconnected() {
            super.disconnected();
            inductionHob.stop();
            looperThread.end();
            inductionHob = null;
            System.out.println("Disconnected!");
        }
    }

    private class PowerCardCallbackImpl implements PowerCardCallback {
        @Override
        public void onPotPresent(boolean[] present) {
            // Pot presence only works when the power is > 0
            inductionHob.setPotPresent(present);
            update();
        }

        @Override
        public void onPoweredOnCommand(final int powerStatus,
                boolean[] powered, final boolean[] hot) {
            System.out.println("onPoweredOnCommand:" + powerStatus);
            inductionHob.setCurrentPowerStatus(powerStatus);
            inductionHob.setCurrentPowerStatus(powered);
            inductionHob.setCurrentHotStatus(hot);
            update();
        }

        @Override
        public void onPowerLimitCommand(final int[] powerLevels) {
            System.out.println("onPowerLimitCommand");
            // TODO implement! How is this command sent when the limit is
            // "removed"?
        }

        @Override
        public void onUnknownData() {
            System.out.println("onUnknownData");
        }
    }

    public void update() {
        //TODO synchronize?
        ZoneController programs[] = new ZoneController[4];
        Map<Byte, TemperatureReading> unusedTemperatures = new HashMap<Byte, TemperatureReading>();
        unusedTemperatures.putAll(temperatures);

        for (int zone = 0; zone < powerControllers.length; zone++) {
            programs[zone] = powerControllers[zone];
            if (powerControllers[zone] != null) {
                Set<Byte> sensors = powerControllers[zone].getUsedSensorAddresses();
                for (Byte sensor : sensors) {
                    unusedTemperatures.remove(sensor);
                }
            }
        }
        for (Gui gui : guis) {
            if (inductionHob != null) {
                gui.setHot(inductionHob.getHot());
                gui.setPotPresent(inductionHob.getPotPresent());
                gui.setCurrentPowerLevels(inductionHob.getCurrenPowerLevels());
                gui.setCurrentTargetPowerLevels(inductionHob.getTargetPowerLevels());
                gui.setIsPowered(inductionHob.isPowered());
            }
            System.out.println("Telling ui to update temperatures");
            List<TemperatureReading> temps = new ArrayList<TemperatureReading>();
            synchronized (this) {
                for (TemperatureReading tempReading : temperatures.values()) {
                    temps.add(tempReading);
                }
            }
            List<TemperatureReading> unused = new ArrayList<TemperatureReading>(unusedTemperatures.values());

            gui.setTemperature(temps);
            gui.setUnusedTemps(unused);
            gui.setConnected(connected);
            gui.programRunning(programs);
        }
    }

    private class KeyBoardCallbackImpl implements KeyBoardCallback {

        @Override
        public void onSetMainPowerCommand(boolean on) {
            System.out.println("onSetMainPowerCommand:" + on);
        }

        @Override
        public void onPowerOnCommand(int[] powerLevels) {
            // System.out.println("onPowerOnCommand");
            // for (int powerLevel : powerLevels) {
            // System.out.println("powerLevel:" + powerLevel);
            // }
            inductionHob.setCurrentPowerLevels(powerLevels);
            update();
        }

        @Override
        public void onUnknownData() {
            System.out.println("onUnknownData");
        }
    }

    interface TemperatureListener {
        public void reportTemperatures(Map<Byte, TemperatureReading> temperatures);
    }

    interface PowerLevelController {
        public void start();
        public void stop();
        public List<TemperatureReading> getTemperatures();
    }

    class LooperThread extends Thread {
        public Handler mHandler;
        private static final int QUIT = 1000;

        class MessageHandler extends Handler {

            @Override
            public void handleMessage(Message msg) {
                // process incoming messages here
                switch(msg.what) {
                    case CHECK_TEMPERATURES:
                        System.out.println("Checking if any temp sensors are gone!");
                        broadcastSensors();
                        break;
                    case QUIT:
                        //TODO quitSafely?
                        System.out.println("LooperThread QUIT!");
                        android.os.Looper.myLooper().quit();
                        break;
                    default:
                        break;
                }
            }
        }

        @Override
        public void run() {
            android.os.Looper.prepare();

            mHandler = new MessageHandler();

            android.os.Looper.loop();
        }

        public void end() {
            mHandler.sendEmptyMessage(QUIT);
        }
    }

    public IOIOLooper createLooper() {
        return new Looper();
    }

    // From gui
    public void setPowerLevel(int zone, int powerLevel) {
        if (inductionHob != null) {
            inductionHob.setTargetPowerLevel(zone, powerLevel);
        }
        update();
    }

    // From gui
    public void stopProgram(final int zone) {
        if (powerControllers[zone] != null) {
            powerControllers[zone].stop();
            powerControllers[zone] = null;
            update();
        }
    }

    private Controller getController(final int zone) {
        Controller controller = new Controller() {
            @Override
            public void setPowerLevel(int level) {
                inductionHob.setTargetPowerLevel(zone, level);
            }
        };
        return controller;
    }

    public void startMilkProgram(final int zone, Set<Byte> sensors) {
        //TODO synchronized?
        stopProgram(zone);
        powerControllers[zone] = new MilkZoneController(getController(zone), sensors);
        powerControllers[zone].start();
        broadcastSensors();
        update();
    }

    public void startProgram(ZoneController.Type type, final int zone, int targetTemp, Byte sensor) {
        //TODO synchronized?
        stopProgram(zone);
        float[] targetTemps = {targetTemp};
        Set<Byte> sensors = new HashSet<Byte>();
        sensors.add(sensor);
        powerControllers[zone] = new ZoneController(type, getController(zone), targetTemps, sensors);
        powerControllers[zone].start();
        broadcastSensors();
        update();
    }

    public void setTargetTemperatures(final int zone, int targetTemp) {
        if (powerControllers[zone] != null) {
            float[] targetTemps = {(float)targetTemp};
            powerControllers[zone].setTargetTemperatures(targetTemps);
        }
    }

    public static final class TemperatureReading {
        public final boolean valid;
        public final int temperature;
        public final byte address;
        public final long reportedTime;

        public TemperatureReading(TemperatureDataEvent tempEvent) {
            temperature = tempEvent.getTemperatureInCelsius();
            valid = tempEvent.isValid();
            address = tempEvent.getAddress();
            reportedTime = SystemClock.elapsedRealtime();
        }
        @Override
        public String toString() {
            StringBuilder res = new StringBuilder();
            if (!valid) {
                res.append("invalid");
            } else {
                res.append(temperature).append("\u2103");
                res.append(" (").append(address).append("@").append(reportedTime).append(")");
            }
            return res.toString();
        }
    }

    public void broadcastSensors() {
        //Remove a temperature sensor after MAX_TEMPERATURE_AGE -0.5s TODO a bit ugly
        long maxAge = SystemClock.elapsedRealtime() - (MAX_TEMPERATURE_AGE - 500);
        Log.w(LOG_TAG, "Clearing old temperature sensors");

        synchronized(this) {
            Set<Entry<Byte, TemperatureReading>> entrySet = temperatures.entrySet();
            Set<Byte> oldTemp = new HashSet<Byte>();
            for (Entry<Byte, TemperatureReading> entry : entrySet) {
                TemperatureReading value = entry.getValue();
                if (value.reportedTime < maxAge) {
                    oldTemp.add(entry.getKey());
                }
            }
            for (Byte old : oldTemp) {
                Log.w(LOG_TAG, "Removing " + old);
                // Tell our listeners that the temperature is now considered invalid
                temperatures.remove(old);
            }

            for (ZoneController zoneController : powerControllers) {
                if (zoneController != null) {
                    zoneController.reportTemperatures(temperatures);
                }
            }
        }
    }

    @Override
    public void notifyEvent(TemperatureEvent event) {
        // TODO we should have some mechanism to report when a temperature
        // is invalid since we have missed a reading. This should probably
        // be handled in lower layers based on timeout.
        if (event instanceof TemperatureDataEvent) {
            TemperatureDataEvent tempEvent = (TemperatureDataEvent) event;
            System.out.print("Fahrenheit=" + tempEvent.getTemperatureInFahrenheit());
            System.out.print(" Celsius=" + tempEvent.getTemperatureInCelsius());
            System.out.print(" valid=" + tempEvent.isValid());
            System.out.println(" Address=" + tempEvent.getAddress());

            synchronized(this) {
                // Update temperature in UI
                temperatures.put(tempEvent.getAddress(), new TemperatureReading(tempEvent));

                looperThread.mHandler.removeMessages(CHECK_TEMPERATURES);
                looperThread.mHandler.sendEmptyMessageDelayed(CHECK_TEMPERATURES,
                        MAX_TEMPERATURE_AGE);
                broadcastSensors();
            }
            update();
        } else {
            System.err.println("Unknown event: " + event);
        }
    }

    public void removeAllGuis() {
        guis.clear();
    }

    public void removeGui(Gui gui) {
        guis.remove(gui);
    }
}
