package HC.Controller;

public interface IController_CallCentre {
    void checkSuspend() throws InterruptedException;

    boolean checkManualMode() throws InterruptedException;
}
