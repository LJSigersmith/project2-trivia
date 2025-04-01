import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread implements Runnable {
    private final Socket clientSocket;
    private final int clientId;
    private final UDPThread udpThread;
    private PrintWriter out;
    private BufferedReader in;
    private boolean running = true;

    public ClientThread(Socket socket, int clientId, UDPThread udpThread) {
        this.clientSocket = socket;
        this.clientId = clientId;
        this.udpThread = udpThread;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            sendMessage("WELCOME|" + clientId);

            while (running) {
                String input = in.readLine();
                if (input == null) {
                    break;
                }
                System.out.println("Client " + clientId + ": " + input);
                processClientMessage(input);
            }
        } catch (IOException e) {
            System.err.println("ClientThread " + clientId + " error: " + e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
            ServerMain.removeClient(clientId);
        }
    }

    private void processClientMessage(String message) {
        if (message.startsWith("DISCONNECT")) {
            running = false;
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public int getClientId() {
        return clientId;
    }
}