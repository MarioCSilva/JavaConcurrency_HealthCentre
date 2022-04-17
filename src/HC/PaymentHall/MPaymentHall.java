package HC.PaymentHall;

import HC.Entities.TCashier;
import HC.Entities.TPatient;
import HC.Queue.PriorityQueue;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MPaymentHall implements IPaymentHall_Cashier, IPaymentHall_Patient {
    private int PYN = 1;
    private int currentPYN = 1;
    private final int size;
    private final ReentrantLock rl;
    private final PriorityQueue paymentQueue;
    private final Condition cArrayPQ[];
    private final boolean bArrayPQ[];
    private final Condition cNotFullPQ;


    private final Condition cCashier;
    private int numPayments = 0;

    public MPaymentHall(int size) {
        this.size = size;
        this.rl = new ReentrantLock();
        this.paymentQueue = new PriorityQueue(size);
        this.cCashier = rl.newCondition();
        this.cNotFullPQ = rl.newCondition();
        this.cArrayPQ = new Condition[size];
        this.bArrayPQ = new boolean[size];
        for (int i = 0; i < size; i++) {
            this.bArrayPQ[i] = false;
            this.cArrayPQ[i] = rl.newCondition();
        }
    }

    
    /** 
     * Method to be called by a Patient Entity to enter this hall.
     *
     * @param patient a Patient that has entered a Hall
     * @throws InterruptedException
     * @throws IOException
     */
    public void enterHall(TPatient patient) throws InterruptedException, IOException {
        int patientIdx = 0;

        // give an PYN to each patient upon entering ETH
        try {
            rl.lock();

            int patientPYN = PYN++;
            // assign a Payment Number to the Patient
            patient.setTN(patientPYN);

            patient.log("PYH");

            // wait while room is full
            while (paymentQueue.isFull() || (currentPYN != patientPYN))
                cNotFullPQ.await();

            patient.checkSuspend();

            // assign the patient to a room
            patientIdx = paymentQueue.put(patient);

            // increase room counter
            paymentQueue.incCounter();

            // signal cashier to handle new payment
            numPayments++;
            cCashier.signal();

            // stay blocked on room since it has entered
            while (!bArrayPQ[patientIdx])
                cArrayPQ[patientIdx].await();

            bArrayPQ[patientIdx] = false;

            patient.checkSuspend();

        } finally {
            rl.unlock();
        }
    }

    
    /** 
     * Method called by the Cashier check for patients in its room and check them.
     *
     * @param cashier the Cashier of the room
     * @throws InterruptedException
     * @throws IOException
     */
    @Override
    public void work(TCashier cashier) throws InterruptedException, IOException {
        TPatient patient;
        int patientIdx = 0;

        while (true) {
            patient = null;
            try {
                rl.lock();
                // if the Cashier has been signaled twice, it wont do
                // two payments at the same time, neither retrieve a patient from the queue
                // if no patients are on it
                while (numPayments == 0)
                    cCashier.await();

                numPayments--;
                
                // gets the patient with higher priority, regarding its PYN
                for (int i = 0; i < size; i++) {
                    if (paymentQueue.getQueue()[i] != null && (patient == null || patient.getTN() > paymentQueue.getQueue()[i].getTN())) {
                        patient = paymentQueue.getQueue()[i];
                        patientIdx = i;
                    }
                }
            } finally {
                rl.unlock();
            }
            // cashier received the payment
            cashier.receivePayment();

            try {
                rl.lock();
                //signals the patient in cause to end its lifecycle
                bArrayPQ[patientIdx] = true;
                cArrayPQ[patientIdx].signal();

                currentPYN++;
                // write on the logger
                paymentQueue.getQueue()[patientIdx].log("OUT");

                paymentQueue.getPatientById(patientIdx);

                cNotFullPQ.signalAll();

                paymentQueue.decCounter();

            } finally {
                rl.unlock();
            }
        }
    }
}
