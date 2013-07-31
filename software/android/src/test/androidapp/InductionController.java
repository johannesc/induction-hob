package test.androidapp;

import inductionlib.InductionControl;
import inductionlib.InductionControl.Role;
import inductionlib.KeyBoardCallback;
import inductionlib.PowerCardCallback;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.Induction;
import ioio.lib.api.Induction.ButtonMaskChangedEvent;
import ioio.lib.api.Induction.InductionEvent;
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
import control.ZoneController;

public class InductionController implements EventCallback, Induction.EventCallback {

    private final InductionHob inductionHob = new InductionHob();
    private final List<TemperatureController> tempControllers =
            new ArrayList<TemperatureController>();

    protected int temperature;
    private final Gui gui;

    private boolean debugLed;

    private DigitalOutput debugLedOutput;
    private Induction induction;
    private TemperatureSensor tempSensor;
	public boolean connected;

    public void setDebugLed(boolean debugLed) {
        this.debugLed = debugLed;
        update();
    }

    public interface Gui {
        public void setCurrentTargetPowerLevels(int[] powerLevels);
        public void setCurrentPowerLevels(int[] powerLevels);
        public void setTemperature(int temperature);
        public void setHot(boolean[] hot);
        public void setPotPresent(boolean[] potPresent);
        public void setConnected(boolean connected);
    }

    public InductionController(Gui gui) {
        this.gui = gui;
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
            debugLedOutput = ioio_.openDigitalOutput(0, true);
            induction = ioio_.openInduction();
            induction.registerCallback(InductionController.this);

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
            ioio_.waitForDisconnect();
            InductionController.this.connected = false;
        }

        @Override
        public void incompatible() {
            super.incompatible();
            System.out.println("incompatible!");
        }

        @Override
        public void disconnected() {
            super.disconnected();
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

    private short lastMask = 0;

    public void update() {
        gui.setCurrentPowerLevels(inductionHob.getCurrenPowerLevels());
        gui.setCurrentTargetPowerLevels(inductionHob.getTargetPowerLevels());
        gui.setTemperature(temperature);
        gui.setHot(inductionHob.getHot());
        gui.setPotPresent(inductionHob.getPotPresent());
        gui.setConnected(connected);

        try {
            debugLedOutput.write(debugLed);
            short buttonMask = inductionHob.getButtonMask();
            if (lastMask != buttonMask) {
                System.out.println("Setting mask to: " + Integer.toHexString(buttonMask));
                induction.setInductionButtonMask(buttonMask);
                lastMask = buttonMask;
            }
        } catch (ConnectionLostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

    class TemperatureController implements ZoneController {
        private int temperature;
        final int zone;

        public TemperatureController(int zone) {
            this.zone = zone;
        }

        public void reportTemperature(int temperature) {
            System.out.println("reportTemperature:" + temperature);
            this.temperature = temperature;
        }

        boolean boiled = false;
        private boolean finished;
        private int userPowerLevel;
        //private long lastLevelStartTime = 0;
        //private int lastLevel;

        // Simple algorithm to heat milk
        @Override
        public int getTargetPowerLevel() {
            if (finished) {
                return userPowerLevel;
            }
            // TODO we can't do this as we don't know when we get asked again
            // We must change the interface so that the ZoneController can request an update
            //
            //long now = System.currentTimeMillis();
            //long duration = now - lastLevelStartTime;
            // We don't want to change up & down all the time.
            // Perhaps we should filter the temperature sensor data
            // as it seems to be a bit too quick?
/*            if (duration < 3000) {
                System.out.println("Short duration, return last value!");
                return lastLevel;
            }*/
            int result;
            if (boiled) {
                // Return 1 as our final step.
                finished = true;
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
            } /*else if (temperature < 96) {
                result = 4;
            } else if (temperature < 97) {
                result = 3;
            } else if (temperature < 98) {
                result = 2;
            } */else {
                boiled = true;
                result = 1;
            }
/*            if (result != lastLevel) {
                lastLevelStartTime = now;
                lastLevel = result;
            }*/
            return result;
        }

        @Override
        public void setTargetPowerLevel(int powerLevel) {
            if (this.userPowerLevel != powerLevel) {
                this.userPowerLevel = powerLevel;
                this.finished = true;
                System.out.println("User changed power level I am finishing up!");
            }
        }

        @Override
        public boolean isFinished() {
            if (finished) {
                System.out.println("I am finished!");
            }
            return finished;
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
        for (TemperatureController oldTempController : tempControllers) {
            if (oldTempController.zone == zone) {
                tempControllers.remove(oldTempController);
                break;
            }
        }
        //TODO remove finished ones as well...
        if (start) {
            TemperatureController tempController = new TemperatureController(zone);
            tempControllers.add(tempController);
            //TODO get rid of old ones!!!
            inductionHob.setProgram(zone, tempController);
        } else {
            inductionHob.setProgram(zone, null);
        }
    }

    @Override
    public void notifyEvent(TemperatureEvent event) {
        if (event instanceof TemperatureDataEvent) {
            TemperatureDataEvent tempEvent = (TemperatureDataEvent) event;
            System.out.print("Fahrenheit=" + tempEvent.getTemperatureInFahrenheit());
            System.out.print(" Celsius=" + tempEvent.getTemperatureInCelsius());
            System.out.println(" Address=" + tempEvent.getAddress());
            for (TemperatureController tempController : tempControllers) {
                tempController.reportTemperature(tempEvent
                        .getTemperatureInCelsius());
            }
            // Update temperature in UI
            temperature = tempEvent.getTemperatureInCelsius();
            update();
        } else {
            System.err.println("Unknown event: " + event);
        }
    }

    @Override
    public void notifyEvent(InductionEvent event) {
        if (event instanceof ButtonMaskChangedEvent) {
            ButtonMaskChangedEvent butChangedEvent = (ButtonMaskChangedEvent) event;
            boolean userPressed = butChangedEvent.getUserPressed();
            short buttonMask = butChangedEvent.getButtonMask();
            System.out.println("ButtonMaskChangedEvent:"
                    + Integer.toHexString(buttonMask & 0xFFFF)
                    + " userPressed = " + userPressed);
            inductionHob.reportActualButtonMask(buttonMask, userPressed);
        } else {
            System.out.println("Got unknown event:" + event);
        }
    }
}
