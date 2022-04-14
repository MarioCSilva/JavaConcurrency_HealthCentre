package HC.CallCentreHall;

import HC.Entities.TCallCentre;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MCallCentreHall implements ICallCentreHall_EntranceHall,
        ICallCentreHall_CallCentre,
        ICallCentreHall_EvaluationHall {
    private int ETHNumPatients;
    private int EVHNumPatients;
    private ReentrantLock lock1;
    private Condition cAwakeCC;
    private boolean bAwakeCC;
    private final int nEVR = 4;
    private final int maxEVH;

    public MCallCentreHall(int nos) {
        this.maxEVH = nos * nEVR;
        this.ETHNumPatients = 0;
        this.EVHNumPatients = 0;
        this.lock1 = new ReentrantLock();
        this.cAwakeCC = lock1.newCondition();
        this.bAwakeCC = false;
    }

    public void notifyETHEntrance() {
        try {
            lock1.lock();

            this.ETHNumPatients++;

            this.cAwakeCC.signal();
            this.bAwakeCC = true;
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            System.out.println("merda1");
            lock1.unlock();
        }
    }

    public void notifyEVHExit() {
        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        try {
            lock1.lock();

            this.EVHNumPatients--;

            System.out.println("Call Center Acordado");
            this.cAwakeCC.signal();
            this.bAwakeCC = true;
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            System.out.println("merda2");
            lock1.unlock();
        }
    }

    public void work(TCallCentre cc) {
        while (true) {
            int numToCall = 0;
            System.out.println("Last seen here6");

            try {
                lock1.lock();

                this.bAwakeCC = false;
                System.out.println("Last seen here5");

                while (!this.bAwakeCC)
                    this.cAwakeCC.await();

                System.out.println(
                        String.format("Current EVHPatients %d, Current ETHPatients %d", EVHNumPatients, ETHNumPatients));

                int freeSpaces = maxEVH - EVHNumPatients;
                numToCall = freeSpaces <= ETHNumPatients ? freeSpaces : ETHNumPatients;
                System.out.println(String.format("Number to Call %d", numToCall));
                System.out.println("Last seen here4");

                if (EVHNumPatients < maxEVH && ETHNumPatients > 0) {
                    this.ETHNumPatients -= numToCall;
                    this.EVHNumPatients += numToCall;
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                System.out.println("merda3");
                lock1.unlock();
            }

            for (int i = 0; i<numToCall; i++) {
                System.out.println("Last seen here0");
                cc.callETHPatient();
                System.out.println("Last seen done allala");
            }
        }
    }
}
