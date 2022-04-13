package HC.FIFO;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import HC.CallCentreHall.ICallCentreHall_EntranceHall;
import HC.Entities.TPatient;

public class MFIFO implements IFIFO {
    private int idxPut = 0;
    private int idxGet = 0;
    private int count = 0;
    
    private final TPatient fifo[];
    private final int size;
    private final ReentrantLock rl;
    private final Condition cNotFull;
    private final Condition cNotEmpty;
    private final Condition cArray[];
    private final boolean[] bExit;
    private boolean useCond;

    public MFIFO(int size) {
        this.size = size;
        this.useCond = true;
        this.fifo = new TPatient[ size ];
        this.cArray = new Condition[size];
        this.rl = new ReentrantLock();
        this.cNotEmpty = rl.newCondition();
        this.cNotFull = rl.newCondition();
        this.bExit = new boolean[size];
        for(int i = 0; i< size; i++){
            this.bExit[i] = false;
            cArray[i] = rl.newCondition();
        }
    }

    public MFIFO(int size, boolean useCond) {
        this.size = size;
        this.useCond = useCond; 
        this.fifo = new TPatient[ size ];
        this.cArray = new Condition[size];
        this.rl = new ReentrantLock();
        this.cNotEmpty = rl.newCondition();
        this.cNotFull = rl.newCondition();
        this.bExit = new boolean[size];
        if (useCond) {
            for(int i = 0; i< size; i++){
                this.bExit[i] = false;
                this.cArray[i] = rl.newCondition();
            }
        }
    }

    @Override
    public void put(TPatient patient) {
        int idx;
        
        try {
            rl.lock();
            // wait while fifo is full
            while ( isFull() )
                cNotFull.await();

            idx = idxPut;
            fifo[ idx ] = patient;

            idxPut = (++idxPut) % size;

            // check if fifo was empty and send a signal if it was
            if ( isEmpty() )
                cNotEmpty.signal();

            // increase count
            count++;

            if (useCond) {
                // stay blocked on fifo since it has entered
                while ( !bExit[ idx ] )
                    cArray[ idx ].await();

                bExit[ idx ] = false;
            }
        } catch ( InterruptedException ex ) {}
        finally {
            rl.unlock();
        }
    }

    
    public void put(TPatient patient, ICallCentreHall_EntranceHall cch) {
        int idx;
        
        try {
            rl.lock();
            // wait while fifo is full
            while ( isFull() )
                cNotFull.await();

            idx = idxPut;
            fifo[ idx ] = patient;

            idxPut = (++idxPut) % size;

            // check if fifo was empty and send a signal if it was
            if ( isEmpty() )
                cNotEmpty.signal();

            // increase count
            count++;

            cch.notifyETHEntrance();
            // stay blocked on fifo since it has entered
            while ( !bExit[ idx ] )
                cArray[ idx ].await();

            bExit[ idx ] = false;
        } catch ( InterruptedException ex ) {}
        finally {
            rl.unlock();
        }
    }

    @Override
    public void get() {
        try{
            rl.lock();

            while ( isEmpty() )
                cNotEmpty.await();

            idxGet = idxGet % size;

            if ( isFull() )
                cNotFull.signal();

            count--;

            if (useCond) {
                bExit[ idxGet ] = true;
                cArray[ idxGet ].signal();
            }
            
            idxGet++;
        } catch( InterruptedException ex ) {}
        finally {
            rl.unlock();
        }
    }

    public void getPatient(TPatient patient) {
        try{
            rl.lock();

            while ( isEmpty() )
                cNotEmpty.await();

            int getId = 0;
            for (int i=0; i<fifo.length; i++) {
                if (fifo[i] !=null && fifo[i].getPatientId() == patient.getPatientId()) {
                    getId = i;
                }
            }

            if ( isFull() )
                cNotFull.signal();

            count --;

            if (useCond) {
                bExit[ getId ] = true;
                cArray[ getId ].signal();
            }
        } catch( InterruptedException ex ) {}
        finally {
            rl.unlock();
        }
    }

    public int getCount() {
        return count;
    }

    public TPatient getHead() {
        return fifo[ idxGet % size ];
    }

    private boolean isFull() {
        return count == size;
    }

    private boolean isEmpty() {
        return count == 0;
    }
}