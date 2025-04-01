public class GameProtocol {
    public static final String WELCOME = "WELCOME";
    public static final String QUESTION = "QUESTION";
    public static final String ACK = "ACK";
    public static final String NACK = "NACK";
    public static final String CORRECT = "CORRECT";
    public static final String WRONG = "WRONG";
    public static final String NEXT = "NEXT";
    public static final String SCORE = "SCORE";
    public static final String TERMINATE = "TERMINATE";
    public static final String DISCONNECT = "DISCONNECT";

    public static final String BUZZ = "BUZZ";

    public static final String DELIMITER = "|";

    public static final int BUZZ_TIMEOUT = 15; // seconds
    public static final int ANSWER_TIMEOUT = 10; // seconds
}