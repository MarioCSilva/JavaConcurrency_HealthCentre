package HC.FIFO;

import HC.Entities.TPatient;

public interface IFIFO {
    void put(TPatient patient);
    void get();
}