/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 * This Monitor contains several errors. Debug it.
 */
package PCFIFO;

import ITF.IConsumer;
import ITF.IProducer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author OMP
 */
public class MFIFO {
    
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
        fifo = new int[ size ];
        rl = new ReentrantLock();
        cNotEmpty = rl.newCondition();
        cNotFull = rl.newCondition();
    }

    @Override
    public void put( int value ) {
        try {
            rl.lock();
            while ( isFull() )
                cNotFull.await();
            fifo[ idxPut ] = value;
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

    private boolean isFull() {
        return count == size;
    }

    private boolean isEmpty() {
        return count == 0;
    }
}
