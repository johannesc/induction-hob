package se.johannes.pctestapp;

import se.johannes.inductionlib.KeyBoardCallback;
import se.johannes.inductionlib.PowerCardCallback;
import se.johannes.inductionlib.SerialCommunication;

public class PCTestApp extends GUI.Callback implements KeyBoardCallback,
        PowerCardCallback {

    SerialCommunication com;
    GUI gui;
    int[] powerLevels = new int[4];

    public static void main(String[] args) {
        new PCTestApp();
    }

    private PCTestApp() {
        com = new SerialCommunication(SerialCommunication.getPortId(), this,
                this);
        gui = new GUI();
        gui.showGUI(this);
    }

    public void onPowerLevelChanged(int zone, int powerLevel) {
        System.out.println("Changing powerlevels!");
        powerLevels[zone] = powerLevel;
        com.setPowerLevel(powerLevels);
    }

    @Override
    public void onSetMainPowerCommand(boolean on) {
        System.out.println("onSetMainPowerCommand: " + on);
    }

    @Override
    public void onPowerOnCommand(int[] powerLevels) {
        //TODO print something more useful
        System.out.println("onPowerOnCommand");
    }

    @Override
    public void onUnknownData() {
        System.err.println("onUnknownData");
    }

    @Override
    public void onPotPresent(boolean[] present) {
        gui.setPotPresent(present);
    }

    @Override
    public void onPoweredOnCommand(int powerStatus, int[] powerLevels,
            boolean[] hot) {
        gui.setPotHot(hot);
        gui.setPower(powerLevels);
    }

    @Override
    public void onPowerLimitCommand(int[] powerLevels) {
        gui.setLimitPower(powerLevels);
    }
}
