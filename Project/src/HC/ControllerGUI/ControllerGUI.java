package HC.ControllerGUI;

import HC.Entities.TPatient;
import HC.Main.GUIHC;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class ControllerGUI {
    private final GUIHC gui;
    private final EventQueue queue;
    private HashMap<TPatient, DefaultListModel> patientList;

    public ControllerGUI(GUIHC gui) {
        this.gui = gui;
        this.queue = new EventQueue();
        this.patientList = new HashMap<>();
    }

    
    /** 
     * @param patient
     * @param room
     * @throws InterruptedException
     */
    public void movePatient(TPatient patient, String room) throws InterruptedException {
        DefaultListModel listToEnter = null;
        DefaultListModel listToRemove = null;
        switch (room) {
            case "ETH":
                listToEnter = (DefaultListModel) gui.getEthL().getModel();
                break;
            case "ET1":
                listToEnter = (DefaultListModel) gui.getETR1L().getModel();
                break;
            case "ET2":
                listToEnter = (DefaultListModel) gui.getETR2L().getModel();
                break;
            case "EVR1":
                listToEnter = (DefaultListModel) gui.getEVR1L().getModel();
                break;
            case "EVR2":
                listToEnter = (DefaultListModel) gui.getEVR2L().getModel();
                break;
            case "EVR3":
                listToEnter = (DefaultListModel) gui.getEVR3L().getModel();
                break;
            case "EVR4":
                listToEnter = (DefaultListModel) gui.getEVR4L().getModel();
                break;
            case "WTH":
                listToEnter = (DefaultListModel) gui.getWTHL().getModel();
                break;
            case "WTR1":
                listToEnter = (DefaultListModel) gui.getWTR1L().getModel();
                break;
            case "WTR2":
                listToEnter = (DefaultListModel) gui.getWTR2L().getModel();
                break;
            case "MDH":
                listToEnter = (DefaultListModel) gui.getMDWL().getModel();
                break;
            case "MDR1":
                listToEnter = (DefaultListModel) gui.getMDR1L().getModel();
                break;
            case "MDR2":
                listToEnter = (DefaultListModel) gui.getMDR2L().getModel();
                break;
            case "MDR3":
                listToEnter = (DefaultListModel) gui.getMDR3L().getModel();
                break;
            case "MDR4":
                listToEnter = (DefaultListModel) gui.getMDR4L().getModel();
                break;
            case "PYH":
                listToEnter = (DefaultListModel) gui.getPYHL().getModel();
                break;
            case "OUT":
                listToEnter = (DefaultListModel) gui.getOUTL().getModel();
                break;
            default:
                throw new IllegalArgumentException(String.format("Room not supported: %s" + room));
        }
        listToRemove = patientList.put(patient, listToEnter);
        movePatientGUI(listToRemove, listToEnter, patient);
    }

    
    /** 
     * @param listToRemove
     * @param listToEnter
     * @param patient
     * @throws InterruptedException
     */
    public void movePatientGUI(DefaultListModel listToRemove, DefaultListModel listToEnter, TPatient patient) throws InterruptedException {
        try {
            queue.invokeAndWait(() -> {
                if (listToRemove != null)
                    listToRemove.removeElement(patient);
                listToEnter.addElement(patient);
            });
        } catch (Exception e) {
            throw new InterruptedException();
        }
    }

    
    /** 
     * @throws InterruptedException
     */
    public void clearGUI() throws InterruptedException {
        this.patientList = new HashMap<>();

        try {
            queue.invokeAndWait(() -> {
                ((DefaultListModel) gui.getEthL().getModel()).removeAllElements();
                ((DefaultListModel) gui.getETR1L().getModel()).removeAllElements();
                ((DefaultListModel) gui.getETR2L().getModel()).removeAllElements();
                ((DefaultListModel) gui.getEVR1L().getModel()).removeAllElements();
                ((DefaultListModel) gui.getEVR2L().getModel()).removeAllElements();
                ((DefaultListModel) gui.getEVR3L().getModel()).removeAllElements();
                ((DefaultListModel) gui.getEVR4L().getModel()).removeAllElements();
                ((DefaultListModel) gui.getWTHL().getModel()).removeAllElements();
                ((DefaultListModel) gui.getWTR1L().getModel()).removeAllElements();
                ((DefaultListModel) gui.getWTR2L().getModel()).removeAllElements();
                ((DefaultListModel) gui.getMDWL().getModel()).removeAllElements();
                ((DefaultListModel) gui.getMDR1L().getModel()).removeAllElements();
                ((DefaultListModel) gui.getMDR2L().getModel()).removeAllElements();
                ((DefaultListModel) gui.getMDR3L().getModel()).removeAllElements();
                ((DefaultListModel) gui.getMDR4L().getModel()).removeAllElements();
                ((DefaultListModel) gui.getPYHL().getModel()).removeAllElements();
                ((DefaultListModel) gui.getOUTL().getModel()).removeAllElements();
            });
        } catch (Exception e) {
            throw new InterruptedException();
        }
    }
}
