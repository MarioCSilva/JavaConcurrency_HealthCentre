package HC.ActiveEntities;

public class AEPatient extends Thread {
    private final int patientId;

    public AEPatient(int patientId) {
        this.patientId = patientId;
    }
}
