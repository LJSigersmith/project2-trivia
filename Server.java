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
    
    DatagramSocket _socket;
    private int _serverPort = 5001;
    private int _nodeID = -1;

    private ArrayList<Question> _questions = new ArrayList<Question>();
    private Question _currentQuestion;

    int _numPlayers = 0;
    ArrayList<Pair<InetAddress, Integer>> _players;
    Boolean _gameStarted = false;

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
		} catch (IOException e) {
			System.out.println("Error loading questions");
			e.printStackTrace();
		}

		//for (Question q : questions) {
		//	System.out.println(q);
		//}

	}


    private void _handleJoinGameRequest(Message message, InetAddress address, int port) {
        
        _numPlayers++;
        _players.add(new Pair<InetAddress, Integer>(address, port));

        
    }
    private void _handleReadyToStart(Message message) {

    }
    private void _handleClientPolled(Message message) {

    }
    private void _handleClientAnswered(Message message) {

    }

    private void _sendMessageToClient(Message message, InetAddress clientAddress, int clientPort) {

        try {

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(message);
            objectStream.flush();
            byte[] data = byteStream.toByteArray();

            DatagramPacket packet = new DatagramPacket(data, data.length, clientAddress, clientPort);
            _socket.send(packet);

        } catch (IOException e) {
            System.out.println("Error sending message to client");
            e.printStackTrace();
        }

    }

    private void _waitForClientMessages() {

        try { _socket = new DatagramSocket(_serverPort); }
        catch (SocketException e) { System.out.println("Error creating socket"); e.printStackTrace(); return; }
        
        try {
        while (true) {

            byte[] payload = new byte[1024];
            DatagramPacket packet = new DatagramPacket(payload, payload.length);
            _socket.receive(packet);

            ByteArrayInputStream in = new ByteArrayInputStream(packet.getData());
            ObjectInputStream is = new ObjectInputStream(in);

            try {
            Message message = (Message) is.readObject();
            System.out.println("Message Recieved: " + message);

            int messageType = message.getType();
            if (Message.MSG_JOIN_GAME_REQUEST == messageType) {
                
                System.out.println("Client requesting to join game");
                _handleJoinGameRequest(message, packet.getAddress(), packet.getPort());
            
            } else if (Message.MSG_READY_TO_START == messageType) {
            
                System.out.println("Client ready to start game");
                _handleReadyToStart(message);
            
            } else if (Message.MSG_POLL == messageType) {
            
                System.out.println("Client polling");
                _handleClientPolled(message);
            
            } else if (Message.MSG_ANSWER == messageType) {
            
                System.out.println("Client answering question");
                _handleClientAnswered(message);
            
            } else {
                System.out.println("Message type not recognized");
            }

            } catch (ClassNotFoundException e) {
                System.out.println("Packet was not Message type");
                e.printStackTrace();
            }

        }
    } catch (IOException e) {
        System.out.println("Error in waitForClientMessages");
        e.printStackTrace();
    }

    }

    public Server() {
        
        // Load questions from file
        _loadQuestions();
        _currentQuestion = _questions.get(0);

        // Start waiting for clients
        Thread waitForClients = new Thread(() -> {
            _waitForClientMessages();
        });
        waitForClients.start();

        // Wait for two players to start game
        while (_numPlayers < 2) {
            try { Thread.sleep(1000); }
            catch (InterruptedException e) { System.out.println("Error sleeping"); e.printStackTrace(); }
            
        }

        // Wait 5 seconds to give other players time to join
        try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
        
        // Tell all players game is starting
        Message startGameMessage = new Message();
        startGameMessage.setType(Message.MSG_STARTING_GAME);
        startGameMessage.setNodeID(_nodeID);
        startGameMessage.setTimestamp(System.currentTimeMillis());
        startGameMessage.setData(null);

        for (Pair<InetAddress, Integer> player: _players) {
            _sendMessageToClient(startGameMessage, player.getKey(), player.getValue());
        }

        // Wait for players to all be ready to start playing (MSG_READY_TO_START)

        // Start game
        // Send first question to all players
        // Wait for first player to poll
        // Keep queue going of polls in background in case first player gets it wrong and theres a handoff
        // Wait for first player to answer
        // Send score to first player and handoff to next player in queue to answer if necessary
        // Send next question, repeat

        // Send game over

    }

}
