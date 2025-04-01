import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

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
