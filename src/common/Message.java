package common;
import java.io.Serializable;

public class Message implements Serializable {
    
    public static final int MSG_JOIN_GAME_REQUEST = 0; // Client requesting to join game
    public static final int MSG_ACKNOWLEDGE_JOIN_REQUEST = 10;
    public static final int MSG_STARTING_GAME = 1; // Server sending game start message
    public static final int MSG_READY_TO_START = 2; // Client ready to start game
    public static final int MSG_QUESTION = 3; // Server sending question
    public static final int MSG_POLL = 4; // Client polling
    public static final int MSG_GOOD_TO_ANSWER = 5; // Server telling client it was first to poll, can answer the question now
    public static final int MSG_NOT_GOOD_TO_ANSWER = 6; // Server telling client it wasnt quick enough, another client is answering the question
    public static final int MSG_ANSWER = 7; // Client answering question
    public static final int MSG_SCORE = 8; // Server sending client their score after answering (either was correct (+10), incorrect (-10), or time expired (-20))
    public static final int MSG_GAME_OVER = 9; // Server sending game over message (with results)
    public static final int MSG_TIMEOUT = 11;
    public static final int MSG_LEAVE = 13;

    private int _type;
    private int _nodeID;
    private long _timestamp;
    private byte[] _data;

    public int getType() {
        return _type;
    }
    public int getNodeID() {
        return _nodeID;
    }
    public long getTimestamp() {
        return _timestamp;
    }
    public byte[] getData() {
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
    public void setData(byte[] d) {
        _data = d;
    }


    public String toString() {
        String typeString = "";
        switch(_type) {
            case 0:
            typeString = "MSG_JOIN_GAME_REQUEST";
            break;
            case 1:
            typeString = "MSG_STARTING_GAME";
            break;
            case 2:
            typeString = "MSG_READY_TO_START";
            break;
            case 3:
            typeString = "MSG_QUESTION";
            break;
            case 4:
            typeString = "MSG_POLL";
            break;
            case 5:
            typeString = "MSG_GOOD_TO_ANSWER";
            break;
            case 6:
            typeString = "MSG_NOT_GOOD_TO_ANSWER";
            break;
            case 7:
            typeString = "MSG_ANSWER";
            break;
            case 8:
            typeString = "MSG_SCORE";
            break;
            case 9:
            typeString = "MSG_GAME_OVER";
            break;
            default:
            typeString = "UNKNOWN_TYPE";
            break;
        }
        return "Message [type=" + typeString + ", nodeID=" + _nodeID + ", timestamp=" + _timestamp + ", data=" + _data + "]";
    }
}
