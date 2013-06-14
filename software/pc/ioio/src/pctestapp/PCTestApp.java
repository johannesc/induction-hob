package pctestapp;

import inductionlib.InductionControl;
import inductionlib.InductionControl.Role;
import inductionlib.KeyBoardCallback;
import inductionlib.PowerCardCallback;
import ioio.lib.api.IOIO;
import ioio.lib.api.Induction;
import ioio.lib.api.Uart;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOConnectionManager.Thread;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.pc.IOIOConsoleApp;

import javax.swing.SwingUtilities;

public class PCTestApp extends IOIOConsoleApp implements GUI.Callback {
    private static final short[] ZONE_TO_PLUS_MASK = new short[4];
    private static final short[] ZONE_TO_MINUS_MASK = new short[4];
    private final InductionState inductionState = new InductionState();
    private final TargetState targetState = new TargetState();

    private GUI gui;
    private short buttonMask = 0x0000;
    private boolean firstPowerStateReceived = false;

    static {
        ZONE_TO_PLUS_MASK[InductionControl.ZONE_LEFT_FRONT] = 0x2000;
        ZONE_TO_PLUS_MASK[InductionControl.ZONE_LEFT_BACK] = 0x0800;
        ZONE_TO_PLUS_MASK[InductionControl.ZONE_RIGHT_BACK] = 0x1000;
        ZONE_TO_PLUS_MASK[InductionControl.ZONE_RIGHT_FRONT] = 0x0040;

        ZONE_TO_MINUS_MASK[InductionControl.ZONE_LEFT_FRONT] = (short)0x8000;
        ZONE_TO_MINUS_MASK[InductionControl.ZONE_LEFT_BACK] = 0x0008;
        ZONE_TO_MINUS_MASK[InductionControl.ZONE_RIGHT_BACK] = 0x0010;
        ZONE_TO_MINUS_MASK[InductionControl.ZONE_RIGHT_FRONT] = 0x0400;
    }

    private class InductionState {
        int[] powerLevels = new int[4];
        boolean powered;
    }

    private class TargetState {
        int[] powerLevels = new int[4];
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("ioio.SerialPorts", "/dev/rfcomm0");
        //System.setProperty("ioio.SerialPorts", "/dev/ttyACM0");
        new PCTestApp().go(args);
    }

    @Override
    protected void run(String[] args) throws Exception {
        gui = new GUI();
        gui.showGUI(this);
        //TODO do something smarter to avoid disconnecting the IOIO
        Thread.sleep(7200000);
    }

    private void updateKeyBoard() {
        short localButtonMask = 0;
        for(int i=0;i<4;i++) {
            int target = targetState.powerLevels[i];
            int current = inductionState.powerLevels[i];
            if (target > current) {
                System.out.println("Need plus for zone " + i);
                localButtonMask |= ZONE_TO_PLUS_MASK[i];
            } else if (target < current) {
                System.out.println("Need minus for zone " + i);
                localButtonMask |= ZONE_TO_MINUS_MASK[i];
            } else {
                System.out.println("Target reached for zone " + i);
            }
        }
        if (!inductionState.powered) {
            System.out.println("Not powered on, use zero mask");
            localButtonMask = 0;
        }
        if (localButtonMask != buttonMask) {
            System.out.println("Updating button mask to " + Integer.toHexString(localButtonMask));
            buttonMask = localButtonMask;
        } else {
            System.out.println("No change in button mask");
        }
    }

    //From GUI!
    @Override
    public void onPowerLevelChanged(int zone, int powerLevel) {
        System.out.println("Changing target powerlevels in zone " + zone + " to " + powerLevel);
        targetState.powerLevels[zone] = powerLevel;
        updateKeyBoard();
    }

    @Override
    public IOIOLooper createIOIOLooper(String connectionType, Object extra) {
        return new IOIOLooper() {
            private Induction induction;
            private short lastMask = 0;

            @Override
            public void setup(IOIO ioio) throws ConnectionLostException,
                    InterruptedException {
                induction = ioio.openInduction();
                Uart uart = ioio.openUart(1, IOIO.INVALID_PIN, 9600, Parity.EVEN, StopBits.ONE);
                PowerCardCallback powerCardCallback = new PowerCardCallbackImpl();
                KeyBoardCallback keyboardCardCallback = new KeyBoardCallbackImpl();
                new InductionControl(uart.getInputStream(),
                        null, powerCardCallback, keyboardCardCallback, Role.PASSIVE);
            }

            @Override
            public void loop() throws ConnectionLostException, InterruptedException {
                if (lastMask != buttonMask ) {
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
        };
    }

    private class PowerCardCallbackImpl implements PowerCardCallback {
        @Override
        public void onPotPresent(boolean[] present) {
            //Pot presence only works when the power is > 0
            System.out.println("onPotPresent");
            for (boolean b : present) {
                System.out.println(b);
            }
            gui.setPotPresent(present);
        }

        @Override
        public void onPoweredOnCommand(final int powerStatus, boolean[] powered,
                final boolean[] hot) {
            System.out.println("onPoweredOnCommand:" + powerStatus);
            for (int i = 0; i < hot.length; i++) {
                boolean hotVal = hot[i];
                boolean poweredVal = powered[i];
                System.out.println("hotVal=" + hotVal + " poweredVal=" + poweredVal);
            }
            inductionState.powered = powerStatus != InductionControl.POWERSTATUS_OFF;

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    gui.setPotHot(hot);
                    if (powerStatus == InductionControl.POWERSTATUS_OFF) {
                        gui.setPowered(false);
                        gui.enablePowerControl(false);
                    } else {
                        gui.setPowered(true);
                        gui.enablePowerControl(true);
                    }
                }
            });
        }

        @Override
        public void onPowerLimitCommand(final int[] powerLevels) {
            System.out.println("onPowerLimitCommand");
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    //TODO verify
                    gui.setLimitPower(powerLevels);
                }
            });
        }

        @Override
        public void onUnknownData() {
            System.out.println("onUnknownData");
        }
    }

    private class KeyBoardCallbackImpl implements KeyBoardCallback {
        @Override
        public void onSetMainPowerCommand(boolean on) {
            System.out.println("onSetMainPowerCommand:" + on);
        }

        @Override
        public void onPowerOnCommand(int[] powerLevels) {
            System.out.println("onPowerOnCommand");
            for (int powerLevel : powerLevels) {
                System.out.println("powerLevel:" + powerLevel);
            }
            inductionState.powerLevels = powerLevels;
            //TODO Change name and figure out how to handle when power is limited
            gui.setLimitPower(powerLevels);
            //Wait with keyboard update until we got the current power
            if (firstPowerStateReceived) {
                updateKeyBoard();
            } else {
                gui.setPower(powerLevels);
            }
            firstPowerStateReceived = true;
        }

        @Override
        public void onUnknownData() {
            System.out.println("onUnknownData");
        }
    }
}
