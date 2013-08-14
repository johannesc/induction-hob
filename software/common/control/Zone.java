package control;

import inductionlib.InductionControl;
import ioio.lib.api.Induction;

public class Zone {
    private int currentPowerLevel;
    //private int targetPowerLevel;
    private boolean powered;
    private boolean potPresent;
    private boolean hot;
    private final int zone;
    private boolean userControlling;

    // TODO fix the zoneController in a smart way!
    // must handle user presses on the induction hob as well in the ui
    private ZoneController zoneController = new DummyZoneController(0);

    private class DummyZoneController implements ZoneController {
        private int targetPowerLevel;

        public DummyZoneController(int targetPowerLevel) {
            this.targetPowerLevel = targetPowerLevel;
        }

        @Override
        public int getTargetPowerLevel() {
            return targetPowerLevel;
        }

        @Override
        public void setTargetPowerLevel(int powerLevel) {
            System.out.println("DummyZoneController:setTargetPowerLevel:" + powerLevel);
            targetPowerLevel = powerLevel;
        }

        @Override
        public boolean isFinished() {
            // We never finish!
            return false;
        }
    }

    private static final int POWER_LEVEL_P = 11;

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
        // TODO: This seems a bit ugly... UI does not get updated?
        /* Disabled for now, seems to be unreliable at the moment, have to dig into the firmware
        if (userControlling) {
            //this.targetPowerLevel = currentPowerLevel;
            zoneController.setTargetPowerLevel(currentPowerLevel);
        }
        */
    }

    public int getCurrentPowerLevel() {
        return currentPowerLevel;
    }

    public void setCurrentPowerLevel(int currentPowerLevel) {
        this.currentPowerLevel = currentPowerLevel;
        //TODO: UI is not aware of this...
        if (userControlling) {
            //this.targetPowerLevel = currentPowerLevel;
            zoneController.setTargetPowerLevel(currentPowerLevel);
        }
    }

    public int getTargetPowerLevel() {
        return zoneController.getTargetPowerLevel();
        //return targetPowerLevel;
    }

    public void setTargetPowerLevel(int targetPowerLevel) {
        if (!userControlling) {
            //this.targetPowerLevel = targetPowerLevel;
            zoneController.setTargetPowerLevel(targetPowerLevel);
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
        int targetPowerLevel = zoneController.getTargetPowerLevel();
        boolean finished = zoneController.isFinished();
        if (finished) {
            // Controller finished TODO tell UI about this in some smart way
            System.out.println("ZoneController finished, creating a dummy one at level=" + targetPowerLevel);
            this.zoneController = new DummyZoneController(targetPowerLevel);
        }
        if (potPresent || (currentPowerLevel == 0) || (targetPowerLevel == 0)) {
            if (targetPowerLevel == currentPowerLevel) {
                //System.out.println("Target reached for zone " + zone);
            } else if (targetPowerLevel == POWER_LEVEL_P) {
                buttonMask |= ZONE_TO_POWER_MASK[zone];
            } else if (targetPowerLevel > currentPowerLevel) {
                //System.out.println("Need plus for zone " + zone);
                buttonMask |= ZONE_TO_PLUS_MASK[zone];
            } else if (targetPowerLevel < currentPowerLevel) {
                //System.out.println("Need minus for zone " + zone);
                buttonMask |= ZONE_TO_MINUS_MASK[zone];
            } else if (targetPowerLevel == 0) {
                buttonMask |= ZONE_TO_PLUS_MASK[zone];
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

    public void setProgram(ZoneController zoneController) {
        if (zoneController == null) {
            this.zoneController = new DummyZoneController(currentPowerLevel);
        } else {
            this.zoneController = zoneController;
        }
    }
}
