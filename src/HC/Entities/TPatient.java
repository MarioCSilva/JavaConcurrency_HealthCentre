package HC.Entities;

import HC.Enumerates.DoS;

import java.util.Random;

import HC.EntranceHall.IEntranceHall_Patient;
import HC.EvaluationHall.IEvaluationHall_Patient;

public class TPatient extends Thread {
    private final int patientId;
    private final IEntranceHall_Patient mEntranceHall;
    private final IEvaluationHall_Patient mEvaluationHall;
    private final boolean isAdult;
    private Integer ETN;
    private DoS dos;
    private int ttm;

    public TPatient(int patientId, int ttm, boolean isAdult, IEntranceHall_Patient mEntranceHall, IEvaluationHall_Patient mEvaluationHall) {
        this.patientId = patientId;
        this.mEntranceHall = mEntranceHall;
        this.mEvaluationHall = mEvaluationHall;
        this.isAdult = isAdult;
        this.ttm = ttm;
    }

    @Override
    public void run() {

        tSleep();

        // enter the ETH
        this.mEntranceHall.enterHall(this);
        System.out.println(String.format("paciente - sai do eth %s", this.toString()));
        
        tSleep();

        // enter the EVH
        this.mEvaluationHall.enterHall(this);

        tSleep();
    }

    public void tSleep() {
        try {
            Thread.sleep(new Random().nextInt(ttm));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean getIsAdult(){
        return this.isAdult;
    }

    public int getPatientId() {
        return this.patientId;
    }

    public void setETN(int ETN) {
        this.ETN = ETN;
    }

    public Integer getETN() {
        return this.ETN;
    }

    public void setDoS(DoS dos) {
        this.dos = dos;
    }

    public DoS getDoS() {
        return this.dos;
    }

    @Override
    public String toString() {
        String adultStr = this.isAdult ? "A" : "C"; 
        String etnStr = this.getETN() == null ? "" : String.valueOf(this.getETN());
        String dosStr = this.getDoS() == null ? "" : this.getDoS().toString().substring(0, 1);
        return String.format("%s%s0%d%s", etnStr, adultStr, patientId, dosStr);
    }
}
