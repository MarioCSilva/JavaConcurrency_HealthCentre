package CC.Controller;

import HC.Communication.Message;
import HC.Enumerates.MessageTopic;

import java.io.*;
import java.net.*;

// Client class
public class Client {
    private final String serverHostName;
    private final int serverPort;

    private Socket clientSocket;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;

    public Client(String serverHostName, int serverPort) {
        this.serverHostName = serverHostName;
        this.serverPort = serverPort;
        SocketAddress serverAddress = new InetSocketAddress(serverHostName, serverPort);

        try {
            clientSocket = new Socket();
            clientSocket.connect(serverAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            // get the output stream of the client
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            // get the input stream of the client
            in = new ObjectInputStream(clientSocket.getInputStream());

            sendMsg(new Message(MessageTopic.START));

            Message msg;
            while ((msg = readMsg()) != null) {
                // writing the received message from
                // client
                System.out.printf(
                        " Sent from the server: %s\n",
                        msg.getTopic());
                System.out.println(msg);
            }
        } catch (IOException | ClassNotFoundException e) {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMsg(Message msg) throws IOException {
        out.writeObject(msg);
    }

    public Message readMsg() throws IOException, ClassNotFoundException {
        return (Message) in.readObject();
    }
}