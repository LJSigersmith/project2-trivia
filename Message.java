import java.io.Serializable;

public class Message implements Serializable {
    
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
