package control;

import inductionlib.InductionControl;
import ioio.lib.api.Induction;

public class InductionHob {
    private static final long MS_TO_WAIT_AFTER_USER_RELEASED = 2000L;
    private final Zone[] zones = new Zone[4];
    private boolean powered = false;
    private boolean userPressed;
    private long userReleasedTime;
    private boolean firstPowerStateReceived;

    private static final short[] ZONE_POWER_CONTROL_MASK = new short[4];
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
            }
        }
        return buttonMask;
    }

    private synchronized boolean isOkToPressButton() {
        if (firstPowerStateReceived) {
            if (isPowered()) {
                if (!userPressed) {
                    if (userReleasedTime < (System.currentTimeMillis()
                            - MS_TO_WAIT_AFTER_USER_RELEASED)) {
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
        short ourMask = getButtonMask();
        System.out.println("ourMask" + Integer.toHexString(ourMask));
        if (this.userPressed != userPressed) {
            if (userPressed) {
                // User has just began pressing a button. The firmware has cleared the mask
                // for us
                this.userPressed = true;
            } else {
                // User has just released all buttons. Lets remember the tim so we know when
                // it is "safe" for us to start "pressing" buttons again.
                this.userPressed = false;
                userReleasedTime = System.currentTimeMillis();
            }
        }
        // Compensate for our "own" mask here.
        short userPressingMask = (short)((buttonMask & ~ourMask) & 0xFFFF);
        System.out.println("userPressingMask" + Integer.toHexString(userPressingMask));
        for (int zone = 0; zone < zones.length; zone++) {
            boolean userControllingZone = userPressed &&
                    (ZONE_POWER_CONTROL_MASK[zone] & userPressingMask) != 0;
            zones[zone].reportUserPowerControl(userControllingZone);
            System.out.println("Zone " + zone + " userControlling:" + userControllingZone);
        }
    }
}
