package se.johannes.pctestapp;

import se.johannes.inductionlib.KeyBoardCallback;
import se.johannes.inductionlib.PowerCardCallback;
import se.johannes.inductionlib.SerialCommunication;

public class PCTestApp extends GUI.Callback implements PowerCardCallback {

    SerialCommunication com;
    GUI gui;
    int[] powerLevels = new int[4];
    private boolean powered;

    public static void main(String[] args) {
        new PCTestApp();
    }

    private PCTestApp() {
        com = new SerialCommunication(SerialCommunication.getPortId(), this,
                KeyBoardCallback.empty);
        gui = new GUI();
        gui.showGUI(this);
    }

    public void onPowerOnOffChanged(boolean power) {
        System.out.println("Turning power o" + (power ? "n" : "ff"));
        powered = power;
        com.setMainPower(powered);
    }

    public void onPowerLevelChanged(int zone, int powerLevel) {
        System.out.println("Changing powerlevels in zone " + zone + " to "
                + powerLevel);
        powerLevels[zone] = powerLevel;
        com.setPowerLevel(powerLevels);
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
    public void onPoweredOnCommand(int powerStatus, boolean[] powered,
            boolean[] hot, boolean expectAck, byte checksum) {
        gui.setPotHot(hot);
        if (powerStatus == SerialCommunication.POWERSTATUS_OFF) {
            gui.setPowered(false);
            gui.enablePowerControl(false); //TODO should be false
        } else {
            gui.setPowered(true);
            gui.enablePowerControl(true);
        }
        if (expectAck) {
            com.sendAckPacket(checksum);
        }
    }

    @Override
    public void onPowerLimitCommand(int[] powerLevels, boolean expectAck,
            byte checksum) {
        gui.setLimitPower(powerLevels);
        if (expectAck) {
            com.sendAckPacket(checksum);
        }
    }
}
