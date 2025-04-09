package Server;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

import common.Message;
import common.Player;
import common.Question;

public class ClientHandler implements Runnable {
    
    private final Socket _socket;
    private ObjectOutputStream _outToClient;
    private ObjectInputStream _objIn;

    InetAddress _clientIP;
    public InetAddress getClientIP() { return _clientIP; }
    int _clientPort;
    public int getClientPort() { return _clientPort; }
    int _clientID = -1;
    public int getClientID() { return _clientID; }
    int _clientScore = 0;
    public int getClientScore() { return _clientScore; }
    public void updateClientScore(int c) { _clientScore += c; }

    private final Server _server;

    private static final CopyOnWriteArrayList<ObjectOutputStream> _clientWriters = new CopyOnWriteArrayList<>();
    
    // Constructor
    public ClientHandler(Socket socket, Server server) {
        _socket = socket;
        _server = server;
        try {
        //this._in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        _outToClient = new ObjectOutputStream(_socket.getOutputStream());
        _outToClient.flush();
        _objIn = new ObjectInputStream(socket.getInputStream());
        //this._dataIn = new DataInputStream(socket.getInputStream());
        } catch (IOException e) { System.out.println("Error in ClientHandler constructor"); e.printStackTrace(); }
    }

    // Kill client
    public void kill() {
        try {
            // Remove client from list and close streams when client disconnects
            synchronized (_clientWriters) {
                _clientWriters.remove(_outToClient);
                Server.removeClient(this);
                _server.GUI_updateConnectedPlayersList(Server._clientHandlers);
                _server.GUI_updatePlayerScoresList(Server._clientHandlers);
            }
            _socket.close();
            _outToClient.close();
            System.out.println("Client disconnected: " + _socket.getRemoteSocketAddress());
        } catch (IOException e) {
            System.out.println("Error closing client: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return _clientIP + ":" + _clientPort + "::ID_" + _clientID;
    }

    // Handle Messages
    private void _handleJoinGameRequest(Message message, InetAddress messageAddress, int messagePort) {
        
        // Server is waiting for players
        if (_server.getGameStage() == Server.STAGE_NOT_STARTED) {
            Player newPlayer = new Player(messageAddress, messagePort, message.getNodeID());
            //_server.addPlayer((newPlayer));
            _server.incrementPlayers();
            System.out.println("Player joined: " + newPlayer.getNodeID() + " from " + newPlayer.getAddress() + ":" + newPlayer.getPort());

            _server.GUI_updateConnectedPlayersList(Server._clientHandlers);
            _server.GUI_updatePlayerScoresList(Server._clientHandlers);
        
            // Acknowledge request
            Message acknowledgementMessage = _server.getAckJoinMessage();
            _sendMessageToClient(_outToClient, acknowledgementMessage);
        } else {
            // wait until server is between questions
            while (_server.getGameStage() != Server.STAGE_BETWEEN_QUESTIONS) { try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); } }
            Player newPlayer = new Player(messageAddress, messagePort, message.getNodeID());
            //_server.addPlayer((newPlayer));
            _server.incrementPlayers();
            System.out.println("Player joined: " + newPlayer.getNodeID() + " from " + newPlayer.getAddress() + ":" + newPlayer.getPort());

            _server.GUI_updateConnectedPlayersList(Server._clientHandlers);
            _server.GUI_updatePlayerScoresList(Server._clientHandlers);
        
            // Acknowledge request
            Message acknowledgementMessage = _server.getAckJoinMessage();
            _sendMessageToClient(_outToClient, acknowledgementMessage);
        }
    }
    private void _handleGoodToAnswer(Message message, InetAddress messaegeAddress, int messagePort) {

        if (_server.getGameStage() == Server.STAGE_ACCEPTING_ANSWER) {

            _server.setPlayerAnswer(message.getData().toString());
            _server.setQuestionAnswered();

        }

    }
    private void _handleAnswer(Message message, InetAddress messageAddress, int messagePort) {

        if (_server.getGameStage() == Server.STAGE_ACCEPTING_ANSWER) {

            byte[] answerData = message.getData();
            int answerOpt = Integer.parseInt(new String(answerData).trim());
            System.out.println("Player answered option#: " + answerOpt);
            Question currQ = _server.getCurrentQuestion();
            String answerString = currQ.getOption(answerOpt - 1);

            System.out.println("Player Answer: " + answerString);
            _server.setPlayerAnswer(answerString);
            _server.setQuestionAnswered();

            _server.GUI_updateClientAnswerLabel(answerString);
            _server.GUI_updateClientAnsweredLabel(true);
        }

    }

    // Sending to Client
    private static void  _sendMessageToClient(ObjectOutputStream outToClient, Message message) {

        try {

            outToClient.writeObject(message);
            outToClient.flush();

        } catch (IOException e) {
            System.out.println("Error sending message to client");
            e.printStackTrace();
        }

    }
    public static void sendMessageToAllClients(Message message) {
        
        System.out.println("Sending message to all clients");
        for (ObjectOutputStream outToClient : _clientWriters) {

            System.out.println("Sending message to client: " + message);
            _sendMessageToClient(outToClient, message);

        }
    }
    public void sendMessageToThisClient(Message message) {

        try {

            //ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            //ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            _outToClient.writeObject(message);
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

            // Add this client's OutputStream to list
            synchronized (_clientWriters) {
                _clientWriters.add(_outToClient);
            }

            // Main loop
            while (true) {    

                try {
 
                Object readObject = _objIn.readObject();
                if (!(readObject instanceof Message)) { System.out.println("Packet not Message type"); continue; }
                
                Message message = (Message) readObject;
                System.out.println(message);
                System.out.println("Message Type: " + message.getType());

                // client ID not set yet
                if (_clientID == -1) { _clientID = message.getNodeID();    }

                if (message.getType() == Message.MSG_JOIN_GAME_REQUEST) { _handleJoinGameRequest(message, _clientIP, _clientPort); }
                if (message.getType() == Message.MSG_GOOD_TO_ANSWER) { _handleGoodToAnswer(message, _clientIP, _clientPort);}
                if (message.getType() == Message.MSG_ANSWER) { _handleAnswer(message, _clientIP, _clientPort); }

                } catch (ClassNotFoundException e) { System.out.println("Packet was not Message type");
                } catch (IOException e) {
                    System.out.println("Error reading message from packet");
                    e.printStackTrace();
                    System.out.println("Error message: ");
                    System.out.println(e.getMessage());
                    break;
                }
            }
        } finally {
            try {
                // Remove client from list and close streams when client disconnects
                synchronized (_clientWriters) {
                    _clientWriters.remove(_outToClient);
                    Server.removeClient(this);
                    _server.GUI_updateConnectedPlayersList(Server._clientHandlers);
                    _server.GUI_updatePlayerScoresList(Server._clientHandlers);
                }
                _socket.close();
                _outToClient.close();
                System.out.println("Client disconnected: " + _socket.getRemoteSocketAddress());
            } catch (IOException e) {
                System.out.println("Error closing client: " + e.getMessage());
            }
        }
    }
}
