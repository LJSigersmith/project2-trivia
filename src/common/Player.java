package common;
import java.net.InetAddress;

public class Player {

    private InetAddress _address;
    private int _port;
    private int _nodeID;

    public Player(InetAddress address, int port, int nodeID) {
        _address = address;
        _port = port;
        _nodeID = nodeID;
    }

    @Override
    public String toString() {
        return _address.getHostName() + ":" + _port + "::ID_" + _nodeID;
    }

    public InetAddress getAddress() { return _address; }
    public void setAddress(InetAddress address) { _address = address; }
    public int getPort() { return _port; }
    public void setPort(int port) { _port = port; }
    public int getNodeID() { return _nodeID; }
    public void setNodeID(int nodeID) { _nodeID = nodeID; }

}
