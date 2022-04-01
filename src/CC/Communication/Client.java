package CC.Communication;

import HC.Communication.Message;
import HC.Enumerates.MessageTopic;

import java.io.*;
import java.net.*;

// Client class
public class Client extends Thread {
    private final String serverHostName;
    private final int serverPort;

    private Socket clientSocket;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    private SocketAddress serverAddress;

    public Client(String serverHostName, int serverPort) {
        this.serverHostName = serverHostName;
        this.serverPort = serverPort;
    }

    public void run() {
        try {
            clientSocket = new Socket();
            this.serverAddress = new InetSocketAddress(serverHostName, serverPort);
            clientSocket.connect(serverAddress);

            // get the output stream of the client
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            // get the input stream of the client
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleMsg() {
        Message msg;
        while (true) {
            try {
                if (!((msg = (Message) in.readObject()) != null)) {
                    // writing the received message from the server
                    System.out.printf(
                            " Sent from the server: %s%n",
                            msg.getTopic());
                    System.out.println(msg);
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMsg(Message msg) {
        try {
            out.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}