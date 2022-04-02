/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package PCFIFO;

import ITF.IConsumer;

/**
 *
 * @author user
 */
public class TConsumer extends Thread {

   private  final IConsumer consumer;
    public TConsumer(IConsumer consumer) {
        this.consumer = consumer;
    }
    @Override
    public void run() {
        while (true) {
            try {

                System.out.println("Consumer: waiting");
                System.out.println("Consumer: " + consumer.get());
            } catch (Exception e) { }
        }
    }
}
