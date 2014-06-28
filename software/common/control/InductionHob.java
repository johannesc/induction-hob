package control;

import inductionlib.InductionControl;
import ioio.lib.api.Induction;
import ioio.lib.api.Induction.ButtonMaskChangedEvent;
import ioio.lib.api.Induction.InductionEvent;
import ioio.lib.api.exception.ConnectionLostException;

public class InductionHob implements Induction.EventCallback, Runnable {
    private static final long MS_TO_WAIT_AFTER_USER_RELEASED = 2000L;
    private static final long MS_TO_WAIT_WHEN_CLOCK_WAS_PRESSED = 4500L;
    private static final long MS_TO_WAIT_BETWEEN_PRESSES = 1000L;

    private final Zone[] zones = new Zone[4];
    private boolean powered = false;
    private boolean userPressed;
    private long safeToPressTime;
    private boolean firstPowerStateReceived;
    private boolean userPressedClock;
    private short lastButtonMask = 0;
    //private long zeroPeriodStop;
    private final Induction induction;
    private boolean running = true;
    private short lastMask = 0;

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

    public InductionHob(Induction induction) {
        this.induction = induction;
        for (int i = 0; i < zones.length; i++) {
            zones[i] = new Zone(i);
        }
        induction.registerCallback(this);
        Thread updateThread = new Thread(this);
        updateThread.start();
    }

    public synchronized void setTargetPowerLevel(int zone, int powerLevel) {
        System.out.println("Changing target powerlevels in zone " + zone + " to " + powerLevel);
        zones[zone].setTargetPowerLevel(powerLevel);
        notifyAll();
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
        notifyAll();
    }

    public synchronized void setCurrentPowerStatus(boolean[] powered) {
        for (int zone = 0; zone < powered.length; zone++) {
            zones[zone].setPowered(powered[zone]);
        }
        notifyAll();
    }

    public synchronized void setCurrentHotStatus(boolean[] hot) {
        for (int zone = 0; zone < hot.length; zone++) {
            zones[zone].setHot(hot[zone]);
        }
        notifyAll();
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
        notifyAll();
    }

    public synchronized int[] getCurrenPowerLevels() {
        int[] powerLevels = new int[zones.length];
        for (int zone = 0; zone < zones.length; zone++) {
            powerLevels[zone] = zones[zone].getCurrentPowerLevel();
        }
        return powerLevels;
    }

    private synchronized short getButtonMask() {
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
        // Make sure we wait some time before we press a new button combination
        if (lastButtonMask != buttonMask){
            if (buttonMask == 0) {
                safeToPressTime = System.currentTimeMillis() + MS_TO_WAIT_BETWEEN_PRESSES;
            } else {
                if (lastButtonMask == 0) {
                    if (System.currentTimeMillis() < safeToPressTime) {
                        System.out.println("We need to wait some time between kep presses");
                        buttonMask = 0;
                    }
                } else {
                    safeToPressTime = System.currentTimeMillis() + MS_TO_WAIT_BETWEEN_PRESSES;
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
                        userPressedClock = false;
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

    private synchronized void reportActualButtonMask(final short buttonMask,
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
        notifyAll();
    }

    @Override
    public void notifyEvent(InductionEvent event) {
        if (event instanceof ButtonMaskChangedEvent) {
            ButtonMaskChangedEvent butChangedEvent = (ButtonMaskChangedEvent) event;
            boolean userPressed = butChangedEvent.getUserPressed();
            short buttonMask = butChangedEvent.getButtonMask();
            System.out.println("ButtonMaskChangedEvent:"
                    + Integer.toHexString(buttonMask & 0xFFFF)
                    + " userPressed = " + userPressed);
            reportActualButtonMask(buttonMask, userPressed);
        } else {
            System.out.println("Got unknown event:" + event);
        }
    }

    public synchronized void stop() {
        System.out.println(this.getClass().getSimpleName() + ": Stopping thread");
        running = false;
        notifyAll();
    }

    @Override
    public synchronized void run() {
        while (running) {
            try {
                short buttonMask = getButtonMask();
                if (lastMask  != buttonMask) {
                    System.out.println("Setting mask to: " + Integer.toHexString(buttonMask));
                    induction.setInductionButtonMask(buttonMask);
                    lastMask = buttonMask;
                }

                long now = System.currentTimeMillis();
                long nextUpdate = safeToPressTime - now;
                if (nextUpdate > 0) {
                    System.out.println(this.getClass().getSimpleName() +
                            ": wait "+ nextUpdate + "ms");
                    this.wait(nextUpdate);
                } else {
                    System.out.println(this.getClass().getSimpleName() + ": wait forever");
                    this.wait();
                }
                System.out.println(this.getClass().getSimpleName() + ": wait is over");
            } catch (InterruptedException e) {
                //Ignore silently
                System.out.println(this.getClass().getSimpleName() + ": Interrupted!");
            } catch (ConnectionLostException e) {
                System.out.println("Connection lost, exiting thread");
                e.printStackTrace();
                running = false;
            }
        }
        System.out.println(this.getClass().getSimpleName() + ": Thread is exiting");
    }
}
