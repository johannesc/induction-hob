package control;

import inductionlib.InductionControl;
import ioio.lib.api.Induction;

public class InductionHob {
    private static final long MS_TO_WAIT_AFTER_USER_RELEASED = 2000L;
    private static final long MS_TO_WAIT_WHEN_CLOCK_WAS_PRESSED = 4500L;
    private final Zone[] zones = new Zone[4];
    private boolean powered = false;
    private boolean userPressed;
    private long safeToPressTime;
    private boolean firstPowerStateReceived;
    private boolean userPressedClock;
    private short lastButtonMask = 0;
    private long zeroPeriodStop;

    private static final short[] ZONE_POWER_CONTROL_MASK = new short[4];
    private static final long MS_TO_WAIT_BETWEEN_PRESSES = 1000L;
    static {
        ZONE_POWER_CONTROL_MASK[InductionControl.ZONE_LEFT_FRONT]
                = Induction.BUTTON_MASK_POWER_CONTROL_LEFT_FRONT;
        ZONE_POWER_CONTROL_MASK[InductionControl.ZONE_LEFT_BACK]
                = Induction.BUTTON_MASK_POWER_CONTROL_LEFT_BACK;
        ZONE_POWER_CONTROL_MASK[InductionControl.ZONE_RIGHT_BACK]
                = Induction.BUTTON_MASK_POWER_CONTROL_RIGHT_BACK;
        ZONE_POWER_CONTROL_MASK[InductionControl.ZONE_RIGHT_FRONT]
                = Induction.BUTTON_MASK_POWER_CONTROL_RIGHT_FRONT;
    }

    public InductionHob() {
        for (int i = 0; i < zones.length; i++) {
            zones[i] = new Zone(i);
        }
    }

    public synchronized void setTargetPowerLevel(int zone, int powerLevel) {
        System.out.println("Changing target powerlevels in zone " + zone + " to " + powerLevel);
        zones[zone].setTargetPowerLevel(powerLevel);
    }

    public synchronized int[] getTargetPowerLevels() {
        int[] targetPowerLevels = new int[zones.length];
        for (int zone = 0; zone < zones.length; zone++) {
            targetPowerLevels[zone] = zones[zone].getTargetPowerLevel();
        }
        return targetPowerLevels;
    }

    public synchronized void setCurrentPowerStatus(int powerStatus) {
        powered = (powerStatus != InductionControl.POWERSTATUS_OFF);
    }

    public synchronized void setCurrentPowerStatus(boolean[] powered) {
        for (int zone = 0; zone < powered.length; zone++) {
            zones[zone].setPowered(powered[zone]);
        }
    }

    public synchronized void setCurrentHotStatus(boolean[] hot) {
        for (int zone = 0; zone < hot.length; zone++) {
            zones[zone].setHot(hot[zone]);
        }
    }

    public synchronized boolean isPowered() {
        return powered;
    }

    public synchronized boolean[] getHot() {
        boolean[] hot = new boolean[4];
        for (int zone = 0; zone < hot.length; zone++) {
            hot[zone] = zones[zone].isHot();
        }
        return hot;
    }

    public synchronized void setCurrentPowerLevels(int[] powerLevels) {
        firstPowerStateReceived = true;
        for (int zone = 0; zone < powerLevels.length; zone++) {
            zones[zone].setCurrentPowerLevel(powerLevels[zone]);
        }
    }

    public synchronized int[] getCurrenPowerLevels() {
        int[] powerLevels = new int[zones.length];
        for (int zone = 0; zone < zones.length; zone++) {
            powerLevels[zone] = zones[zone].getCurrentPowerLevel();
        }
        return powerLevels;
    }

    public synchronized short getButtonMask() {
        short buttonMask = 0;

        if (isOkToPressButton()) {
            for (int zone = 0; zone < zones.length; zone++) {
                buttonMask |= zones[zone].getButtonMask();
                if (buttonMask != 0) {
                    // Only control one zone at a time
                    break;
                }
            }
        }
        if (lastButtonMask != buttonMask){
            if (buttonMask == 0) {
                zeroPeriodStop = System.currentTimeMillis() + MS_TO_WAIT_BETWEEN_PRESSES;
            } else {
                if (lastButtonMask == 0) {
                    if (System.currentTimeMillis() < zeroPeriodStop) {
                        System.out.println("We need to wait some time between kep presses");
                        buttonMask = 0;
                    }
                } else {
                    zeroPeriodStop = System.currentTimeMillis() + MS_TO_WAIT_BETWEEN_PRESSES;
                    buttonMask = 0;
                }
            }
            lastButtonMask = buttonMask;
        }
        return buttonMask;
    }

    private synchronized boolean isOkToPressButton() {
        if (firstPowerStateReceived) {
            if (isPowered()) {
                if (!userPressed) {
                    if (safeToPressTime < System.currentTimeMillis()) {
                        return true;
                    } else {
                        //System.out.println("User recently released a button, lets wait more");
                    }
                } else {
                    System.out.println("User is holding a button, lets wait");
                }
            }
        }
        return false;
    }

    public synchronized void setPotPresent(boolean[] present) {
        for (int zone = 0; zone < present.length; zone++) {
            zones[zone].setPotPresent(present[zone]);
        }
    }

    public synchronized boolean[] getPotPresent() {
        boolean[] present = new boolean[4];
        for (int zone = 0; zone < present.length; zone++) {
            present[zone] = zones[zone].isPotPresent();
        }
        return present;
    }

    public synchronized void reportActualButtonMask(final short buttonMask,
            final boolean userPressed) {
        if (this.userPressed != userPressed) {
            if (userPressed) {
                // User has just began pressing a button. The firmware has cleared the mask
                // for us
                this.userPressed = true;
            } else {
                // User has just released all buttons. Lets remember the time so we know when
                // it is "safe" for us to start "pressing" buttons again.
                this.userPressed = false;
                safeToPressTime = System.currentTimeMillis();
                if (userPressedClock) {
                    safeToPressTime += MS_TO_WAIT_WHEN_CLOCK_WAS_PRESSED;
                    userPressedClock = false;
                    System.out.println("Waiting extra time since clock was pressed");
                } else {
                    safeToPressTime += MS_TO_WAIT_AFTER_USER_RELEASED;
                    System.out.println("Waiting some time before we start pressing again");
                }
            }
        }
        if ((buttonMask & Induction.BUTTON_MASK_CLOCK) != 0){
            // We need to wait a bit longer
            userPressedClock = true;
        }
        System.out.println("userPressingMask" + Integer.toHexString(buttonMask));
        for (int zone = 0; zone < zones.length; zone++) {
            boolean userControllingZone = userPressed &&
                    (ZONE_POWER_CONTROL_MASK[zone] & buttonMask) != 0;
            zones[zone].reportUserPowerControl(userControllingZone);
            System.out.println("Zone " + zone + " userControlling:" + userControllingZone);
        }
    }

    public void setProgram(int zone, ZoneController zoneController) {
        zones[zone].setProgram(zoneController);
    }
}
