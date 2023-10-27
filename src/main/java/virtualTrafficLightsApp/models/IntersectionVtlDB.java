package virtualTrafficLightsApp.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * The IntersectionVtlDB class represents a database of intersection connections
 * and vehicles, allowing
 * for adding vehicles to the intersection, retrieving the closest vehicle to
 * the intersection, and
 * managing the intersection connections.
 */
public class IntersectionVtlDB {

    private String intersectionVtlId;
    private HashMap<String, Connection> intersectionVtlDB;
    private final ArrayList<String> intersectionConnections = new ArrayList<>();

    public IntersectionVtlDB(HashMap<String, Connection> intersectionVtlDB, String intersectionVtlId) {
        this.intersectionVtlId = intersectionVtlId;
        this.intersectionVtlDB = intersectionVtlDB;
    }

    public String getIntersectionVtlId() {
        return intersectionVtlId;
    }

    public void setIntersectionVtlId(String intersectionVtlId) {
        this.intersectionVtlId = intersectionVtlId;
    }

    public HashMap<String, Connection> getintersectionVtlDB() {
        return intersectionVtlDB;
    }

    public void setintersectionVtlDB(HashMap<String, Connection> intersectionVtlDB) {
        this.intersectionVtlDB = intersectionVtlDB;
    }

    public ArrayList<String> getIntersectionConnections() {
        return intersectionConnections;
    }

    /**
     * The function `addVehicleToIntersection` adds a new vehicle to an
     * intersection's database, updating
     * its information if it already exists.
     *
     * @param new_vehicle The new_vehicle parameter is an object of type
     *                    CAMExtension, which contains
     *                    information about a new vehicle that needs to be added to
     *                    an intersection.
     */
    public void addVehicleToIntersection(Vehicle new_vehicle) {
        String new_vehicle_lane_id = new_vehicle.getConnection_id();
        String new_vehicle_connection_id = new_vehicle_lane_id.substring(0, new_vehicle_lane_id.length() - 2);
        String new_vehicle_id = new_vehicle.getVehicle_id();
        if (!intersectionVtlDB.containsKey(new_vehicle_connection_id)) {
            if (!intersectionConnections.contains(new_vehicle_connection_id)
                    && connectionToIntersection(new_vehicle_connection_id)) {
                intersectionConnections.add(new_vehicle_connection_id);
            }
            if (intersectionConnections.contains(new_vehicle_connection_id)) {
                intersectionVtlDB.put(new_vehicle_connection_id,
                        new Connection(new_vehicle_connection_id, new ArrayList<>()));
                intersectionVtlDB.get(new_vehicle_connection_id)
                        .addLaneToConnection(new Lane(new_vehicle_lane_id, false, 0, 0, 0, 0, new ArrayList<>()));
            }
        }
        boolean vehicleExists = false;
        for (Connection connection : intersectionVtlDB.values()) {
            for (Lane lane : connection.getConnections_lanes()) {
                for (Vehicle vehicle : lane.getLane_list_of_vehicles()) {
                    if (vehicle.getVehicle_id().equals(new_vehicle_id)) {
                        if (lane.getLane_id().equals(new_vehicle_lane_id)) {
                            vehicle.setVehicle_id(new_vehicle.getVehicle_id());
                            vehicle.setDistance_to_intersection(new_vehicle.getDistance_to_intersection());
                            vehicle.setVehicle_route(new_vehicle.getVehicle_route());
                            vehicle.setIs_vehicle_stopped(new_vehicle.isIs_vehicle_stopped());
                            vehicle.setCurrent_velocity(new_vehicle.getCurrent_velocity());
                            lane.getLane_list_of_vehicles()
                                    .sort(Comparator.comparingDouble(Vehicle::getDistance_to_intersection));
                            vehicle.setIs_blinker_right(new_vehicle.isIs_blinker_right());
                            vehicle.setIs_blinker_right(new_vehicle.isIs_blinker_left());
                        } else {
                            // remove the vehicle from its current road if it's on a different road and add
                            // it to the new road
                            lane.getLane_list_of_vehicles().remove(vehicle);
                            lane.setLane_length();
                            lane.setLane_number_of_vehicles();
                        }
                        vehicleExists = true;
                        break;
                    }
                }
            }
        }
        if (!vehicleExists && connectionToIntersection(new_vehicle_connection_id)) {
            // add the vehicle to the new road if it's not already in the map
            for (Lane currentLane : intersectionVtlDB.get(new_vehicle_connection_id).getConnections_lanes()) {
                if (currentLane.getLane_id().equals(new_vehicle_lane_id)) {
                    currentLane.add_new_vehicle(new_vehicle);
                    break;
                }
            }
        }
    }

    /**
     * The function checks if the last part of a given vehicle connection ID is
     * equal to the
     * intersectionVtlId.
     * 
     * @param new_vehicle_connection_id The `new_vehicle_connection_id` parameter is
     *                                  a string that
     *                                  represents the connection ID of a new
     *                                  vehicle.
     * @return The method is returning a boolean value.
     */
    private boolean connectionToIntersection(String new_vehicle_connection_id) {
        String[] parts = new_vehicle_connection_id.split("_");
        if (parts.length >= 1) {
            String lastPart = parts[parts.length - 1];
            return lastPart.equals(intersectionVtlId);
        }
        return false;
    }

    /**
     * The function returns the closest vehicle to an intersection given a lane ID.
     *
     * @param lane_id The `lane_id` parameter is a string that represents the ID of
     *                a lane in the
     *                intersection.
     * @return The method is returning a CAMExtension object.
     */
    public Vehicle getClosestVehicleToIntersection(String lane_id) {
        for (Connection connection : intersectionVtlDB.values()) {
            for (Lane lane_map : connection.getConnections_lanes()) {
                if (lane_map.getLane_id().equals(lane_id) && !lane_map.getLane_list_of_vehicles().isEmpty()) {
                    return lane_map.getLane_list_of_vehicles().get(0);
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "intersectionVtlDB{" + "\n" + "intersectionVtlDB=" + intersectionVtlDB + '}' + '\n';
    }
}
