import java.io.*;
import java.util.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {
    
    DatagramSocket _socket;
    private int _serverPort = 5001;

    private ArrayList<Question> _questions = new ArrayList<Question>();
    private Question _currentQuestion;

    int _numClients = 0;
    ArrayList<String> _clients;
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


    private void _handleJoinGameRequest(Message message) {
        _numClients++;
        
    }
    private void _handleReadyToStart(Message message) {

    }
    private void _handleClientPolled(Message message) {

    }
    private void _handleClientAnswered(Message message) {

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
                _handleJoinGameRequest(message);
            
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

    }

}
