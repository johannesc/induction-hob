package control;

import inductionlib.InductionControl;
import ioio.lib.api.Induction;

public class Zone {
    private int currentPowerLevel;
    private boolean potPresent;
    private boolean hot;
    private final int zone;
    private boolean userControlling;

    private int targetPowerLevel;
    private boolean controlling = false;

    private static final int POWER_LEVEL_P = 11;
    public static final int POWER_LEVEL_MAX = 11;
    public static final int POWER_LEVEL_MIN = 1;
    public static final int POWER_LEVEL_OFF = 0;

    private static final short[] ZONE_TO_PLUS_MASK = new short[4];
    private static final short[] ZONE_TO_MINUS_MASK = new short[4];
    private static final short[] ZONE_TO_POWER_MASK = new short[4];

    static {
        ZONE_TO_PLUS_MASK[InductionControl.ZONE_LEFT_FRONT]
                   = Induction.BUTTON_MASK_PLUS_LEFT_FRONT;
        ZONE_TO_PLUS_MASK[InductionControl.ZONE_LEFT_BACK]
                   = Induction.BUTTON_MASK_PLUS_LEFT_BACK;
        ZONE_TO_PLUS_MASK[InductionControl.ZONE_RIGHT_BACK]
                   = Induction.BUTTON_MASK_PLUS_RIGHT_BACK;
        ZONE_TO_PLUS_MASK[InductionControl.ZONE_RIGHT_FRONT]
                   = Induction.BUTTON_MASK_PLUS_RIGHT_FRONT;

        ZONE_TO_MINUS_MASK[InductionControl.ZONE_LEFT_FRONT]
                   = Induction.BUTTON_MASK_MINUS_LEFT_FRONT;
        ZONE_TO_MINUS_MASK[InductionControl.ZONE_LEFT_BACK]
                   = Induction.BUTTON_MASK_MINUS_LEFT_BACK;
        ZONE_TO_MINUS_MASK[InductionControl.ZONE_RIGHT_BACK]
                   = Induction.BUTTON_MASK_MINUS_RIGHT_BACK;
        ZONE_TO_MINUS_MASK[InductionControl.ZONE_RIGHT_FRONT]
                  = Induction.BUTTON_MASK_MINUS_RIGHT_FRONT;

        ZONE_TO_POWER_MASK[InductionControl.ZONE_LEFT_FRONT]
                  = Induction.BUTTON_MASK_POWER_LEFT_FRONT;
        ZONE_TO_POWER_MASK[InductionControl.ZONE_LEFT_BACK]
                  = Induction.BUTTON_MASK_POWER_LEFT_BACK;
        ZONE_TO_POWER_MASK[InductionControl.ZONE_RIGHT_BACK]
                  = Induction.BUTTON_MASK_POWER_RIGHT_BACK;
        ZONE_TO_POWER_MASK[InductionControl.ZONE_RIGHT_FRONT]
                  = Induction.BUTTON_MASK_POWER_RIGHT_FRONT;
    }

    protected Zone(int zone) {
        this.zone = zone;
    }

    /**
     *
     * @param userControlling true if the user is controlling the zone
     *          from the induction hob.
     */
    public void reportUserPowerControl(boolean userControlling) {
        this.userControlling = userControlling;
        // User is controlling power here, lets abort any ongoing target power controlling
        if (userControlling) {
            System.out.println("user is controlling zone " + zone + " from the hob, abort our!");
            controlling = false;
        }
    }

    public int getCurrentPowerLevel() {
        return currentPowerLevel;
    }

    public void setCurrentPowerLevel(int currentPowerLevel) {
        this.currentPowerLevel = currentPowerLevel;

        if (userControlling || !controlling) {
            this.targetPowerLevel = currentPowerLevel;
        }
    }

    public int getTargetPowerLevel() {
        return targetPowerLevel;
    }

    public void setTargetPowerLevel(int targetPowerLevel) {
        if (!userControlling) {
            this.targetPowerLevel = targetPowerLevel;
            if (this.currentPowerLevel != targetPowerLevel) {
                controlling = true;
            }
       }
    }

    public void setPowered(boolean powered) {
    }

    public boolean isPotPresent() {
        return potPresent;
    }

    public void setPotPresent(boolean potPresent) {
        this.potPresent = potPresent;
    }

    public short getButtonMask() {
        short buttonMask = 0;

        if (!controlling) {
            return buttonMask;
        }

        if (potPresent || (currentPowerLevel == 0) || (targetPowerLevel == 0)) {
            if (targetPowerLevel == currentPowerLevel) {
                controlling = false;
                //System.out.println("Target reached for zone " + zone);
            } else if (targetPowerLevel == POWER_LEVEL_P) {
                buttonMask |= ZONE_TO_POWER_MASK[zone];
            } else if (targetPowerLevel == 0) {
                buttonMask |= ZONE_TO_PLUS_MASK[zone];
                buttonMask |= ZONE_TO_MINUS_MASK[zone];
            } else if (targetPowerLevel > currentPowerLevel) {
                //System.out.println("Need plus for zone " + zone);
                buttonMask |= ZONE_TO_PLUS_MASK[zone];
            } else if (targetPowerLevel < currentPowerLevel) {
                //System.out.println("Need minus for zone " + zone);
                buttonMask |= ZONE_TO_MINUS_MASK[zone];
            }
        }
        return buttonMask;
    }

    public boolean isHot() {
        return hot;
    }

    public void setHot(boolean hot) {
        this.hot = hot;
    }
}
