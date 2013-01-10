package se.johannes.pctestapp;

import javax.swing.SwingUtilities;

import se.johannes.inductionlib.KeyBoardCallback;
import se.johannes.inductionlib.PowerCardCallback;
import se.johannes.inductionlib.InductionControl;

// I use the AWT-thread to send data to the induction cooker
// this is not the best thing to do, but its just a hack anyway...
//
public class PCTestApp extends GUI.Callback implements PowerCardCallback {

    InductionControl com;
    GUI gui;
    int[] powerLevels = new int[4];
    private boolean powered;
    Thread timerThread = null;

    public static void main(String[] args) {
        new PCTestApp();
    }

    private PCTestApp() {
        SerialPortHelper spHelp = new SerialPortHelper();
        com = new InductionControl(spHelp.getInputStream(),
                spHelp.getOutputStream(), this, KeyBoardCallback.empty);
        gui = new GUI();
        gui.showGUI(this);
    }

    @Override
    public void onPowerOnOffChanged(boolean power) {
        System.out.println("Turning power o" + (power ? "n" : "ff"));
        powered = power;
        com.setMainPower(powered);
    }

    @Override
    public void onPowerLevelChanged(int zone, int powerLevel) {
        System.out.println("Changing powerlevels in zone " + zone + " to "
                + powerLevel);
        powerLevels[zone] = powerLevel;
        com.setPowerLevel(powerLevels);
        boolean inUse = false;
        for (int i = 0; i < powerLevels.length; i++) {
            if (powerLevels[i] != InductionControl.POWER_LEVEL_0) {
                inUse = true;
                break;
            }
        }
        if (inUse) {
            StartTimer();
        } else {
            stopTimer();
        }
    }

    private synchronized void stopTimer() {
        if (timerThread != null) {
            Thread temp = timerThread;
            timerThread = null;
            temp.interrupt();
            try {
                System.out.println("Waiting for thread to finish...");
                temp.join(0);
                System.out.println("...Done!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void StartTimer() {
        if (timerThread == null) {
            timerThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        do {
                            Thread.sleep(10000);
                            //Use same thread for all communication
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    com.setPowerLevel(powerLevels);
                                }
                            });
                        } while (timerThread != null);
                    } catch (InterruptedException e) {
                        // Normal code flow
                    }
                    System.out.println("Timer thread ending...");
                }
            });
            timerThread.start();
        }
    }

    @Override
    public void onUnknownData() {
        System.err.println("onUnknownData");
    }

    @Override
    public void onPotPresent(boolean[] present, boolean expectAck,
            byte checksum) {
        gui.setPotPresent(present);
        if (expectAck) {
            com.sendAckPacket(checksum);
        }
    }

    @Override
    public void onPoweredOnCommand(final int powerStatus, boolean[] powered,
            final boolean[] hot, final boolean expectAck, final byte checksum) {
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
                if (expectAck) {
                    com.sendAckPacket(checksum);
                }
            }
        });
    }

    @Override
    public void onPowerLimitCommand(final int[] powerLevels,
            final boolean expectAck, final byte checksum) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                gui.setLimitPower(powerLevels);
                if (expectAck) {
                    com.sendAckPacket(checksum);
                }
            }
        });
    }
}
