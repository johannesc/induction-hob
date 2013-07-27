package control;

public interface ZoneController {

    /**
     * @return current desired power level
     */
    int getTargetPowerLevel();

    /**
     * @param powerLevel The desired power level from user device (i.e. induction hob
     *          or the program UI).
     */
    void setTargetPowerLevel(int powerLevel);

    /**
     * @return true if this controller is finished
     */
    boolean isFinished();

}
