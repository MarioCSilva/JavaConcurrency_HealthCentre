package HC.FIFO;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

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

    // @Override
    // public void put(TPatient patient) {
    //     int idx;
        
    //     try {
    //         rl.lock();
    //         // wait while fifo is full
    //         while ( isFull() )
    //             cNotFull.await();

    //         idx = idxPut;
    //         fifo[ idx ] = patient;

    //         idxPut = (++idxPut) % size;

    //         // check if fifo was empty and send a signal if it was
    //         if ( isEmpty() )
    //             cNotEmpty.signal();

    //         // increase count
    //         count++;

    //         if (useCond) {
    //             // stay blocked on fifo since it has entered
    //             while ( !bExit[ idx ] )
    //                 cArray[ idx ].await();

    //             bExit[ idx ] = false;
    //         }
    //     } catch ( InterruptedException ex ) {}
    //     finally {
    //         rl.unlock();
    //     }
    // }

    public void put(TPatient patient, int idx) {
        fifo[ idx ] = patient;
    }

    public int put(TPatient patient) {
        int i = 0;
        for (i=0; i<size; i++) {
            if (fifo[i] ==null) {
                fifo[i] = patient;
                break;
            }
        }
        return i;
        // int idx = idxPut;
        // fifo[ idx ] = patient;
        // idxPut = (++idxPut) % size;
        // return idx;
    }
    
    @Override
    public int get() {
        // while ( isEmpty() ) {
        //     System.out.println(String.format("%d PRESOSOSDAODOASODSAODOASDOSAODOASDSA", count));
        //     cNotEmpty.await();
        // }

        // if ( isFull() )
        //     cNotFull.signal();

        // count--;

        // if (useCond) {
        //     bExit[ idxGet ] = true;
        //     cArray[ idxGet ].signal();
        // }

        int idx = idxGet;

        fifo [ idx ] = null;

        idxGet = ++idxGet % size;

        return idx;
    }

    public int getPatient(TPatient patient) {
        int idx = 0;

        for (int i=0; i<size; i++) {
            if (fifo[i] !=null && fifo[i].getPatientId() == patient.getPatientId()) {
                idx = i;
                break;
            }
        }

        fifo [ idx ] = null;

        return idx;
    }

    public TPatient[] getFIFO() {
        return fifo;
    }

    public void incCounter() {
        count++;
    }
    
    public void decCounter(){
        count--;
    }
    public int getCount() {
        return count;
    }

    public TPatient getHead() {
        return fifo[idxGet];
    }

    public boolean isFull() {
        return count == size;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public void getPatientById(int idx) {
        fifo [ idx ] = null;
    }
}



