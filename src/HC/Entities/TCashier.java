package HC.Entities;

import HC.PaymentHall.IPaymentHall_Cashier;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TCashier extends Thread {
    private final int PYT;
    private final IPaymentHall_Cashier pyh;

    public TCashier(int PYT, IPaymentHall_Cashier pyh){
        this.PYT = PYT;
        this.pyh = pyh;
    }

    public void receivePayment() {
        // evaluation time
        if (PYT > 0) {
            try {
                int appTime = new Random().nextInt(PYT);
                TimeUnit.MILLISECONDS.sleep(appTime);
            } catch (InterruptedException e) {};
        }
    }

    @Override
    public void run() {
        pyh.work(this);
    }
}