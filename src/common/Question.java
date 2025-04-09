package common;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Question implements Serializable {
    
    private String question;
    private String[] options;
    private String correctOption;
    private int questionNumber;

    public static Question fromBytes(byte[] data) { return null; }

    public String getQuestion() { return question; }
    public String[] getOptions() { return options; }
    public String getOption(int i) { return options[i]; }
    public String getCorrectOption() { return correctOption; }
    public int getQuestionNumber() { return questionNumber; }

    public Question(String question, String[] options, String correctOption, int questionNumber) {
        this.question = question;
        this.options = options;
        this.correctOption = correctOption;
        this.questionNumber = questionNumber;
    }

    public String toString() {
        return "Question: " + question + "\nOptions: " + String.join(", ", options) + "\nCorrect Answer: " + correctOption;
    }
    public byte[] toBytes() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(this);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
