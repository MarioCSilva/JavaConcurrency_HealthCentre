/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package PCFIFO;

import ITF.IProducer;

/**
 *
 * @author user
 */
public class TProducer extends Thread {

    private  final IProducer producer;
    public TProducer(IProducer producer) {
        this.producer = producer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(2000);
                int value = 1;
                System.out.println("Producer adding: " + value);
                producer.put(value);
            }
        } catch (InterruptedException e) {
        }
    }
}
