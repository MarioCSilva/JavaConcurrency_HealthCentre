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

    public MFIFO(int size) {
        this.size = size;
        this.fifo = new int[ size ];
        this.rl = new ReentrantLock();
        this.cNotEmpty = rl.newCondition();
        this.cNotFull = rl.newCondition();
    }

    @Override
    public void put(int ETN) {
        try {
            rl.lock();
            while ( isFull() )
                cNotFull.await();
            fifo[ idxPut ] = ETN;
            idxPut = (++idxPut) % size;
            count++;
            cNotEmpty.signal();
        } catch ( InterruptedException ex ) {}
        finally {
            rl.unlock();
        }
    }

    @Override
    public int get() {
        try{
            rl.lock();
            try {
                while ( isEmpty() )
                    cNotEmpty.await();
            } catch( InterruptedException ex ) {}
            idxGet = idxGet % size;
            count --;
            cNotFull.signal();
            return fifo[idxGet++];
        }
        finally {
            rl.unlock();
        }
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