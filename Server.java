import java.io.*;
import java.util.*;

import javafx.util.Pair;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {
    
    // Network
    private int _serverPort = 5001;
    private int _nodeID = -1;
    private DatagramSocket _serverSocket;

    // Game
    private ArrayList<Question> _questions = new ArrayList<Question>();
    private Question _currentQuestion;
    private int _numQuestions;
    private int _currentQuestionIndex;

    // Players
    int _numPlayers = 0;
    ArrayList<Player> _players = new ArrayList<Player>();
    ArrayList<Player> _activePlayers = new ArrayList<Player>();
    Boolean _gameStarted = false;

    // Polling
    ArrayList<Player> _pollingQueue;

    // Answering
    Boolean _playerAnswered = false;
    String _playerAnswer;

    int STAGE_NOT_STARTED = 0;
    int STAGE_STARTING_GAME = 1;
    int STAGE_QUESTIONS = 2;
    int STAGE_WAITING_FOR_ANSWER = 3;
    int _gameStage = STAGE_NOT_STARTED;

    // Setup
    private void _loadQuestions() {

		String filePath = "questions.properties";
		Properties properties = new Properties();

		try {
			FileInputStream fileInputStream = new FileInputStream(filePath);
			properties.load(fileInputStream);
			fileInputStream.close();

			for (int i = 1; i <= 10; i++) {
				String question = properties.getProperty("question" + i);
				String optionsString = properties.getProperty("options" + i);
				String correctOption = properties.getProperty("correct_answer" + i);

				String[] options = optionsString.split(", ");

				Question newQuestion = new Question(question, options, correctOption);
				_questions.add(newQuestion);

			}

            _numQuestions = _questions.size();
            _currentQuestion = _questions.get(0);
            _currentQuestionIndex = 0;
            _numQuestions = _questions.size();

		} catch (IOException e) {
			System.out.println("Error loading questions");
			e.printStackTrace();
		}

		//for (Question q : questions) {
		//	System.out.println(q);
		//}

	}

    // Sending to Client
    private void  _sendMessageToClient(Message message, InetAddress clientAddress, int clientPort) {

        try {

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(message);
            objectStream.flush();
            byte[] data = byteStream.toByteArray();

            DatagramPacket packet = new DatagramPacket(data, data.length, clientAddress, clientPort);
            _serverSocket.send(packet);

        } catch (IOException e) {
            System.out.println("Error sending message to client");
            e.printStackTrace();
        }

    }

    // Handling Client Messages
    void _listenForClientMessages() {

        while (true) {
            
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                _serverSocket.receive(packet);
            } catch (IOException e) {
                System.out.println("Error receiving packet");
                e.printStackTrace();
                continue;
            }
            
            try {

            ByteArrayInputStream bIn = new ByteArrayInputStream(packet.getData());
            ObjectInputStream objIn = new ObjectInputStream(bIn);
            Message message = (Message) objIn.readObject();

            if (message.getType() == Message.MSG_JOIN_GAME_REQUEST) { _handleJoinGameRequest(message, packet.getAddress(), packet.getPort()); }

        } catch (ClassNotFoundException e) {
            System.out.println("Packet was not Message type");
        } catch (IOException e) {
            System.out.println("Error reading message from packet");
            e.printStackTrace();
        }
            
            
        }

    }
    void _handleJoinGameRequest(Message message, InetAddress messageAddress, int messagePort) {
        
        // Server is waiting for players
        if (_gameStage == STAGE_NOT_STARTED) {
            Player newPlayer = new Player(messageAddress, messagePort, message.getNodeID());
            _players.add(newPlayer);
            _numPlayers++;
            System.out.println("Player joined: " + newPlayer.getNodeID() + " from " + newPlayer.getAddress() + ":" + newPlayer.getPort());
        
            // Acknowledge request
            Message acknowledgementMessage = getAckJoinMessage();
            _sendMessageToClient(acknowledgementMessage, messageAddress, messagePort);
        }
    }
    void _handleReadyToStart(Message message, InetAddress messageAddress, int messagePort) {

        if (_gameStage == STAGE_STARTING_GAME) {
            Player player = new Player(messageAddress, messagePort, message.getNodeID());
            _activePlayers.add(player);
        }

    }

    // Game
    private void _broadcastQuestion(Question question) {
        
    }
    public void start() {

        _gameStage = STAGE_NOT_STARTED;
         // Start background thread to listen for client messages to join
         Thread listenForClientMessagesThread = new Thread(() -> {
            _listenForClientMessages();
         });
         listenForClientMessagesThread.start();


        // Wait for two players to start game
        while (_numPlayers < 2) {
            System.out.println("Waiting for players to join...");
            try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
        }
        // Wait 5 seconds to give other players time to join
        try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
        
        _gameStage = STAGE_STARTING_GAME;
        // Tell all players game is starting
        Message startGameMessage = getStartGameMessage();
        for (Player player: _players) {
            _sendMessageToClient(startGameMessage, player.getAddress(), player.getPort());
        }
        // Wait 5 seconds to give players time to respond ready to start
        try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }

        // _activePlayers is now all players that responded READY TO START

        // Start game
        _gameStage = STAGE_QUESTIONS;
        while (_currentQuestionIndex < _numQuestions) {
            
            // Send question to all active players with time question will expire
            _broadcastQuestion(_currentQuestion);

            // Wait for first player to poll

            // Send GOOD TO ANSWER to first player to poll

            // Wait for ANSWER

            // Send SCORE back to client answering

            // If question was incorrect, wait for ANSWER from next in poll queue
            // If question was incorrect & polling queue is empty & time has expired, send next QUESTION
            // If question was correct, send next QUESTION
            
        }
    }
    
    // Constructor / Deconstructor
    public Server() {
        
        // Load questions from file
        _loadQuestions();

        // Initialize socket
        try {
            _serverSocket = new DatagramSocket(_serverPort);
        } catch (SocketException e) {
            System.out.println("Error creating socket");
            e.printStackTrace();
        }

    }

    // Messages
    private Message getStartGameMessage() {
        Message startGameMessage = new Message();
        startGameMessage.setType(Message.MSG_STARTING_GAME);
        startGameMessage.setNodeID(_nodeID);
        startGameMessage.setTimestamp(System.currentTimeMillis());
        startGameMessage.setData(null);
        return startGameMessage;
    }
    private Message getAckJoinMessage() {
        Message ackJoinMessage = new Message();
        ackJoinMessage.setType(Message.MSG_ACKNOWLEDGE_JOIN_REQUEST);
        ackJoinMessage.setNodeID(_nodeID);
        ackJoinMessage.setTimestamp(System.currentTimeMillis());
        ackJoinMessage.setData(null);
        return ackJoinMessage;
    }

}
