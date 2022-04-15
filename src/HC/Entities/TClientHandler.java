package HC.Entities;

import HC.CallCentreHall.*;
import HC.Communication.Message;
import HC.EntranceHall.IEntranceHall_CallCentre;
import HC.EntranceHall.IEntranceHall_Patient;
import HC.EntranceHall.MEntranceHall;
import HC.Enumerates.MessageTopic;
import HC.EvaluationHall.IEvaluationHall_Nurse;
import HC.EvaluationHall.IEvaluationHall_Patient;
import HC.EvaluationHall.MEvaluationHall;
import HC.FIFO.MFIFO;
import HC.Logger.*;
import HC.MedicalHall.IMedicalHall_CallCentre;
import HC.MedicalHall.IMedicalHall_Doctor;
import HC.MedicalHall.IMedicalHall_Patient;
import HC.MedicalHall.MMedicalHall;
import HC.WaitingHall.IWaitingHall_CallCentre;
import HC.WaitingHall.IWaitingHall_Patient;
import HC.WaitingHall.MWaitingHall;

import java.io.*;
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
    private TAdultPatient adultPatients[];
    private TChildPatient childPatients[];
    private TCallCentre cc;
    private ILog_ClientHandler clientLogger;
    private MLog defaultLogger;
    

    public TClientHandler(Socket socket, MLog logger) {
        this.clientSocket = socket;
        this.clientLogger = (ILog_ClientHandler) logger;
        this.defaultLogger = logger;
    }

    public void startSimulation(Message msg) {
        clientLogger.write(String.format("NoA: %d, NoC: %d, NoS: %d", msg.getNumberOfAdults(), msg.getNumberOfChildren(), msg.getNos()));
        clientLogger.writeHeaders();
        clientLogger.writeState("INIT");

        // initiate monitors
        cch = new MCallCentreHall(msg.getNos());
        eth = new MEntranceHall(msg.getNos());
        meh = new MEvaluationHall();
        wth = new MWaitingHall(msg.getNos());
        mdh = new MMedicalHall();


        // initiate entities
        cc = new TCallCentre((IEntranceHall_CallCentre) eth, (ICallCentreHall_CallCentre) cch,
                (IWaitingHall_CallCentre) wth, (IMedicalHall_CallCentre) mdh);
        clientLogger.writeState("RUN");
        cc.start();
        for (int i=0; i<4; i++){
            TDoctor doctor = new TDoctor(msg.getMdt(), i, (IMedicalHall_Doctor) mdh);
            doctor.start();
            TNurse nurse = new TNurse(msg.getEvt(), i, (IEvaluationHall_Nurse) meh);
            nurse.start();
        }
        adultPatients = new TAdultPatient[msg.getNumberOfAdults()];
        childPatients = new TChildPatient[msg.getNumberOfChildren()];
        int patientId = 0;
        for(int i=0; i<msg.getNumberOfAdults(); i++) {
            adultPatients[i] = new TAdultPatient(patientId++, msg.getTtm(), (ILog_Patient) defaultLogger,
                    (ICallCentreHall_Patient) cch, (IEntranceHall_Patient) eth, (IEvaluationHall_Patient) meh,
                    (IWaitingHall_Patient) wth, (IMedicalHall_Patient) mdh);
            adultPatients[i].start();
        }
        for (int i=0; i<msg.getNumberOfChildren(); i++) {
            childPatients[i] = new TChildPatient(patientId++, msg.getTtm(), (ILog_Patient) defaultLogger,
                    (ICallCentreHall_Patient) cch, (IEntranceHall_Patient) eth, (IEvaluationHall_Patient) meh,
                    (IWaitingHall_Patient) wth, (IMedicalHall_Patient) mdh);
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
            for (;;) {
                try {
                    msg = (Message) in.readObject();
                } catch (Exception e) {
                    break;
                }

                System.out.printf(
                        " Sent from the client: %s%n",
                        msg.getTopic());
                MessageTopic topic = msg.getTopic();
                if (topic == MessageTopic.START) {
                    System.out.println("Starting simulation");
                    // start a new simulation
                    startSimulation(msg);
                    // Patients are created and go to the Entrance Hall
                } else if (topic == MessageTopic.SUSPEND) {
                    // to suspend the running simulation
                    // Patients, Call Center and Cashier suspend their activity
                } else if (topic == MessageTopic.RESUME) {
                    // to resume the running simulation
                    // Patients, Call Centre and Cashier resume their normal activity
                } else if (topic == MessageTopic.STOP) {
                    // to stop the simulation
                    // simulation evolves to its initial state and all Patients die
                } else if (topic == MessageTopic.END) {
                    // to end the simulation
                    // the two processes end
                } else if (topic == MessageTopic.MODE) {
                    // option = {manual, auto}
                } else {
                    break;
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
