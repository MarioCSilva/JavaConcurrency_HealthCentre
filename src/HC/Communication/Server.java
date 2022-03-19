package HC.Communication;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    private int port;
    private ServerSocket serverSocket;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;

    public Server(int port) {
        port = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);

            // running infinite loop for getting
            // client request
            while (true) {

                // socket object to receive incoming client
                // requests
                Socket client = serverSocket.accept();

                // Displaying that new client is connected
                // to server
                System.out.println("New client connected"
                        + client.getInetAddress()
                        .getHostAddress());

                // create a new thread object
                ClientHandler clientSock = new ClientHandler(client);

                // This thread will handle the client
                // separately
                new Thread(clientSock).start();
            }
        }
        catch(Exception e) {
            System.err.println(e);
        }
    }

    public void close() {
        try {
            serverSocket.close();
        }
        catch(Exception e) {
            System.err.println(e);
        }
    }
}

