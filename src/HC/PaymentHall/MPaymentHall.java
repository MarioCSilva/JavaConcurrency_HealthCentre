package HC.PaymentHall;

import HC.Entities.TCashier;
import HC.Entities.TPatient;
import HC.FIFO.MFIFO;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MPaymentHall implements IPaymentHall_Cashier, IPaymentHall_Patient {
    private int PYN = 1;
    private int currentPYN = 1;
    private final int size;
    private final ReentrantLock rl;
    private final MFIFO paymentQueue;
    private final Condition cArrayPQ[];
    private final boolean bArrayPQ[];
    private final Condition cNotFullPQ;


    private final Condition cCashier;
    private int numPayments = 0;

    public MPaymentHall(int size) {
        this.size = size;
        this.rl = new ReentrantLock();
        this.paymentQueue = new MFIFO(size);
        this.cCashier = rl.newCondition();
        this.cNotFullPQ = rl.newCondition();
        this.cArrayPQ = new Condition[size];
        this.bArrayPQ = new boolean[size];
        for(int i = 0; i< size; i++) {
            this.bArrayPQ[i] = false;
            this.cArrayPQ[i] = rl.newCondition();
        }
    }

    public void enterHall(TPatient patient) {
        int patientIdx = 0;

        // give an PYN to each patient upon entering ETH
        try {
            rl.lock();

            int patientPYN = PYN++;
            
            patient.setTN(patientPYN);

            patient.log("PYH");

            // wait while room is full
            while (paymentQueue.isFull() || (currentPYN != patientPYN) )
                cNotFullPQ.await();

            // assign the patient to a room
            patientIdx = paymentQueue.put(patient);

            // increase room counter
            paymentQueue.incCounter();

            // signal cashier to handle new payment
            numPayments++;
            cCashier.signal();

            // stay blocked on room since it has entered
            while ( !bArrayPQ[ patientIdx ] )
                cArrayPQ[ patientIdx ].await();

            bArrayPQ[ patientIdx ] = false;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public void work(TCashier cashier) {
        TPatient patient;
        int patientIdx = 0;
        
        while (true){
            patient = null;
            try {
                rl.lock();

                while ( numPayments == 0 )
                    cCashier.await();

                numPayments--;

                for (int i=0; i<size; i++) {
                    if (paymentQueue.getFIFO()[i] != null && (patient == null || patient.getTN() > paymentQueue.getFIFO()[i].getTN())) {
                        patient = paymentQueue.getFIFO()[i];
                        patientIdx = i;
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                rl.unlock();
            }

            cashier.receivePayment();

            try {
                rl.lock();
                bArrayPQ[ patientIdx ] = true;
                cArrayPQ[ patientIdx ].signal();

                currentPYN++;

                paymentQueue.getFIFO()[ patientIdx ].log("OUT");

                paymentQueue.getPatientById(patientIdx);

                cNotFullPQ.signalAll();

                paymentQueue.decCounter();

            } finally {
                rl.unlock();
            }
        }
    }
}
