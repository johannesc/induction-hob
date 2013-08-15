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
import java.util.List;

import control.InductionHob;

public class InductionController implements EventCallback {

    private InductionHob inductionHob;
    private final List<TemperatureListener> temperatureListeners =
            new ArrayList<TemperatureListener>();

    protected int temperature;
    private Gui gui;

    private final PowerLevelController[] powerControllers = new PowerLevelController[4];

    private TemperatureSensor tempSensor;
    public boolean connected;

    public interface Gui {
        public void setCurrentTargetPowerLevels(int[] powerLevels);
        public void setCurrentPowerLevels(int[] powerLevels);
        public void setTemperature(int temperature);
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
            gui.setTemperature(temperature);
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
        public void reportTemperature(int temperature);
    }

    interface PowerLevelController {
        public void start();
        public void stop();
    }

    class MilkBoilerController implements PowerLevelController, TemperatureListener {
        private int temperature;
        final int zone;
        private boolean boiled;

        public MilkBoilerController(int zone) {
            this.zone = zone;
        }

        @Override
        public void reportTemperature(int temperature) {
            System.out.println("reportTemperature:" + temperature);
            this.temperature = temperature;
            int powerLevel = getTargetPowerLevel();
            inductionHob.setTargetPowerLevel(zone, powerLevel);
        }

        private int getTargetPowerLevel() {
            int result;
            if (boiled) {
                // Return 1 as our final step.
                result = 1;
            } else if (temperature < 80) {
                result = 11;
            } else if (temperature < 88) {
                result = 10;
            } else if (temperature < 93) {
                result = 9;
            } else if (temperature < 94) {
                result = 8;
            } else if (temperature < 95) {
                result = 6;
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
            temperatureListeners.remove(this);
        }

        @Override
        public void start() {
            temperatureListeners.add(this);
        }

        @Override
        public void stop() {
            temperatureListeners.remove(this);
        }
    }

    public IOIOLooper createLooper() {
        return new Looper();
    }

    // From gui
    public void setPowerLevels(int[] powerLevels) {
        for (int i = 0; i < powerLevels.length; i++) {
            inductionHob.setTargetPowerLevel(i, powerLevels[i]);
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

    @Override
    public void notifyEvent(TemperatureEvent event) {
        if (event instanceof TemperatureDataEvent) {
            TemperatureDataEvent tempEvent = (TemperatureDataEvent) event;
            System.out.print("Fahrenheit=" + tempEvent.getTemperatureInFahrenheit());
            System.out.print(" Celsius=" + tempEvent.getTemperatureInCelsius());
            System.out.println(" Address=" + tempEvent.getAddress());
            for (TemperatureListener temperatureListener : temperatureListeners) {
                temperatureListener.reportTemperature(tempEvent
                        .getTemperatureInCelsius());
            }
            // Update temperature in UI
            temperature = tempEvent.getTemperatureInCelsius();
            update();
        } else {
            System.err.println("Unknown event: " + event);
        }
    }
}
