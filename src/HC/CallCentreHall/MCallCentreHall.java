package HC.CallCentreHall;

import HC.Entities.TCallCentre;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MCallCentreHall implements ICallCentreHall_Patient,
        ICallCentreHall_CallCentre {
    private int ETHNumPatients;
    private int EVHNumPatients;
    private int WTHNumPatients;
    private int MDHNumPatients;
    private ReentrantLock lock1;
    private Condition cAwakeCC;
    private boolean bAwakeCC;
    private final int maxSeatsEVR = 4;
    private final int maxSeatsMDW = 2;

    public MCallCentreHall(int nos) {
        this.ETHNumPatients = 0;
        this.EVHNumPatients = 0;
        this.WTHNumPatients = 0;
        this.MDHNumPatients = 0;
        this.lock1 = new ReentrantLock();
        this.cAwakeCC = lock1.newCondition();
        this.bAwakeCC = false;
    }

    public void notifyEntrance(String hall) {
        System.out.println(String.format("entrance to hall %s", hall));
        lock1.lock();

        if ( hall.equals("ETH") )
            this.ETHNumPatients++;
        else
            this.WTHNumPatients++;

        this.cAwakeCC.signal();
        this.bAwakeCC = true;
        System.out.println(String.format("merda1 %s", hall));

        lock1.unlock();
    }

    public void notifyExit(String hall) {
        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        lock1.lock();

        if ( hall.equals("EVH") )
            this.EVHNumPatients--;
        else {
            this.MDHNumPatients--;
            System.out.println(
                String.format("asdasdsa WTHPatients %d, MDHPatients %d",
                    WTHNumPatients, MDHNumPatients));
        }

        System.out.println("Call Center Acordado");
        this.cAwakeCC.signal();
        this.bAwakeCC = true;
        System.out.println("merda2");

        lock1.unlock();
    }

    public void work(TCallCentre cc) {
        while (true) {
            int numToCallETH = 0, numToCallWTH = 0, freeSpaces;
            System.out.println("Last seen here6");

            try {
                lock1.lock();

                System.out.println("Last seen here5");

                while (!this.bAwakeCC)
                    this.cAwakeCC.await();

                this.bAwakeCC = false;

                System.out.println(
                        String.format("EVHPatients %d, ETHPatients %d, WTHPatients %d, MDHPatients %d",
                            EVHNumPatients, ETHNumPatients, WTHNumPatients, MDHNumPatients));

                freeSpaces = maxSeatsEVR - EVHNumPatients;
                numToCallETH = freeSpaces <= ETHNumPatients ? freeSpaces : ETHNumPatients;
                System.out.println(String.format("Number to Call ETH %d", numToCallETH));

                if (EVHNumPatients < maxSeatsEVR && ETHNumPatients > 0) {
                    for (int i = 0; i<numToCallETH; i++)
                        cc.callETHPatient();
                    ETHNumPatients -= numToCallETH;
                    EVHNumPatients += numToCallETH;
                } else
                    numToCallETH = 0;

                freeSpaces = maxSeatsMDW - MDHNumPatients;
                numToCallWTH = freeSpaces <= WTHNumPatients ? freeSpaces : WTHNumPatients;
                System.out.println(String.format("Number to Call WTH %d", numToCallWTH));

                if (MDHNumPatients < maxSeatsMDW && WTHNumPatients > 0) {
                    WTHNumPatients -= numToCallWTH;
                    MDHNumPatients += numToCallWTH;
                    System.out.println(
                        String.format("WTHPatients %d, MDHPatients %d",
                            WTHNumPatients, MDHNumPatients));
                } else
                    numToCallWTH = 0;

            } catch (Exception e) {
                System.out.println(e);
            } finally {
                System.out.println("merda3");
                lock1.unlock();
            }

            // for (int i = 0; i<numToCallETH; i++)
            //     cc.callETHPatient();
            
            for (int i = 0; i<numToCallWTH; i++)
                cc.callWTHPatient();
        }
    }
}
