package HC.Entities;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import HC.Enumerates.DoS;
import HC.EvaluationHall.IEvaluationHall_Nurse;

public class TNurse extends Thread {
    private final DoS dosValues[];
    private final int dosValuesLength;
    private final int EVT;
    private final int roomId;
    private final IEvaluationHall_Nurse evh;

    public TNurse(int EVT, int roomId, IEvaluationHall_Nurse evh) {
        this.EVT = EVT;
        this.roomId = roomId;
        this.dosValues = DoS.values();
        this.dosValuesLength = this.dosValues.length;
        this.evh = evh;
    }

    public void evaluate(TPatient patient) {
        // evaluation time
        if (EVT > 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(new Random().nextInt(EVT));
            } catch (InterruptedException e) {};
        }
        // decide the dos
        DoS dos = dosValues[new Random().nextInt(dosValuesLength)];

        // assign the dos to the patient
        patient.setDoS(dos);
    }

    @Override
    public void run() {
        evh.work(this);
    }

    public int getRoomId() {
        return roomId;
    }
}