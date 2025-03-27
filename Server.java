import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class Server {
    
    private ArrayList<Question> questions = new ArrayList<Question>();
    private Question currentQuestion;

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

        // Send question to clients
        
    }

}
