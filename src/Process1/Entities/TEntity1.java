/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 * Thread representing one Entity
 */
package Process1.Entities;

import Process1.Monitor.ISharedRegion1_Entity1;
import Process1.Monitor.ISharedRegion2_Entity1;

/**
 * @author user
 */
public class TEntity1 extends Thread {

    private final int tE1Id;                    // Thread Id
    private final ISharedRegion1_Entity1 sr1;
    private final ISharedRegion2_Entity1 sr2;


    public TEntity1(int tE1Id, ISharedRegion1_Entity1 sr1, ISharedRegion2_Entity1 sr2) {
        this.tE1Id = tE1Id;
        this.sr1 = sr1;
        this.sr2 = sr2;
    }

    @Override
    public void run() {
        // state machine for the Entity1 (Thread)
        // for example
        sr1.a1();
        sr2.c2();
        sr2.c1();
        sr1.a2();
    }
}
