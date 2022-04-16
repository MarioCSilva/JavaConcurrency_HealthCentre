package HC.Communication;

import HC.Enumerates.MessageTopic;

import java.io.Serializable;

public class Message implements Serializable {
    private MessageTopic topic;
    private int numberOfAdults;
    private int numberOfChildren;
    private int nos;
    private int evt;
    private int mdt;
    private int pyt;
    private int ttm;

    public Message(MessageTopic topic) {
        this.topic = topic;
    }

    public Message(MessageTopic topic, int numberOfAdults, int numberOfChildren, int nos, int evt, int mdt, int pyt, int ttm) {
        this.topic = topic;
        this.numberOfAdults = numberOfAdults;
        this.numberOfChildren = numberOfChildren;
        this.nos = nos;
        this.evt = evt;
        this.mdt = mdt;
        this.pyt = pyt;
        this.ttm = ttm;
    }

    public void setTopic(MessageTopic topic) {
        this.topic = topic;
    }

    public MessageTopic getTopic() {
        return this.topic;
    }

    public int getNumberOfAdults() {
        return numberOfAdults;
    }

    public void setNumberOfAdults(int numberOfAdults) {
        this.numberOfAdults = numberOfAdults;
    }

    public int getNumberOfChildren() {
        return numberOfChildren;
    }

    public void setNumberOfChildren(int numberOfChildren) {
        this.numberOfChildren = numberOfChildren;
    }

    public int getNos() {
        return nos;
    }

    public void setNos(int nos) {
        this.nos = nos;
    }

    public int getEvt() {
        return evt;
    }

    public void setEvt(int evt) {
        this.evt = evt;
    }

    public int getMdt() {
        return mdt;
    }

    public void setMdt(int mdt) {
        this.mdt = mdt;
    }

    public int getPyt() {
        return pyt;
    }

    public void setPyt(int pyt) {
        this.pyt = pyt;
    }

    public int getTtm() {
        return ttm;
    }

    public void setTtm(int ttm) {
        this.ttm = ttm;
    }

}