package pctestapp;

import inductionlib.InductionControl;
import inductionlib.InductionControl.Role;
import inductionlib.KeyBoardCallback;
import inductionlib.PowerCardCallback;
import ioio.lib.api.IOIO;
import ioio.lib.api.Induction;
import ioio.lib.api.Induction.ButtonMaskChangedEvent;
import ioio.lib.api.Induction.InductionEvent;
import ioio.lib.api.TemperatureSensor;
import ioio.lib.api.TemperatureSensor.TemperatureEvent;
import ioio.lib.api.TemperatureSensor.TemperatureDataEvent;
import ioio.lib.api.Uart;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOConnectionManager.Thread;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.pc.IOIOConsoleApp;

import javax.swing.SwingUtilities;

import control.InductionHob;

public class PCTestApp extends IOIOConsoleApp implements GUI.Callback {
    private final InductionHob inductionHob = new InductionHob();

    private GUI gui;

    public static void main(String[] args) throws Exception {
        //System.setProperty("ioio.SerialPorts", "/dev/rfcomm0");
        System.setProperty("ioio.SerialPorts", "/dev/ttyACM0");
        new PCTestApp().go(args);
    }

    @Override
    protected void run(String[] args) throws Exception {
        gui = new GUI();
        gui.showGUI(this);
        //TODO do something smarter to avoid disconnecting the IOIO
        Thread.sleep(7200000);
    }

    //From GUI!
    @Override
    public void onPowerLevelChanged(int zone, int powerLevel) {
        inductionHob.setTargetPowerLevel(zone, powerLevel);
    }

    @Override
    public IOIOLooper createIOIOLooper(String connectionType, Object extra) {
        return new IOIOLooper() {
            private Induction induction;
            private short lastMask = 0;
            private TemperatureSensor tempSensor;

            @Override
            public void setup(IOIO ioio) throws ConnectionLostException,
                    InterruptedException {
                induction = ioio.openInduction();
                tempSensor = ioio.openTemperatureSensor();
                Uart uart = ioio.openUart(1, IOIO.INVALID_PIN, 9600, Parity.EVEN, StopBits.ONE);
                PowerCardCallback powerCardCallback = new PowerCardCallbackImpl();
                KeyBoardCallback keyboardCardCallback = new KeyBoardCallbackImpl();
                new InductionControl(uart.getInputStream(),
                        null, powerCardCallback, keyboardCardCallback, Role.PASSIVE);
            }

            @Override
            public void loop() throws ConnectionLostException, InterruptedException {
                readInductionEvents();
                short buttonMask = inductionHob.getButtonMask();
                if (lastMask != buttonMask ) {
                    System.out.println("Setting mask to: " + Integer.toHexString(buttonMask));
                    induction.setInductionButtonMask(buttonMask);
                    lastMask = buttonMask;
                }
                Thread.sleep(10);
            }

            @Override
            public void incompatible() {
                System.out.println("incompatible!");
            }

            @Override
            public void disconnected() {
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
                            System.out.println("ButtonMaskChangedEvent:" +
                                Integer.toHexString(buttonMask & 0xFFFF) +
                                " userPressed = " + userPressed);
                            if (userPressed) {
                                // This means that the firmware has changed the mask to 0
                                lastMask = 0;
                                System.out.println("Firmware has cleared our mask, lets do the same");
                            }
                            inductionHob.reportActualButtonMask(buttonMask, userPressed);
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
                            System.out.println(" Address="
                                    + tempEvent.getAddress());
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
        };
    }

    private class PowerCardCallbackImpl implements PowerCardCallback {
        @Override
        public void onPotPresent(boolean[] present) {
            //Pot presence only works when the power is > 0
            inductionHob.setPotPresent(present);
            updateGui();
        }

        @Override
        public void onPoweredOnCommand(final int powerStatus, boolean[] powered,
                final boolean[] hot) {
            System.out.println("onPoweredOnCommand:" + powerStatus);
            inductionHob.setCurrentPowerStatus(powerStatus);
            inductionHob.setCurrentPowerStatus(powered);
            inductionHob.setCurrentHotStatus(hot);
            updateGui();
        }

        @Override
        public void onPowerLimitCommand(final int[] powerLevels) {
            System.out.println("onPowerLimitCommand");
            //TODO implement! How is this command sent when the limit is "removed"?
        }

        @Override
        public void onUnknownData() {
            System.out.println("onUnknownData");
        }
    }

    public void updateGui() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("Updating UI");
                gui.setPotHot(inductionHob.getHot());

                if (inductionHob.isPowered()) {
                    gui.setPowered(true);
                    gui.enablePowerControl(true);
                } else {
                    gui.setPowered(false);
                    gui.enablePowerControl(false);
                }
                gui.setActualPowerLevels(inductionHob.getCurrenPowerLevels());
                gui.setTargetPower(inductionHob.getTargetPowerLevels());
                gui.setPotPresent(inductionHob.getPotPresent());
            }
        });
    }

    private class KeyBoardCallbackImpl implements KeyBoardCallback {
        @Override
        public void onSetMainPowerCommand(boolean on) {
            System.out.println("onSetMainPowerCommand:" + on);
        }

        @Override
        public void onPowerOnCommand(int[] powerLevels) {
            //System.out.println("onPowerOnCommand");
            //for (int powerLevel : powerLevels) {
            //    System.out.println("powerLevel:" + powerLevel);
            //}
            inductionHob.setCurrentPowerLevels(powerLevels);
            updateGui();
        }

        @Override
        public void onUnknownData() {
            System.out.println("onUnknownData");
        }
    }
}
