package HC.Entities;


import HC.CallCentreHall.ICallCentreHall_CallCentre;
import HC.EntranceHall.IEntranceHall_CallCentre;
import HC.Controller.IController_CallCentre;
import HC.MedicalHall.IMedicalHall_CallCentre;
import HC.WaitingHall.IWaitingHall_CallCentre;


public class TCallCentre extends Thread {

    private final IEntranceHall_CallCentre eth;
    private final ICallCentreHall_CallCentre cch;
    private final IWaitingHall_CallCentre wth;
    private final IMedicalHall_CallCentre mdh;

    public IController_CallCentre getController() {
        return controller;
    }

    private final IController_CallCentre controller;

    public TCallCentre(IController_CallCentre controller, IEntranceHall_CallCentre eth, ICallCentreHall_CallCentre cch,
                       IWaitingHall_CallCentre wth, IMedicalHall_CallCentre mdh) {
        this.controller = controller;
        this.eth = eth;
        this.cch = cch;
        this.wth = wth;
        this.mdh = mdh;
    }

    @Override
    public void run() {
        try {
            cch.work(this);
        } catch (InterruptedException e) {
            System.out.println("Call Centre has died");
            Thread.currentThread().interrupt();
        }
    }


    public void kill() {
        this.interrupt();
    }


    public void callETHPatient() throws InterruptedException {
        controller.checkSuspend();
        eth.exitHall();
    }

    public void callWTHAPatient() throws InterruptedException {
        controller.checkSuspend();
        wth.exitHall("A");
    }

    public void callWTHCPatient() throws InterruptedException {
        controller.checkSuspend();
        wth.exitHall("C");
    }

    public void callMDWAPatient() throws InterruptedException {
        controller.checkSuspend();
        mdh.exitWaitingRoom("A");
    }

    public void callMDWCPatient() throws InterruptedException {
        controller.checkSuspend();
        mdh.exitWaitingRoom("C");
    }

}
