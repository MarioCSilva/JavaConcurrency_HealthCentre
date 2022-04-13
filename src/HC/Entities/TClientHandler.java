package HC.Entities;

import HC.CallCentreHall.ICallCentreHall_CallCentre;
import HC.CallCentreHall.ICallCentreHall_EntranceHall;
import HC.CallCentreHall.ICallCentreHall_EvaluationHall;
import HC.CallCentreHall.MCallCentreHall;
import HC.Communication.Message;
import HC.EntranceHall.IEntranceHall_CallCenter;
import HC.EntranceHall.IEntranceHall_Patient;
import HC.EntranceHall.MEntranceHall;
import HC.Enumerates.MessageTopic;
import HC.EvaluationHall.IEvaluationHall_Patient;
import HC.EvaluationHall.MEvaluationHall;
import HC.Logger.ILog_ClientHandler;
import HC.Logger.ILog_EntranceHall;
import HC.Logger.ILog_EvaluationHall;
import HC.Logger.MLog;

import java.io.*;
import java.net.Socket;

public class TClientHandler implements Runnable {
    private final Socket clientSocket;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    private MCallCentreHall cch;
    private MEntranceHall eth;
    private MEvaluationHall meh;
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
        eth = new MEntranceHall((ILog_EntranceHall) defaultLogger, (int) msg.getNos()/2, (ICallCentreHall_EntranceHall) cch);
        meh = new MEvaluationHall((ILog_EvaluationHall) defaultLogger, msg.getNos(), msg.getEvt(), (ICallCentreHall_EvaluationHall) cch);
        // initiate entities
        cc = new TCallCentre((IEntranceHall_CallCenter) eth, (ICallCentreHall_CallCentre) cch);

        clientLogger.writeState("RUN");
        cc.start();
        adultPatients = new TAdultPatient[msg.getNumberOfAdults()];
        childPatients = new TChildPatient[msg.getNumberOfChildren()];
        int patientId = 0;
        for(int i=0; i<msg.getNumberOfAdults(); i++) {
            adultPatients[i] = new TAdultPatient(patientId++, msg.getTtm(), (IEntranceHall_Patient) eth, (IEvaluationHall_Patient) meh);
            adultPatients[i].start();
        }
        for (int i=0; i<msg.getNumberOfChildren(); i++) {
            childPatients[i] = new TChildPatient(patientId++, msg.getTtm(), (IEntranceHall_Patient) eth, (IEvaluationHall_Patient) meh);
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
