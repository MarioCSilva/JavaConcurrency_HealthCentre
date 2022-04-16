package HC.Queue;

import HC.Entities.TPatient;

public class PriorityQueue {
    private int count = 0;
    private final TPatient queue[];
    private final int size;

    public PriorityQueue(int size) {
        this.size = size;
        this.queue = new TPatient[size];
    }

    public int put(TPatient patient) {
        int i;
        for (i = 0; i < size; i++) {
            if (queue[i] == null) {
                queue[i] = patient;
                break;
            }
        }
        return i;
    }


    public TPatient[] getQueue() {
        return queue;
    }

    public void incCounter() {
        count++;
    }

    public void decCounter() {
        count--;
    }

    public boolean isFull() {
        return count == size;
    }

    public void getPatientById(int idx) {
        queue[idx] = null;
    }
}



