package HC.FIFO;

public interface IFIFO {
    public void put(int patientId);
    public int get();
}