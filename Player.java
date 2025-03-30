import java.net.InetAddress;

public class Player {

    private InetAddress _address;
    private int _port;
    private int _nodeID;
    private int _score;
    private boolean _readyToStart;
    private boolean _goodToAnswer;
    private boolean _answered;
    private long _lastPollTime;
    private long _lastAnswerTime;

    public Player(InetAddress address, int port, int nodeID) {
        _address = address;
        _port = port;
        _nodeID = nodeID;
        _score = 0;
        _readyToStart = false;
        _goodToAnswer = false;
        _answered = false;
        _lastPollTime = 0;
        _lastAnswerTime = 0;
    }

    public InetAddress getAddress() {
        return _address;
    }

    public void setAddress(InetAddress address) {
        _address = address;
    }

    public int getPort() {
        return _port;
    }

    public void setPort(int port) {
        _port = port;
    }

    public int getNodeID() {
        return _nodeID;
    }

    public void setNodeID(int nodeID) {
        _nodeID = nodeID;
    }

    public int getScore() {
        return _score;
    }

    public void setScore(int score) {
        _score = score;
    }

    public boolean isReadyToStart() {
        return _readyToStart;
    }

    public void setReadyToStart(boolean readyToStart) {
        _readyToStart = readyToStart;
    }

    public boolean isGoodToAnswer() {
        return _goodToAnswer;
    }

    public void setGoodToAnswer(boolean goodToAnswer) {
        _goodToAnswer = goodToAnswer;
    }

    public boolean isAnswered() {
        return _answered;
    }

    public void setAnswered(boolean answered) {
        _answered = answered;
    }

    public long getLastPollTime() {
        return _lastPollTime;
    }

    public void setLastPollTime(long lastPollTime) {
        _lastPollTime = lastPollTime;
    }

    public long getLastAnswerTime() {
        return _lastAnswerTime;
    }

    public void setLastAnswerTime(long lastAnswerTime) {
        _lastAnswerTime = lastAnswerTime;
    }
}
