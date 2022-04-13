package HC.Entities;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import HC.Enumerates.DoS;

public class Nurse {
    private final DoS dosValues[];
    private final int dosValuesLength;
    private int EVT;

    public Nurse(int EVT) {
        this.EVT = EVT;
        this.dosValues = DoS.values();
        this.dosValuesLength = this.dosValues.length;
    }

    public void evaluate(TPatient patient) {
        // evaluation time
        try {
            TimeUnit.MILLISECONDS.sleep(new Random().nextInt(EVT));
        } catch (InterruptedException e) {};

        // decide the dos
        DoS dos = dosValues[new Random().nextInt(dosValuesLength)];

        // assign the dos to the patient
        patient.setDoS(dos);
    }
}