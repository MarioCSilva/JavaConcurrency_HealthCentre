package HC.Controller;

import HC.Entities.TPatient;
import HC.Logger.MLog;
import HC.Main.GUIHC;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public class Controller {
    private final GUIHC gui;
    private final EventQueue queue;

    public Controller(GUIHC gui) {
        this.gui = gui;
        this.queue = new EventQueue();
    }

    public void addPatient(TPatient patient, String room) {
        switch (room) {
            case "ETH":
                addPatientGUI((DefaultListModel) gui.getEthL().getModel(), patient);
                break;
            case "ET1":
                break;
            case "ET2":
                break;
            case "EVR1":
                break;
            case "EVR2":
                break;
            case "EVR3":
                break;
            case "EVR4":
                break;
            case "WTH":
                break;
            case "WTR1":
                break;
            case "WTR2":
                break;
            case "MDH":
                break;
            case "MDR1":
                break;
            case "MDR2":
                break;
            case "MDR3":
                break;
            case "MDR4":
                break;
            case "PYH":
                break;
            case "OUT":
                break;
            default:
                throw new IllegalArgumentException(String.format("Room not supported: %s" + room));
        };
    }

    public void addPatientGUI(DefaultListModel list, TPatient patient) {
        try {
            queue.invokeAndWait(() -> {
                list.addElement(patient.toString());
            });
        } catch ( Exception e ) {
            throw new RuntimeException(e);
        }
    }

//    public void removePatient(TPatient patient, String room) {
//        switch (room) {
//            case "ETH":
//                (Seats) eth.getComponent(0);
//                break;
//            case "ET1":
//                (Seats) etr1.getComponent(0);
//                break;
//            case "ET2":
//                (Seats) etr2.getComponent(0);
//                break;
//            case "EVR1":
//                (Seats) evr1.getComponent(0);
//                break;
//            case "EVR2":
//                (Seats) evr2.getComponent(0);
//                break;
//            case "EVR3":
//                (Seats) evr3.getComponent(0);
//                break;
//            case "EVR4":
//                (Seats) evr4.getComponent(0);
//                break;
//            case "WTH":
//                (Seats) wth.getComponent(0);
//                break;
//            case "WTR1":
//                (Seats) wtr1.getComponent(0);
//                break;
//            case "WTR2":
//                (Seats) wtr2.getComponent(0);
//                break;
//            case "MDH":
//                (Seats) mdw.getComponent(0);
//                break;
//            case "MDR1":
//                (Seats) mdr1.getComponent(0);
//                break;
//            case "MDR2":
//                (Seats) mdr2.getComponent(0);
//                break;
//            case "MDR3":
//                (Seats) mdr3.getComponent(0);
//                break;
//            case "MDR4":
//                (Seats) mdr4.getComponent(0);
//                break;
//            case "PYH":
//                (Seats) pyh.getComponent(0);
//                break;
//            case "CSH":
//                (Seats) cashier.getComponent(0);
//                break;
//            case "OUT":
//                (Seats) out.getComponent(0);
//                break;
//            default:
//                throw new IllegalArgumentException("Room unrecognized: " + room);
//                break;
//        };
//    }
}
