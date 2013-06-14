package inductionlib;

/**
 *
 * Interface methods called when commands received from the power card
 */
public interface PowerCardCallback {

    /**
     * Called when pot presence is announced
     *
     * @param present
     *            true if a pot is present, false otherwise
     */
    public void onPotPresent(boolean[] present);

    /**
     * Sent by the power card every now and then.
     *
     * @param powerStatus
     *            0=Off, 1=On (no zones on), 3=On (at least one zone on)
     * @param powered
     *            true for each zone that is powered
     * @param hot
     *            If the corresponding zone is hot
     */
    public void onPoweredOnCommand(int powerStatus, boolean[] powered,
            boolean[] hot);

    /**
     * Sent when power is limited by power card
     *
     * @param powerLevels
     *            The power level of each zone
     */
    public void onPowerLimitCommand(int[] powerLevels);

    public void onUnknownData();

    public static final PowerCardCallback empty = new PowerCardCallback() {

        @Override
        public void onPotPresent(boolean[] present) {
        }

        @Override
        public void onPoweredOnCommand(int powerStatus, boolean[] powered,
                boolean[] hot) {
        }

        @Override
        public void onPowerLimitCommand(int[] powerLevels) {
        }

        @Override
        public void onUnknownData() {
        }

    };
}
