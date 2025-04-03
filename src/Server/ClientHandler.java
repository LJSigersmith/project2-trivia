package Server;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import common.Message;
import common.Player;

public class ClientHandler implements Runnable {
    
    private final Socket _socket;
    private BufferedReader _in;
    private DataInputStream _dataIn;
    private OutputStream _outToClient;

    InetAddress _clientIP;
    public InetAddress getClientIP() { return _clientIP; }
    int _clientPort;
    public int getClientPort() { return _clientPort; }

    private final Server _server;

    private static final CopyOnWriteArrayList<OutputStream> _clientWriters = new CopyOnWriteArrayList<>();
    
    public ClientHandler(Socket socket, Server server) {
        this._socket = socket;
        this._server = server;
        try {
        this._in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this._dataIn = new DataInputStream(socket.getInputStream());
        this._outToClient = _socket.getOutputStream();
        } catch (IOException e) { System.out.println("Error in ClientHandler constructor"); e.printStackTrace(); }
    }

    private void _handleJoinGameRequest(Message message, InetAddress messageAddress, int messagePort) {
        
        // Server is waiting for players
        if (_server.getGameStage() == Server.STAGE_NOT_STARTED) {
            Player newPlayer = new Player(messageAddress, messagePort, message.getNodeID());
            //_server.addPlayer((newPlayer));
            _server.incrementPlayers();
            System.out.println("Player joined: " + newPlayer.getNodeID() + " from " + newPlayer.getAddress() + ":" + newPlayer.getPort());
        
            // Acknowledge request
            Message acknowledgementMessage = _server.getAckJoinMessage();
            _sendMessageToClient(_outToClient, acknowledgementMessage);
        }
    }
    private void _handleReadyToStart(Message message, InetAddress messageAddress, int messagePort) {

        if (_server.getGameStage() == Server.STAGE_STARTING_GAME) {
            Player player = new Player(messageAddress, messagePort, message.getNodeID());
            //_activePlayers.add(player);
            System.out.println("Player ready to start: " + player.getNodeID() + " from " + messageAddress + ":" + messagePort);
        }

    }
    private void _handleGoodToAnswer(Message message, InetAddress messaegeAddress, int messagePort) {

        if (_server.getGameStage() == Server.STAGE_ACCEPTING_ANSWER) {

            _server.setPlayerAnswer(message.getData().toString());
            _server.setQuestionAnswered();

        }

    }

    // Sending to Client
    private static void  _sendMessageToClient(OutputStream outToClient, Message message) {

        try {

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(message);
            objectStream.flush();
            byte[] data = byteStream.toByteArray();

            outToClient.write(data);
            outToClient.flush();

        } catch (IOException e) {
            System.out.println("Error sending message to client");
            e.printStackTrace();
        }

    }
    public static void sendMessageToAllClients(Message message) {
        for (OutputStream outToClient : _clientWriters) {

            _sendMessageToClient(outToClient, message);

        }
    }
    public void sendMessageToThisClient(Message message) {

        try {

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(message);
            objectStream.flush();
            byte[] data = byteStream.toByteArray();

            _outToClient.write(data);
            _outToClient.flush();

        } catch (IOException e) {
            System.out.println("Error sending message to client");
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        try {

            _clientIP = _socket.getInetAddress();
            _clientPort = _socket.getPort();

            _in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));

            // Add this client's OutputStream to list
            synchronized (_clientWriters) {
                _clientWriters.add(_outToClient);
            }

            byte[] buffer = new byte[1024];
            int bytesRead;
            // Read incoming tcp messages
            while ((bytesRead = _dataIn.read(buffer)) != -1) {
                
                byte[] data = Arrays.copyOf(buffer, bytesRead);
                
                try {

                ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
                ObjectInputStream objIn = new ObjectInputStream(byteIn);
                Message message = (Message) objIn.readObject();

                if (message.getType() == Message.MSG_JOIN_GAME_REQUEST) { _handleJoinGameRequest(message, _clientIP, _clientPort); }
                if (message.getType() == Message.MSG_READY_TO_START) { _handleReadyToStart(message, _clientIP, _clientPort);} 
                // POLL is handled on UDP, ClientHandler is all TCP
                //if (message.getType() == Message.MSG_POLL) { _handlePoll(message, _clientIP, _clientPort); }
                if (message.getType() == Message.MSG_GOOD_TO_ANSWER) { _handleGoodToAnswer(message, _clientIP, _clientPort);}

                } catch (ClassNotFoundException e) {
                    System.out.println("Packet was not Message type");
                } catch (IOException e) {
                    System.out.println("Error reading message from packet");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Error with client communication: " + e.getMessage());
        } finally {
            try {
                // Remove client from list and close streams when client disconnects
                synchronized (_clientWriters) {
                    _clientWriters.remove(_outToClient);
                }
                _socket.close();
                _in.close();
                _outToClient.close();
                System.out.println("Client disconnected: " + _socket.getRemoteSocketAddress());
            } catch (IOException e) {
                System.out.println("Error closing client: " + e.getMessage());
            }
        }
    }
}
