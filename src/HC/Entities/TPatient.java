package HC.Entities;

import HC.Enumerates.DoS;

import java.util.Random;

import HC.EntranceHall.IEntranceHall_Patient;
import HC.EvaluationHall.IEvaluationHall_Patient;
import HC.CallCentreHall.ICallCentreHall_Patient;
import HC.Logger.ILog_Patient;
import HC.WaitingHall.IWaitingHall_Patient;

public class TPatient extends Thread {
    private final int patientId;
    private final IEntranceHall_Patient mEntranceHall;
    private final IEvaluationHall_Patient mEvaluationHall;
    private final ICallCentreHall_Patient mCallCentreHall;
    private final IWaitingHall_Patient mWaitingHall;
    private final boolean isAdult;
    private Integer TN;
    private DoS dos;
    private int ttm;
    private final ILog_Patient logger;

    public TPatient(int patientId, int ttm, boolean isAdult, ILog_Patient logger, ICallCentreHall_Patient mCallCentreHall,
                    IEntranceHall_Patient mEntranceHall, IEvaluationHall_Patient mEvaluationHall, IWaitingHall_Patient mWaitingHall) {
        this.patientId = patientId;
        this.logger = logger;
        this.mEntranceHall = mEntranceHall;
        this.mEvaluationHall = mEvaluationHall;
        this.mCallCentreHall = mCallCentreHall;
        this.mWaitingHall = mWaitingHall;
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

        // enter the WTH
        this.mWaitingHall.enterHall(this);
    }

    public void log(String room) {
        logger.writePatient(this, room);
    }

    public void notifyEntrance(String hall) {
        mCallCentreHall.notifyEntrance(hall);
    }

    public void notifyExit(String hall) {
        mCallCentreHall.notifyExit(hall);
    }

    public void tSleep() {
        if (ttm > 0) {
            try {
                Thread.sleep(new Random().nextInt(ttm));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean getIsAdult(){
        return this.isAdult;
    }

    public int getPatientId() {
        return this.patientId;
    }

    public void setTN(int TN) {
        this.TN = TN;
    }

    public Integer getTN() {
        return this.TN;
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
        String tnStr = this.getTN() == null ? "" : String.valueOf(this.getTN());
        String dosStr = this.getDoS() == null ? "" : this.getDoS().toString().substring(0, 1);
        return String.format("%s0%s%s", adultStr, tnStr, dosStr);
    }
}
