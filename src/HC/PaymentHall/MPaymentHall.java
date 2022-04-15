package HC.PaymentHall;

import HC.Entities.TCashier;
import HC.Entities.TPatient;
import HC.FIFO.MFIFO;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MPaymentHall implements IPaymentHall_Cashier, IPaymentHall_Patient {
    private int PYN = 1;
    private final int size;
    private final ReentrantLock rl;
    private final MFIFO paymentQueue;
    private final Condition cArrayPQ[];
    private final boolean[] bArrayQ;
    private final Condition cNotFullPQ;


    private final Condition cCashier;
    private int numPayments = 0;

    public MPaymentHall(int size) {
        this.size = size;
        this.rl = new ReentrantLock();
        this.paymentQueue = new MFIFO(size);
        this.cNotFullPQ = rl.newCondition();
        this.cCashier = rl.newCondition();
        this.cArrayPQ = new Condition[size];
        this.bArrayQ = new boolean[size];
        for(int i = 0; i< size; i++){
            this.bArrayQ[i] = false;
            this.cArrayPQ[i] = rl.newCondition();
        }
    }

    public void enterHall(TPatient patient) {
        int patientIdx = 0;

        // give an PYN to each patient upon entering ETH
        try {
            rl.lock();

            patient.setTN(PYN++);

            patient.log("PYH");

            // wait while room is full
            while (paymentQueue.isFull())
                cNotFullPQ.await();
            
            // assign the patient to a room
            patientIdx = paymentQueue.put(patient);

            // increase room counter
            paymentQueue.incCounter();

            // signal cashier to handle new payment
            numPayments++;
            cCashier.signal();

            // stay blocked on room since it has entered
            while ( !bArrayQ[ patientIdx ] )
                cArrayPQ[ patientIdx ].await();

            bArrayQ[ patientIdx ] = false;

            paymentQueue.getPatientById(patientIdx);

            if ( paymentQueue.isFull() )
                cNotFullPQ.signal();

            paymentQueue.decCounter();

            patient.log("OUT");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println(String.format("SAI DO ETH AGORA %s", patient));
            rl.unlock();
        }
    }

    @Override
    public void work(TCashier cashier) {
        TPatient patient = null;
        int patientIdx = 0;
        
        while (true){
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
                bArrayQ[ patientIdx ] = true;
                cArrayPQ[ patientIdx ].signal();
            } finally {
                rl.unlock();
            }
        }
    }
}
