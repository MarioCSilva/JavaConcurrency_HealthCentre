package HC.Entities;

import HC.CallCentreHall.ICallCentreHall_Patient;
import HC.EntranceHall.IEntranceHall_Patient;
import HC.Enumerates.DoS;
import HC.EvaluationHall.IEvaluationHall_Patient;
import HC.Controller.IController_Patient;
import HC.MedicalHall.IMedicalHall_Patient;
import HC.PaymentHall.IPaymentHall_Patient;
import HC.WaitingHall.IWaitingHall_Patient;

import java.io.IOException;
import java.util.Random;

public class TPatient extends Thread {
    private final int patientId;
    private final IEntranceHall_Patient mEntranceHall;
    private final IEvaluationHall_Patient mEvaluationHall;
    private final ICallCentreHall_Patient mCallCentreHall;
    private final IWaitingHall_Patient mWaitingHall;
    private final IMedicalHall_Patient mMedicalHall;
    private final IPaymentHall_Patient mPaymentHall;
    private final boolean isAdult;
    private final String patientType;
    private Integer TN;
    private DoS dos;
    private int ttm;
    private final IController_Patient controller;

    public TPatient(int patientId, int ttm, boolean isAdult, IController_Patient controller, ICallCentreHall_Patient mCallCentreHall,
                    IEntranceHall_Patient mEntranceHall, IEvaluationHall_Patient mEvaluationHall, IWaitingHall_Patient mWaitingHall,
                    IMedicalHall_Patient mMedicalHall, IPaymentHall_Patient mPaymentHall) {
        this.patientId = patientId;
        this.controller = controller;
        this.mEntranceHall = mEntranceHall;
        this.mEvaluationHall = mEvaluationHall;
        this.mCallCentreHall = mCallCentreHall;
        this.mWaitingHall = mWaitingHall;
        this.mMedicalHall = mMedicalHall;
        this.mPaymentHall = mPaymentHall;
        this.isAdult = isAdult;
        this.patientType = isAdult ? "A" : "C";
        this.ttm = ttm;
    }

    public void kill() {
        this.interrupt();
    }

    @Override
    public void run() {
        try {
            tSleep();

            // enter the ETH
            this.mEntranceHall.enterHall(this);

            tSleep();

            // enter the EVH
            this.mEvaluationHall.enterHall(this);

            tSleep();

            // enter the WTH
            this.mWaitingHall.enterHall(this);

            tSleep();

            // enter the MDH
            this.mMedicalHall.enterHall(this);

            tSleep();

            // enter the MDH
            this.mPaymentHall.enterHall(this);

        } catch (InterruptedException | IOException e) {
            System.out.println(String.format("Patient %s has died", toString()));
            Thread.currentThread().interrupt();
        }
    }

    public void log(String room) throws InterruptedException, IOException {
        controller.writePatientMovement(this, room);
    }

    public void notifyEntrance(String hall) {
        mCallCentreHall.notifyEntrance(this, hall);
    }

    public void notifyExit(String hall) {
        mCallCentreHall.notifyExit(this, hall);
    }

    public void tSleep() throws InterruptedException {
        checkSuspend();

        if (ttm > 0) {
            Thread.sleep(new Random().nextInt(ttm));
            checkSuspend();
        }
    }

    public void checkSuspend() throws InterruptedException {
        controller.checkSuspend();
    }

    public boolean getIsAdult() {
        return this.isAdult;
    }

    public String getPatientType() {
        return this.patientType;
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
        String dosStr = this.getDoS() == null ? "" : this.getDoS().toString().substring(0, 1);
        return String.format("%s%02d%s", adultStr, this.getTN(), dosStr);
    }
}
