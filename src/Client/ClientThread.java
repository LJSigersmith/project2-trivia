package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import Server.Server;

public class ClientThread implements Runnable {
    private final Socket clientSocket;
    private final int clientId;
    private final UDPThread udpThread;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean running = true;
    private int score = 0; // Added score tracking

    public ClientThread(Socket socket, int clientId, UDPThread udpThread) {
        this.clientSocket = socket;
        this.clientId = clientId;
        this.udpThread = udpThread;
    }

    @Override
    public void run() {
        try {
            // Set socket timeout to detect disconnected clients
            clientSocket.setSoTimeout(30000); // 30 seconds timeout

            // Initialize streams
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Send welcome message
            sendMessage("WELCOME|" + clientId);
            System.out.println("Client " + clientId + " connected from " + clientSocket.getInetAddress());

            String input;
            while (running && !clientSocket.isClosed()) {
                try {
                    input = in.readLine();
                    if (input == null) {
                        System.out.println("Client " + clientId + " disconnected.");
                        break;
                    }
                    System.out.println("Client " + clientId + ": " + input);
                    processClientMessage(input);
                } catch (SocketTimeoutException e) {
                    // Check if client is still connected with a ping
                    sendMessage("PING");
                } catch (SocketException e) {
                    System.out.println("Connection reset for client " + clientId);
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("ClientThread " + clientId + " error: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void processClientMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }

        String[] parts = message.split("\\|");
        String command = parts[0];

        switch (command) {
            case "DISCONNECT":
                System.out.println("Client " + clientId + " requested disconnect");
                running = false;
                break;
            case "PONG":
                // Client responded to ping, connection still active
                break;
            case "SCORE":
                if (parts.length > 1) {
                    try {
                        int points = Integer.parseInt(parts[1]);
                        updateScore(points);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid score format from client " + clientId);
                    }
                }
                break;
            case "UDP":
                // Forward message to UDP thread if needed
                if (parts.length > 1 && udpThread != null) {
                    udpThread.sendToClient(clientId, parts[1]);
                }
                break;
            default:
                System.out.println("Unknown command from client " + clientId + ": " + command);
        }
    }

    public void sendMessage(String message) {
        if (out != null && !clientSocket.isClosed()) {
            out.println(message);
            // Check if message was actually sent
            if (out.checkError()) {
                System.err.println("Error sending message to client " + clientId);
                running = false;
            }
        }
    }

    public void updateScore(int points) {
        score += points;
        System.out.println("Client " + clientId + " score updated to: " + score);
        // Notify other clients or update leaderboard if needed
    }

    public int getScore() {
        return score;
    }

    public int getClientId() {
        return clientId;
    }

    public void disconnect() {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing resources for client " + clientId + ": " + e.getMessage());
        }
        System.out.println("Client " + clientId + " resources cleaned up");
        Server.removeClient(clientId);
    }
}

