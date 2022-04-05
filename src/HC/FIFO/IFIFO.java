package HC.FIFO;

public interface IFIFO {
    void put(int patientId);
    int get();
}