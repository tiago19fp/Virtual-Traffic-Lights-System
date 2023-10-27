package virtualTrafficLightsApp.models;

import java.util.ArrayList;

/**
 * The Connection class represents a connection between intersections in a
 * traffic system, with a unique ID and
 * a list of lanes.
 */
public class Connection {

    private String connection_id;
    private ArrayList<Lane> connections_lanes;

    public Connection(String connection_id, ArrayList<Lane> connections_lanes) {
        this.connection_id = connection_id;
        this.connections_lanes = connections_lanes;
    }

    public String getConnection_id() {
        return connection_id;
    }

    public void setConnection_id(String connection_id) {
        this.connection_id = connection_id;
    }

    public ArrayList<Lane> getConnections_lanes() {
        return connections_lanes;
    }

    public void setConnections_lanes(ArrayList<Lane> connections_lanes) {
        this.connections_lanes = connections_lanes;
    }

    public void addLaneToConnection(Lane new_lane) {
        connections_lanes.add(new_lane);
    }

    @Override
    public String toString() {
        return "Connection{" +
                "connection_id='" + connection_id + '\'' +
                ", connections_lanes=" + connections_lanes +
                '}';
    }
}
