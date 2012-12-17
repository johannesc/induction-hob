package se.johannes.inductionlib;

/**
 * 
 * Interface methods called when commands received from the keyboard
 */
public interface KeyBoardCallback {

    /**
     * Called to set main power on/off from the keyboard
     * @param on If main power should be on (fan is also started)
     */
    public void onSetMainPowerCommand(boolean on);

    /**
     * Called when changing power level
     * @param powerLevels
     */
    public void onPowerOnCommand(int[] powerLevels);

    public void onUnknownData();

    public static final KeyBoardCallback empty = new KeyBoardCallback() {

        @Override
        public void onSetMainPowerCommand(boolean on) {
        }

        @Override
        public void onPowerOnCommand(int[] powerLevels) {
        }

        @Override
        public void onUnknownData() {
        }
    };
}
