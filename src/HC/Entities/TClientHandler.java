package HC.Entities;

import HC.CallCentreHall.ICallCentreHall_CallCentre;
import HC.CallCentreHall.ICallCentreHall_Patient;
import HC.CallCentreHall.MCallCentreHall;
import HC.Communication.Message;
import HC.EntranceHall.IEntranceHall_CallCentre;
import HC.EntranceHall.IEntranceHall_Patient;
import HC.EntranceHall.MEntranceHall;
import HC.Enumerates.MessageTopic;
import HC.EvaluationHall.IEvaluationHall_Nurse;
import HC.EvaluationHall.IEvaluationHall_Patient;
import HC.EvaluationHall.MEvaluationHall;
import HC.Controller.*;
import HC.MedicalHall.IMedicalHall_CallCentre;
import HC.MedicalHall.IMedicalHall_Doctor;
import HC.MedicalHall.IMedicalHall_Patient;
import HC.MedicalHall.MMedicalHall;
import HC.PaymentHall.IPaymentHall_Cashier;
import HC.PaymentHall.IPaymentHall_Patient;
import HC.PaymentHall.MPaymentHall;
import HC.WaitingHall.IWaitingHall_CallCentre;
import HC.WaitingHall.IWaitingHall_Patient;
import HC.WaitingHall.MWaitingHall;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TClientHandler implements Runnable {
    private final Socket clientSocket;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    private MCallCentreHall cch;
    private MEntranceHall eth;
    private MEvaluationHall meh;
    private MWaitingHall wth;
    private MMedicalHall mdh;
    private MPaymentHall pyh;
    private TAdultPatient adultPatients[];
    private TChildPatient childPatients[];
    private TNurse nurses[];
    private TDoctor doctors[];

    private TCashier cashier;
    private TCallCentre cc;

    private IController_ClientHandler clientController;
    private MController defaultController;


    public TClientHandler(Socket socket, MController controller) {
        this.clientSocket = socket;
        this.clientController = (IController_ClientHandler) controller;
        this.defaultController = controller;
    }


    public void startSimulation(Message msg) throws IOException {
        // initiate monitors
        cch = new MCallCentreHall(msg.getNos(), (IController_CallCentreHall) defaultController);

        eth = new MEntranceHall(msg.getNos());
        meh = new MEvaluationHall();
        wth = new MWaitingHall(msg.getNos());
        mdh = new MMedicalHall();
        pyh = new MPaymentHall(msg.getNos());

        // initiate entities
        cc = new TCallCentre((IController_CallCentre) defaultController, (IEntranceHall_CallCentre) eth, (ICallCentreHall_CallCentre) cch,
                (IWaitingHall_CallCentre) wth, (IMedicalHall_CallCentre) mdh);
        cc.start();
        cashier = new TCashier((IController_Cashier) defaultController, msg.getPyt(), (IPaymentHall_Cashier) pyh);
        cashier.start();
        doctors = new TDoctor[4];
        nurses = new TNurse[4];
        for (int i = 0; i < 4; i++) {
            doctors[i] = new TDoctor((IController_Doctor) defaultController, msg.getMdt(), i, (IMedicalHall_Doctor) mdh);
            doctors[i].start();
            nurses[i] = new TNurse((IController_Nurse) defaultController, msg.getEvt(), i, (IEvaluationHall_Nurse) meh);
            nurses[i].start();
        }
        adultPatients = new TAdultPatient[msg.getNumberOfAdults()];
        childPatients = new TChildPatient[msg.getNumberOfChildren()];
        int patientId = 0;
        for (int i = 0; i < msg.getNumberOfAdults(); i++) {
            adultPatients[i] = new TAdultPatient(patientId++, msg.getTtm(), (IController_Patient) defaultController,
                    (ICallCentreHall_Patient) cch, (IEntranceHall_Patient) eth, (IEvaluationHall_Patient) meh,
                    (IWaitingHall_Patient) wth, (IMedicalHall_Patient) mdh, (IPaymentHall_Patient) pyh);
            adultPatients[i].start();
        }
        for (int i = 0; i < msg.getNumberOfChildren(); i++) {
            childPatients[i] = new TChildPatient(patientId++, msg.getTtm(), (IController_Patient) defaultController,
                    (ICallCentreHall_Patient) cch, (IEntranceHall_Patient) eth, (IEvaluationHall_Patient) meh,
                    (IWaitingHall_Patient) wth, (IMedicalHall_Patient) mdh, (IPaymentHall_Patient) pyh);
            childPatients[i].start();
        }
    }

    public void run() {
        try {
            // get the output stream of client
            out = new ObjectOutputStream(clientSocket.getOutputStream());

            // get the input stream of client
            in = new ObjectInputStream(clientSocket.getInputStream());
            Message msg;
            label:
            for (; ; ) {
                try {
                    msg = (Message) in.readObject();
                } catch (Exception e) {
                    break;
                }

                MessageTopic topic = msg.getTopic();
                switch (topic) {
                    case START:
                        System.out.println("Starting simulation");
                        // start a new simulation
                        clientController.startSimulation(msg);
                        startSimulation(msg);
                        // Patients are created and go to the Entrance Hall
                        break;
                    case SUSPEND:
                        // to suspend the running simulation
                        System.out.println("Suspending simulation");
                        clientController.suspendSimulation();
                        break;
                    case RESUME:
                        System.out.println("Resuming simulation");
                        // to resume the running simulation
                        clientController.resumeSimulation();
                        break;
                    case STOP:
                        System.out.println("Stopping simulation");
                        // to stop the simulation
                        // simulation evolves to its initial state and all Patients die
                        int i;
                        for (i = 0; i < adultPatients.length; i++)
                            adultPatients[i].interrupt();
                        for (i = 0; i < childPatients.length; i++)
                            childPatients[i].kill();
                        for (i = 0; i < doctors.length; i++)
                            doctors[i].kill();
                        for (i = 0; i < nurses.length; i++)
                            nurses[i].kill();
                        cashier.kill();
                        cc.kill();
                        Thread.sleep(100);
                        clientController.stopSimulation();
                        break;
                    case END:
                        System.out.println("Ending Simulation");
                        // to end the simulation
                        clientController.endSimulation();
                        break;
                    case MODE:
                        System.out.println("Changing Operating Mode");
                        // option = {manual, auto}
                        clientController.changeOperatingMode();
                        break;
                    case OUTPATIENT:
                        System.out.println("Moving a Patient");
                        // move patient when manual mode is on
                        clientController.movePatient();
                        break;
                    default:
                        break label;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                    clientSocket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
