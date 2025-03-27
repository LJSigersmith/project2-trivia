import java.io.Serializable;

public class Message implements Serializable {
    
    private static int MSG_JOIN_GAME_REQUEST = 0; // Client requesting to join game
    private static int MSG_STARTING_GAME = 1; // Server sending game start message
    private static int MSG_READY_TO_START = 2; // Client ready to start game
    private static int MSG_QUESTION = 3; // Server sending question
    private static int MSG_POLL = 4; // Client polling
    private static int MSG_GOOD_TO_ANSWER = 5; // Server telling client it was first to poll, can answer the question now
    private static int MSG_NOT_GOOD_TO_ANSWER = 6; // Server telling client it wasnt quick enough, another client is answering the question
    private static int MSG_ANSWER = 7; // Client answering question
    private static int MSG_SCORE = 8; // Server sending client their score after answering (either was correct +1 or wasnt +0)
    private static int MSG_GAME_OVER = 9; // Server sending game over message (with results)

    private int _type;
    private int _nodeID;
    private long _timestamp;
    private String _data;

    public int getTpe() {
        return _type;
    }
    public int getNodeID() {
        return _nodeID;
    }
    public long getTimestamp() {
        return _timestamp;
    }
    public String getData() {
        return _data;
    }
    public void setType(int t) {
        _type = t;
    }
    public void setNodeID(int n) {
        _nodeID = n;
    }
    public void setTimestamp(long t) {
        _timestamp = t;
    }
    public void setData(String d) {
        _data = d;
    }

    

}
