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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import control.InductionHob;

public class InductionController implements EventCallback {

    private static final int CHECK_TEMPERATURES = 42;
    private static final long MAX_TEMPERATURE_AGE = 60l*1000;
    private InductionHob inductionHob;
    private final List<TemperatureListener> temperatureListeners =
            new ArrayList<TemperatureListener>();

    private Gui gui;
    private final Map<Byte, TemperatureReading> temperatures =
            new LinkedHashMap<Byte, TemperatureReading>();

    private final PowerLevelController[] powerControllers = new PowerLevelController[4];

    private TemperatureSensor tempSensor;
    public boolean connected;
    LooperThread looperThread = new LooperThread();

    public interface Gui {
        public void setCurrentTargetPowerLevels(int[] powerLevels);
        public void setCurrentPowerLevels(int[] powerLevels);
        public void setTemperature(List<TemperatureReading> temps);
        public void setHot(boolean[] hot);
        public void setPotPresent(boolean[] potPresent);
        public void setConnected(boolean connected);
        public void programDone(int zone);
    }

    public InductionController() {
    }

    public void setGui(Gui gui) {
        this.gui = gui;
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
        if (gui != null) {
            if (inductionHob != null) {
                gui.setHot(inductionHob.getHot());
                gui.setPotPresent(inductionHob.getPotPresent());
                gui.setCurrentPowerLevels(inductionHob.getCurrenPowerLevels());
                gui.setCurrentTargetPowerLevels(inductionHob.getTargetPowerLevels());
            }
            //TODO synchronize/make a copy!
            System.out.println("Telling ui to update temperatures");
            List<TemperatureReading> temps = new ArrayList<TemperatureReading>();
            synchronized (this) {
                for (TemperatureReading tempReading : temperatures.values()) {
                    temps.add(tempReading);
                }
            }

            gui.setTemperature(temps);
            gui.setConnected(connected);
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
        public void reportTemperature(byte address, int temperature, boolean valid);
    }

    interface PowerLevelController {
        public void start();
        public void stop();
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
                        clearOldSensors();
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

    class MilkBoilerController implements PowerLevelController, TemperatureListener {
        private int temperature;
        final int zone;
        private boolean boiled;
        private boolean temperatureValid;
        private boolean temperatureReceived = false;
        private byte temperatureAddress;

        public MilkBoilerController(int zone) {
            this.zone = zone;
        }

        @Override
        public void reportTemperature(byte address, int temperature, boolean valid) {
            System.out.println("reportTemperature:" + temperature);
            if (!temperatureReceived && valid) {
                temperatureAddress = address;
                temperatureReceived = true;
            }
            if (temperatureReceived) {
                if (address == this.temperatureAddress) {
                    this.temperature = temperature;
                    this.temperatureValid = valid;
                    int powerLevel = getTargetPowerLevel();
                    inductionHob.setTargetPowerLevel(zone, powerLevel);
                } else {
                    System.out.println("More that one temp sensor, skipping this " + address);
                }
            }
        }

        private int getTargetPowerLevel() {
            int result;
            if (boiled || !temperatureValid) {
                // Return 1 as our final step.
                result = 1;
            } else if (temperature < 78) {
                result = 11;
            } else if (temperature < 86) {
                result = 10;
            } else if (temperature < 93) {
                result = 9;
            } else if (temperature < 94) {
                result = 8;
            } else if (temperature < 95) {
                result = 6;
            } else if (temperature < 96) {
                result = 5;
            } else if (temperature < 97) {
                result = 4;
            } else {
                boiled = true;
                setFinished();
                result = 1;
            }
            return result;
        }

        private void setFinished() {
            if (gui != null) {
                gui.programDone(zone);
            }
            unRegisterTemperatureListener(this);
        }

        @Override
        public void start() {
            registerTemperatureListener(this);
        }

        @Override
        public void stop() {
            unRegisterTemperatureListener(this);
        }
    }

    public void registerTemperatureListener(TemperatureListener listener) {
        temperatureListeners.add(listener);
        Set<Entry<Byte, TemperatureReading>> entrySet = temperatures.entrySet();
        for (Entry<Byte, TemperatureReading> entry : entrySet) {
            listener.reportTemperature(entry.getValue().address,
                    entry.getValue().temperature, entry.getValue().valid);
        }
    }

    public void unRegisterTemperatureListener(TemperatureListener listener) {
        temperatureListeners.remove(listener);
    }

    public IOIOLooper createLooper() {
        return new Looper();
    }

    // From gui
    public void setPowerLevels(int[] powerLevels) {
        if (inductionHob != null) {
            for (int i = 0; i < powerLevels.length; i++) {
                inductionHob.setTargetPowerLevel(i, powerLevels[i]);
            }
        }
        update();
    }

    // From gui
    public void startStopProgram(int zone, boolean start) {
        if (powerControllers[zone] != null) {
            powerControllers[zone].stop();
        }
        if (start) {
            powerControllers[zone] = new MilkBoilerController(zone);
            powerControllers[zone].start();
        }
    }

    public static class TemperatureReading {
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
    }

    public void clearOldSensors() {
        //Remove a temperature sensor after MAX_TEMPERATURE_AGE -0.5s TODO a bit ugly
        long maxAge = SystemClock.elapsedRealtime() - (MAX_TEMPERATURE_AGE - 500);
        System.out.println("Clearing old temperature sensors");

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
                System.out.println("Removing " + old);
                // Tell our listeners that the temperature is now considered invalid
                for (TemperatureListener temperatureListener : temperatureListeners) {
                    temperatureListener.reportTemperature(old, 1000, false);
                }
                temperatures.remove(old);
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

                clearOldSensors();
                looperThread.mHandler.removeMessages(CHECK_TEMPERATURES);
                looperThread.mHandler.sendEmptyMessageDelayed(CHECK_TEMPERATURES,
                        MAX_TEMPERATURE_AGE);

                for (TemperatureListener temperatureListener : temperatureListeners) {
                    temperatureListener.reportTemperature(tempEvent.getAddress(),
                            tempEvent.getTemperatureInCelsius(), tempEvent.isValid());
                }
            }
            update();
        } else {
            System.err.println("Unknown event: " + event);
        }
    }
}
