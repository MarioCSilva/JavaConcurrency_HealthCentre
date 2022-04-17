package HC.Controller;

import java.util.concurrent.locks.Condition;

public interface IController_CallCentre {
    void checkSuspend() throws InterruptedException;

    boolean checkManualMode() throws InterruptedException;

    boolean getIsManualMode();
}
