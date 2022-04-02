/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 * Thread representing one Entity
 */
package Process1.Entities;

import Process1.Monitor.ISharedRegion1_Entity2;
import Process1.Monitor.ISharedRegion2_Entity2;

/**
 *
 * @author user
 */
public class TEntity2 extends Thread {
    
    private final int tE1Id;
    private final ISharedRegion1_Entity2 sr1;
    private final ISharedRegion2_Entity2 sr2;
    
    public TEntity2(int tE1Id, ISharedRegion1_Entity2 sr1, ISharedRegion2_Entity2 sr2 ) {
        this.tE1Id = tE1Id;
        this.sr1 = sr1;
        this.sr2 = sr2;
    }
    @Override
    public void run() {
        // state machine for the Entity2 (Thread)
        // for example
        sr2.d1();
        sr2.d2();
        sr1.b1();
        sr1.b2();
    }
}
