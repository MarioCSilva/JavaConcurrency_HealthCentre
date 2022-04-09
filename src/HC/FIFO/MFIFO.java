package HC.FIFO;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MFIFO implements IFIFO {
    private int idxPut = 0;
    private int idxGet = 0;
    private int count = 0;
    
    private final int fifo[];
    private final int size;
    private final ReentrantLock rl;
    private final Condition cNotFull;
    private final Condition cNotEmpty;
    private final Condition cArray[];
    private final boolean[] bExit;

    public MFIFO(int size) {
        this.size = size;
        this.fifo = new int[ size ];
        this.cArray = new Condition[size];
        this.rl = new ReentrantLock();
        this.cNotEmpty = rl.newCondition();
        this.cNotFull = rl.newCondition();
        this.bExit = new boolean[size];
        for(int i = 0; i< size; i++){
            bExit[i] = false;
            cArray[i] = rl.newCondition();
        }
    }

    @Override
    public void put(int ETN) {
        try {
            rl.lock();
            // wait while fifo is full
            while ( isFull() )
                cNotFull.await();

            fifo[ idxPut ] = ETN;
            idxPut = (++idxPut) % size;

            // check if fifo was empty and send a signal if it was
            if ( isEmpty() )
                cNotEmpty.signal();

            // increase count
            count++;

            // stay blocked on fifo since it has entered
            while ( !bExit[ idxPut ] )
                cArray[ idxPut ].await();

            bExit[ idxPut ] = false;
        } catch ( InterruptedException ex ) {}
        finally {
            rl.unlock();
        }
    }

    @Override
    public int get() {
        try{
            rl.lock();

            while ( isEmpty() )
                cNotEmpty.await();

            idxGet = idxGet % size;

            if ( isFull() )
                cNotFull.signal();

            count --;

            bExit[ idxGet ] = true;
            
            cArray[ idxGet ].signal();

            return fifo[idxGet++];
        } catch( InterruptedException ex ) {}
        finally {
            rl.unlock();
        }
        return -1;
    }

    public int getHead() {
        return fifo[ idxGet % size ];
    }

    private boolean isFull() {
        return count == size;
    }

    private boolean isEmpty() {
        return count == 0;
    }
}