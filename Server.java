import java.io.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    
    private Socket s = null;
    private ServerSocket ss = null;
    private DataInputStream in = null;

    private ArrayList<Question> questions = new ArrayList<Question>();
    private Question currentQuestion;

    int numClients = 0;
    ArrayList<String> clients;

    void loadQuestions() {

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
				questions.add(newQuestion);

			}
		} catch (IOException e) {
			System.out.println("Error loading questions");
			e.printStackTrace();
		}

		//for (Question q : questions) {
		//	System.out.println(q);
		//}

	}

    public Server() {
        
        // Load questions from file
        loadQuestions();
        currentQuestion = questions.get(0);

        while (numClients < 2) {
            // Start server and wait for at least 2 clients to start game
            try {
                ss = new ServerSocket();
                s = ss.accept();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }

}
