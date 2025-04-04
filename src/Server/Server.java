package Server;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import common.Player;
import common.Question;
import common.Message;
import Server.ClientHandler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {
    
    // Network
    private int _serverTCPPort = 5001;
    private int _serverUDPPort = 5002;
    private int _nodeID = -1;
    private ServerSocket _serverSocket;

    // Client Threads
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);

    // Game
    private ArrayList<Question> _questions = new ArrayList<Question>();
    private Question _currentQuestion;
    private int _numQuestions;
    private int _currentQuestionIndex;

    // Players
    private int _numPlayers = 0;
    public void incrementPlayers() { _numPlayers++; }
    Boolean _gameStarted = false;
    public static final ArrayList<ClientHandler> _clientHandlers = new ArrayList<ClientHandler>();

    // Polling
    ArrayList<Player> _pollingQueue;
    public void addToPollingQueue(Player player) { _pollingQueue.add(player); }

    // Question Loop
    Boolean _questionAnswered = false;
    public void setQuestionAnswered() { _questionAnswered = true; }
    String _playerAnswer = "";
    public void setPlayerAnswer(String ans) { _playerAnswer = ans; }
    long _pollExpiration;
    long _answerExpiration;

    // Game Stages
    public static int STAGE_NOT_STARTED = 0;
    public static int STAGE_STARTING_GAME = 1;
    public static int STAGE_ACCEPTING_POLLING = 2;
    public static int STAGE_ACCEPTING_ANSWER = 3;
    private int _gameStage = STAGE_NOT_STARTED;
    public int getGameStage() { return _gameStage; }

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
    private int _initSocket() {
        try {
            _serverSocket = new ServerSocket(_serverTCPPort);
            return 0;
        } catch (IOException e) {
            System.out.println("Error initializing server socket");
            e.printStackTrace();
            return -1;
        }
    }

    // Handling Client Messages
    void _listenForClientMessages() {

        // New thread for each client
        while (true) {

            try {
                Socket clientSocket = _serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                addClient(clientHandler);
                pool.submit(clientHandler);

            } catch (IOException e) {
                System.out.println("Error accepting client: " );
                e.printStackTrace();
            }
            
        }

    }
    public static void addClient(ClientHandler clientHandler) {
        synchronized (_clientHandlers) {
            _clientHandlers.add(clientHandler);
        }
    }
    public static void removeClient(ClientHandler clientHandler) {
        synchronized (_clientHandlers) {
            _clientHandlers.remove(clientHandler);
        }
    }

    private void _sendMessageToClient(Message message, Player player) {

        ClientHandler client = null;
        for (ClientHandler clientHandler : _clientHandlers) {
            if (clientHandler.getClientIP() == player.getAddress() && clientHandler.getClientPort() == player.getPort()) {
                client = clientHandler;
                break;
            }
        }
        if (client == null) { System.out.println("Trying to send message to client that does not match player"); return; }

        client.sendMessageToThisClient(message);

    }
    private void _broadcastQuestion(Question question, long questionExpiration) {

            Message questionMessage = new Message();
            questionMessage.setType(Message.MSG_QUESTION);
            questionMessage.setTimestamp(questionExpiration);
            questionMessage.setNodeID(_nodeID);

            // Send question without correctOption so player cant cheat
            Question questionForClient = new Question(question.getQuestion(), question.getOptions(), null);
            questionMessage.setData(questionForClient.toBytes());

            ClientHandler.sendMessageToAllClients(questionMessage);

    }
    
    private void _handlePoll(Message message, InetAddress messagAddress, int messagePort) {

        Player player = new Player(messagAddress, messagePort, message.getNodeID());
        addToPollingQueue(player);
    }
    private void _listenForUDPMessages() {

        // Init UDP Socket
        DatagramSocket UDPSocket = null;
         try {
             UDPSocket = new DatagramSocket(_serverUDPPort);
         } catch (SocketException e) {
             System.out.println("SocketException occurred while listening for UDP messages");
             e.printStackTrace();
         } catch (IOException e) {
             System.out.println("IOException occurred while listening for UDP messages");
             e.printStackTrace();
         }

        // Listen for UDP Messages
        while (true) {

            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                UDPSocket.receive(packet);
            } catch (IOException e) {
                System.out.println("Error receiving packet");
                e.printStackTrace();
                continue;
            }


            try {

                ByteArrayInputStream bIn = new ByteArrayInputStream(packet.getData());
                ObjectInputStream objIn = new ObjectInputStream(bIn);
                Message message = (Message) objIn.readObject();
     
                if (message.getType() == Message.MSG_POLL) { _handlePoll(message, packet.getAddress(), packet.getPort()); }
     
            } catch (ClassNotFoundException e) { System.out.println("Packet was not Message type");
            } catch (IOException e) { System.out.println("Error reading message from packet"); e.printStackTrace(); }
            
        }
    }
    
    // Constructor / Deconstructor
    public Server() {
        
        // Load questions from file
        _loadQuestions();

    }
    
    // Game
    public void start() {

        int socketInit = _initSocket();
        if (socketInit == -1) { return; }

        _gameStage = STAGE_NOT_STARTED;
         // Start background thread to listen for client TCP connections
         Thread listenForClientMessagesThread = new Thread(() -> {
            _listenForClientMessages();
         });
         listenForClientMessagesThread.start();

         // Start background thread to listen for client UDP messages
         Thread UDPThread = new Thread(() -> {
            _listenForUDPMessages();
         });
         UDPThread.start();


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
        ClientHandler.sendMessageToAllClients(startGameMessage);

        // Wait 5 seconds to give players time to respond ready to start
        try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }

        // Not sure we need to have a READY TO START, or handle it I guess
        // Server should just wait for clients to join, then eventually start game
        // If a client has requested to join, it should be assumed it is ready to start

        // Start game
        boolean moveToNextQuestion = false;
        while (_currentQuestionIndex < _numQuestions) {
            
            // Set question
            _currentQuestion = _questions.get(_currentQuestionIndex);

            // Send question to all active players with time polling will expire
            _gameStage = STAGE_ACCEPTING_POLLING;
            _pollingQueue.clear();
            _questionAnswered = false;
            _pollExpiration = System.currentTimeMillis() + 15000; // 15 seconds to poll
            _broadcastQuestion(_currentQuestion, _pollExpiration);

            while (System.currentTimeMillis() < _pollExpiration) {} // wait for polling time to expire
            _gameStage = STAGE_ACCEPTING_ANSWER;

            // Send GOOD TO ANSWER to first player to poll with time allowed to answer
            while (!moveToNextQuestion) {

                // if nobody polled, move on
                if (_pollingQueue.size() == 0) { moveToNextQuestion = true; continue; }

                Player firstToPoll = _pollingQueue.get(0);
                _playerAnswer = "";
                _answerExpiration = System.currentTimeMillis() + 10000; // 10 sec to answer
                Message goodToAnswerMessage = getGoodToAnsMessage(_answerExpiration);
                _sendMessageToClient(goodToAnswerMessage, firstToPoll);
                
                while (!_questionAnswered || System.currentTimeMillis() < _answerExpiration) {} // wait for answer time to expire or answer to be recieved

                // Check answer
                int playerScoreUpdate = 0;
                if (!_questionAnswered) { // timer expired, question wasnt answered, pass to next in poll queue
                    playerScoreUpdate = -20;
                }
                if (_questionAnswered && _playerAnswer == _currentQuestion.getCorrectOption()) { // player answered right, go to next question
                    playerScoreUpdate = 10;
                    moveToNextQuestion = true;
                }
                if (_questionAnswered && _playerAnswer != _currentQuestion.getCorrectOption()) { // player answered wrong, go to next in polling queue
                    playerScoreUpdate = -10;
                }
                // Send SCORE back to client answering
                Message scoreMessage = getScoreMessage(playerScoreUpdate);
                _sendMessageToClient(scoreMessage, firstToPoll);
            
                if (moveToNextQuestion) { continue; }
                if (_pollingQueue.size() == 0) { moveToNextQuestion = true; continue; } // if nobody left in polling queue, go to next question

                // remove all instances of player in polling queue (they already had their chance to answer)
                _pollingQueue.removeIf(player -> player.equals(firstToPoll));
                // loop back to next in poll queue

            }

            // Move on to next question
            _currentQuestionIndex++;

        }
    
        // Game over, send to all players
        Message gameOverMessage = getGameOverMessage();
        ClientHandler.sendMessageToAllClients(gameOverMessage);

        System.out.println("Game completed");
    }
    
    // Messages
    public Message getStartGameMessage() {
        Message startGameMessage = new Message();
        startGameMessage.setType(Message.MSG_STARTING_GAME);
        startGameMessage.setNodeID(_nodeID);
        startGameMessage.setTimestamp(System.currentTimeMillis());
        startGameMessage.setData(null);
        return startGameMessage;
    }
    public Message getAckJoinMessage() {
        Message ackJoinMessage = new Message();
        ackJoinMessage.setType(Message.MSG_ACKNOWLEDGE_JOIN_REQUEST);
        ackJoinMessage.setNodeID(_nodeID);
        ackJoinMessage.setTimestamp(System.currentTimeMillis());
        ackJoinMessage.setData(null);
        return ackJoinMessage;
    }
    public Message getGoodToAnsMessage(long answerExpirationTime) {
        Message goodToAnsMessage = new Message();
        goodToAnsMessage.setType(Message.MSG_GOOD_TO_ANSWER);
        goodToAnsMessage.setNodeID(_nodeID);
        goodToAnsMessage.setTimestamp(answerExpirationTime); // time when answering is allowed until (10 secs)
        goodToAnsMessage.setData(null);
        return goodToAnsMessage;
    }
    public Message getScoreMessage(int playerScoreUpdate) {
        Message scoreMessage = new Message();
        scoreMessage.setType(Message.MSG_SCORE);
        scoreMessage.setNodeID(_nodeID);
        scoreMessage.setTimestamp(System.currentTimeMillis()); 
        scoreMessage.setData(Integer.toString(playerScoreUpdate).getBytes());
        return scoreMessage;
    }
    public Message getGameOverMessage() {
        Message scoreMessage = new Message();
        scoreMessage.setType(Message.MSG_GAME_OVER);
        scoreMessage.setNodeID(_nodeID);
        scoreMessage.setTimestamp(System.currentTimeMillis()); // time when answering is allowed until (10 secs)
        scoreMessage.setData(null);
        return scoreMessage;
    }

}
