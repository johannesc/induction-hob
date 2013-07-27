package test.androidapp;

import java.util.ArrayList;
import java.util.List;

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
import ioio.lib.api.TemperatureSensor.TemperatureDataEvent;
import ioio.lib.api.TemperatureSensor.TemperatureEvent;
import ioio.lib.api.Uart;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOConnectionManager.Thread;
import ioio.lib.util.IOIOLooper;
import control.InductionHob;
import control.ZoneController;

public class InductionController {

    private final InductionHob inductionHob = new InductionHob();
    private final List<TemperatureController> tempControllers =
            new ArrayList<TemperatureController>();
    //    int[] powerLevels;

    protected int temperature;
    private final Gui gui;

    private boolean debugLed;

    public void setDebugLed(boolean debugLed) {
        this.debugLed = debugLed;
    }

    public interface Gui {
        void setCurrentTargetPowerLevels(int[] powerLevels);
        void setCurrentPowerLevels(int[] powerLevels);
        void setTemperature(int temperature);
        void setHot(boolean[] hot);
        void setPotPresent(boolean[] potPresent);
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
        private DigitalOutput led_;
        private Induction induction;
        private short lastMask = 0;
        private TemperatureSensor tempSensor;
        // TODO fix...
        private int lastLevel;

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
            led_ = ioio_.openDigitalOutput(0, true);
            induction = ioio_.openInduction();
            tempSensor = ioio_.openTemperatureSensor();
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
            led_.write(debugLed);
            readInductionEvents();
/*            if (tempController != null) {
                int level = tempController.getTargetPowerLevel();
                if (level != lastLevel) {
                    System.out.println("TempController: new temperature!");
                    lastLevel = level;
                    inductionHob.setTargetPowerLevel(1, level);
                    updateGui();
                }
            }
*/            short buttonMask = inductionHob.getButtonMask();
            if (lastMask != buttonMask) {
                System.out.println("Setting mask to: "
                        + Integer.toHexString(buttonMask));
                induction.setInductionButtonMask(buttonMask);
                lastMask = buttonMask;
            }
            // TODO replace with a wait/notify thing
            Thread.sleep(10);
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

        private void readInductionEvents() {
            try {
                while (induction.getEventCount() > 0) {
                    InductionEvent event = induction.readEvent();
                    if (event instanceof ButtonMaskChangedEvent) {
                        ButtonMaskChangedEvent butChangedEvent = (ButtonMaskChangedEvent) event;
                        boolean userPressed = butChangedEvent.getUserPressed();
                        short buttonMask = butChangedEvent.getButtonMask();
                        System.out.println("ButtonMaskChangedEvent:"
                                + Integer.toHexString(buttonMask & 0xFFFF)
                                + " userPressed = " + userPressed);
                        if (userPressed) {
                            // This means that the firmware has changed the mask
                            // to 0
                            lastMask = 0;
                            System.out
                                    .println("Firmware has cleared our mask, lets do the same");
                        }
                        inductionHob.reportActualButtonMask(buttonMask,
                                userPressed);
                    } else {
                        System.out.println("Got unknown event:" + event);
                    }
                }

                while (tempSensor.getEventCount() > 0) {
                    TemperatureEvent event = tempSensor.readEvent();
                    if (event instanceof TemperatureDataEvent) {
                        TemperatureDataEvent tempEvent = (TemperatureDataEvent) event;
                        System.out.print("Fahrenheit="
                                + tempEvent.getTemperatureInFahrenheit());
                        System.out.print(" Celsius="
                                + tempEvent.getTemperatureInCelsius());
                        System.out
                                .println(" Address=" + tempEvent.getAddress());
                        for (TemperatureController tempController : tempControllers) {
                            tempController.reportTemperature(tempEvent
                                    .getTemperatureInCelsius());
                        }
                        temperature = tempEvent.getTemperatureInCelsius();
                    } else {
                        System.err.println("Unknown event: " + event);
                    }
                }
            } catch (ConnectionLostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class PowerCardCallbackImpl implements PowerCardCallback {
        @Override
        public void onPotPresent(boolean[] present) {
            // Pot presence only works when the power is > 0
            inductionHob.setPotPresent(present);
            updateGui();
        }

        @Override
        public void onPoweredOnCommand(final int powerStatus,
                boolean[] powered, final boolean[] hot) {
            System.out.println("onPoweredOnCommand:" + powerStatus);
            inductionHob.setCurrentPowerStatus(powerStatus);
            inductionHob.setCurrentPowerStatus(powered);
            inductionHob.setCurrentHotStatus(hot);
            updateGui();
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

    public void updateGui() {
        gui.setCurrentPowerLevels(inductionHob.getCurrenPowerLevels());
        gui.setCurrentTargetPowerLevels(inductionHob.getTargetPowerLevels());
        gui.setTemperature(temperature);
        gui.setHot(inductionHob.getHot());
        gui.setPotPresent(inductionHob.getPotPresent());
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
            updateGui();
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

        // Simple algorithm to heat milk
        @Override
        public int getTargetPowerLevel() {
            if (finished) {
                return userPowerLevel;
            }
            if (boiled) {
                // Return 1 as our final step.
                finished = true;
                return 1;
            }
            if (temperature < 80) {
                return 11;
            } else if (temperature < 88) {
                return 10;
            } else if (temperature < 93) {
                return 9;
            } else if (temperature < 94) {
                return 8;
            } else if (temperature < 95) {
                return 3;
            } else {
                boiled = true;
                return 1;
            }
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
    }

    // From gui
    public void startStopProgram(int zone, boolean start) {
        // TODO support tempController on any zone...
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
}
