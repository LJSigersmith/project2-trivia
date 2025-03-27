
public class Question {
    
    private String question;
    private String[] options;
    private String correctOption;

    public String getQuestion() {
        return question;
    }
    public String[] getOptions() {
        return options;
    }
    public String getCorrectOption() {
        return correctOption;
    }

    public Question(String question, String[] options, String correctOption) {
        this.question = question;
        this.options = options;
        this.correctOption = correctOption;
    }

    public String toString() {
        return "Question: " + question + "\nOptions: " + String.join(", ", options) + "\nCorrect Answer: " + correctOption;
    }
}
