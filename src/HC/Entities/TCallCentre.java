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

    
    /**
     * Method responsible for killing the Thread
     */
    public void kill() {
        this.interrupt();
    }


    /** 
     * @throws InterruptedException
     */
    public void callETHPatient() throws InterruptedException {
        controller.checkSuspend();
        eth.exitHall();
    }

    
    /** 
     * 
     * @throws InterruptedException
     */
    public void callWTHAPatient() throws InterruptedException {
        controller.checkSuspend();
        wth.exitHall("A");
    }

    
    /** 
     * @throws InterruptedException
     */
    public void callWTHCPatient() throws InterruptedException {
        controller.checkSuspend();
        wth.exitHall("C");
    }

    
    /** 
     * @throws InterruptedException
     */
    public void callMDWAPatient() throws InterruptedException {
        controller.checkSuspend();
        mdh.exitWaitingRoom("A");
    }

        
    /** 
     * @return IController_CallCentre
     */
    public IController_CallCentre getController() {
        return controller;
    }

    
    /** 
     * @throws InterruptedException
     */
    public void callMDWCPatient() throws InterruptedException {
        controller.checkSuspend();
        mdh.exitWaitingRoom("C");
    }

}
