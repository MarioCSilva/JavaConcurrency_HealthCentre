package HC.Entities;

import HC.Controller.IController_Doctor;
import HC.MedicalHall.IMedicalHall_Doctor;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TDoctor extends Thread {
    private int MDT;
    private int roomId;
    private final IMedicalHall_Doctor mdh;
    private final IController_Doctor controller;

    public TDoctor(IController_Doctor controller, int MDT, int roomId, IMedicalHall_Doctor mdh) {
        this.controller = controller;
        this.MDT = MDT;
        this.roomId = roomId;
        this.mdh = mdh;
    }

    /**
     * Method Responsible for killing the Thread
     */
    public void kill() {
        this.interrupt();
    }

    /** 
     * Method that doctor executes for doing the doctor appointment.
     * Thread sleeps for a random range defined by MDT.
     * If MDT is zero this does not apply.
     *
     * @param patient Patient to be checked by a Doctor
     * @throws InterruptedException
     */
    public void evaluate(TPatient patient) throws InterruptedException {
        controller.checkSuspend();
        // evaluation time
        if (MDT > 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(new Random().nextInt(MDT));
            } catch (InterruptedException e) {};
            controller.checkSuspend();
        }
    }

    @Override
    public void run() {
        try {
            mdh.work(this);
        } catch (InterruptedException e) {
            System.out.println(String.format("Doctor of room %d has died", roomId));
            Thread.currentThread().interrupt();
        }
    }


    /** 
     * @return int the index of the room where the Doctor is assigned.
     */
    public int getRoomId() {
        return roomId;
    }
}