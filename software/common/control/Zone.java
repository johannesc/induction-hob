package control;

import inductionlib.InductionControl;
import ioio.lib.api.Induction;

public class Zone {
    private int currentPowerLevel;
    private int targetPowerLevel;
    private boolean powered;
    private boolean potPresent;
    private boolean hot;
    private final int zone;
    private boolean userControlling;

    private static final short[] ZONE_TO_PLUS_MASK = new short[4];
    private static final short[] ZONE_TO_MINUS_MASK = new short[4];
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
    }

    public Zone(int zone) {
        this.zone = zone;
    }

    public void reportUserPowerControl(boolean userControlling) {
        this.userControlling = userControlling;
        // User is controlling power here, lets abort any ongoing target power controlling
        // TODO: This seems a bit ugly... UI does not get updated?
        if (userControlling) {
            this.targetPowerLevel = currentPowerLevel;
        }
    }

    public int getCurrentPowerLevel() {
        return currentPowerLevel;
    }
    public void setCurrentPowerLevel(int currentPowerLevel) {
        this.currentPowerLevel = currentPowerLevel;
        //TODO: UI is not aware of this...
        if (userControlling) {
            this.targetPowerLevel = currentPowerLevel;
        }
    }
    public int getTargetPowerLevel() {
        return targetPowerLevel;
    }
    public void setTargetPowerLevel(int targetPowerLevel) {
        if (!userControlling) {
            this.targetPowerLevel = targetPowerLevel;
        }
    }
    public void setPowered(boolean powered) {
        this.powered = powered;
    }
    public boolean isPotPresent() {
        return potPresent;
    }
    public void setPotPresent(boolean potPresent) {
        this.potPresent = potPresent;
    }

    public short getButtonMask() {
        short buttonMask = 0;
        if (potPresent || (currentPowerLevel == 0)) {
            if (targetPowerLevel > currentPowerLevel) {
                //System.out.println("Need plus for zone " + zone);
                buttonMask |= ZONE_TO_PLUS_MASK[zone];
            } else if (targetPowerLevel < currentPowerLevel) {
                //System.out.println("Need minus for zone " + zone);
                buttonMask |= ZONE_TO_MINUS_MASK[zone];
            } else {
                //System.out.println("Target reached for zone " + zone);
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
