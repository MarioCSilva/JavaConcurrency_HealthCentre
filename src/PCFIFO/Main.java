/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 * This version is not complete and MFIFO has some errors.
 * Complete this versions and debug MFIFO
 */
package PCFIFO;

import ITF.IConsumer;
import ITF.IProducer;

/**
 *
 * @author user
 */
public class Main {
    public static void  main (String[] args){
        MFIFO mfifo = new MFIFO(5);
        TConsumer consumer = new TConsumer((IConsumer) mfifo);
        TProducer producer = new TProducer((IProducer) mfifo);
        TProducer producer2 = new TProducer((IProducer) mfifo);
        consumer.run();
        producer.run();
        producer2.run();
    }

}
