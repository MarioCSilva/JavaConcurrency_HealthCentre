package HC.Entities;
import HC.MedicalHall.IMedicalHall_Doctor;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TDoctor extends Thread {

    private int MDT;
    private int roomId;
    private final IMedicalHall_Doctor mdh;

    public TDoctor(int MDT, int roomId, IMedicalHall_Doctor mdh){
        this.MDT = MDT;
        this.roomId = roomId;
        this.mdh = mdh;
    }
    
    public void evaluate(TPatient patient) {
        // evaluation time
        if (MDT > 0) {
            try {
                int appTime = new Random().nextInt(MDT);
                patient.setAppointmentTime(appTime);
                TimeUnit.MILLISECONDS.sleep(appTime);
            } catch (InterruptedException e) {};
        }
    }

    @Override
    public void run() {
        mdh.work(this);
    }

    public int getRoomId() {
        return roomId;
    }
}