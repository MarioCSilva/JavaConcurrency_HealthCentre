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

    
    /** 
     * @param patient a patient to be put on the Queue
     * @return the index occupied on the queue
     */
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


    
    /** 
     * @return the Queue Array
     */
    public TPatient[] getQueue() {
        return queue;
    }

    public void incCounter() {
        count++;
    }

    public void decCounter() {
        count--;
    }

    
    /** 
     * Check if queue is full
     * @return boolean
     */
    public boolean isFull() {
        return count == size;
    }

    
    /** 
     * Retrieves a patient from the queue, emptying the index queue assigned
     * @param idx the index of the patient to be retrieved
     */
    public void getPatientById(int idx) {
        queue[idx] = null;
    }
}



