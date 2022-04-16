package HC.Entities;

import HC.Enumerates.DoS;
import HC.EvaluationHall.IEvaluationHall_Nurse;
import HC.Controller.IController_Nurse;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TNurse extends Thread {
    private final DoS dosValues[];
    private final int dosValuesLength;
    private final int EVT;
    private final int roomId;
    private final IEvaluationHall_Nurse evh;
    private final IController_Nurse controller;

    public TNurse(IController_Nurse controller, int EVT, int roomId, IEvaluationHall_Nurse evh) {
        this.controller = controller;
        this.EVT = EVT;
        this.roomId = roomId;
        this.dosValues = DoS.values();
        this.dosValuesLength = this.dosValues.length;
        this.evh = evh;
    }

    public void evaluate(TPatient patient) throws InterruptedException {
        controller.checkSuspend();

        // evaluation time
        if (EVT > 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(new Random().nextInt(EVT));
            } catch (InterruptedException e) {
            }
            ;
            controller.checkSuspend();
        }

        // decide the dos
        DoS dos = dosValues[new Random().nextInt(dosValuesLength)];

        // assign the dos to the patient
        patient.setDoS(dos);
    }

    @Override
    public void run() {
        try {
            evh.work(this);
        } catch (InterruptedException | IOException e) {
            System.out.println(String.format("Nurse has died", toString()));
            Thread.currentThread().interrupt();
        }
    }

    public void kill() {
        this.interrupt();
    }


    public int getRoomId() {
        return roomId;
    }
}