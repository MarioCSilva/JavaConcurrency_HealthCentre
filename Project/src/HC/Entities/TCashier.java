package HC.Entities;

import HC.Controller.IController_Cashier;
import HC.PaymentHall.IPaymentHall_Cashier;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TCashier extends Thread {

    private final int PYT;
    private final IPaymentHall_Cashier pyh;
    private final IController_Cashier controller;

    public TCashier(IController_Cashier controller, int PYT, IPaymentHall_Cashier pyh) {
        this.controller = controller;
        this.PYT = PYT;
        this.pyh = pyh;
    }

    
    /** 
     * Method used to receive Payments from a Patient.
     * Theads sleeps a random time to the range applied by PYT,
     * if greater than zero.
     * @throws InterruptedException
     */
    public void receivePayment() throws InterruptedException {
        controller.checkSuspend();
        // payment time
        if (PYT > 0) {
            try {
                int appTime = new Random().nextInt(PYT);
                TimeUnit.MILLISECONDS.sleep(appTime);
            } catch (InterruptedException e) {
            }
            ;
            controller.checkSuspend();
        }
    }

    @Override
    public void run() {
        try {
            pyh.work(this);
        } catch (InterruptedException | IOException e) {
            System.out.println("Cashier has died");
            Thread.currentThread().interrupt();
        }
    }
    /**
     * Method responsible for killing the Thread
     */
    public void kill() {
        this.interrupt();
    }
}