import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.function.Consumer;

public class NetworkHandler {
    private Socket tcpSocket;
    private DatagramSocket udpSocket;
    private PrintWriter tcpOut;
    private BufferedReader tcpIn;
    private String serverAddress;
    private int tcpPort;
    private int udpPort;
    private int clientId;
    private Consumer<String> messageHandler;
    private boolean running = true;

    public NetworkHandler(String serverAddress, int tcpPort, int udpPort, Consumer<String> messageHandler) {
        this.serverAddress = serverAddress;
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
        this.messageHandler = messageHandler;
    }

    public void connect() throws IOException {
        // Establish TCP connection
        tcpSocket = new Socket(serverAddress, tcpPort);
        tcpOut = new PrintWriter(tcpSocket.getOutputStream(), true);
        tcpIn = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

        udpSocket = new DatagramSocket();

        new Thread(this::listenForMessages).start();
    }

    private void listenForMessages() {
        try {
            String message;
            while (running && (message = tcpIn.readLine()) != null) {
                if (message.startsWith("WELCOME|")) {
                    clientId = Integer.parseInt(message.split("\\|")[1]);
                }
                messageHandler.accept(message);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Network error: " + e.getMessage());
            }
        }
    }

    public void sendTcpMessage(String message) {
        tcpOut.println(message);
    }

    public void sendUdpMessage(String message) {
        try {
            byte[] buffer = message.getBytes();
            InetAddress address = InetAddress.getByName(serverAddress);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, udpPort);
            udpSocket.send(packet);
        } catch (IOException e) {
            System.err.println("UDP send error: " + e.getMessage());
        }
    }

    public void disconnect() {
        running = false;
        try {
            sendTcpMessage("DISCONNECT");
            if (tcpIn != null) tcpIn.close();
            if (tcpOut != null) tcpOut.close();
            if (tcpSocket != null) tcpSocket.close();
            if (udpSocket != null) udpSocket.close();
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }

    public int getClientId() {
        return clientId;
    }
}