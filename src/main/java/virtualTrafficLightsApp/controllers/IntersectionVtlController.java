package virtualTrafficLightsApp.controllers;

import virtualTrafficLightsApp.models.Connection;
import virtualTrafficLightsApp.models.IntersectionVtlDB;
import virtualTrafficLightsApp.models.Lane;
import virtualTrafficLightsApp.models.Vehicle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The IntersectionVtlController class manages traffic lights and lanes at an
 * intersection.
 */
public class IntersectionVtlController {
    private IntersectionVtlDB intersectionVtlDB;
    private int connection_green_light_counter = 0;

    public IntersectionVtlController(IntersectionVtlDB intersectionVtlDB) {
        this.intersectionVtlDB = intersectionVtlDB;
    }

    public IntersectionVtlDB getIntersectionVtlDB() {
        return intersectionVtlDB;
    }

    public void setIntersectionVtlDB(IntersectionVtlDB intersectionVtlDB) {
        this.intersectionVtlDB = intersectionVtlDB;
    }

    /**
     * The function changes the traffic light states for a given intersection based
     * on the current time and
     * the time it takes for the lights to change.
     *
     * @param current_time The current time in seconds.
     * @param time_to_stop The parameter `time_to_stop` represents the duration in
     *                     seconds for which the
     *                     traffic lights will remain red before changing to green
     *                     for the next connection.
     * @return The method is returning the current_time variable, which is of type
     *         long.
     */
    public long changeTrafficLightsStatic(long current_time, int time_to_stop) {
        String current_connection_green_light = intersectionVtlDB.getIntersectionConnections()
                .get(connection_green_light_counter);
        for (Connection connection : intersectionVtlDB.getintersectionVtlDB().values()) {
            if (intersectionVtlDB.getIntersectionConnections().contains(connection.getConnection_id())) {
                for (Lane lane : connection.getConnections_lanes()) {
                    lane.setLane_stop_time_in_seconds(time_to_stop);
                    lane.changeTrafficLightState(false, current_time);
                }
            }
        }
        if (intersectionVtlDB.getintersectionVtlDB().containsKey(current_connection_green_light)) {
            for (Connection connection : intersectionVtlDB.getintersectionVtlDB().values()) {
                if (connection.getConnection_id().equals(current_connection_green_light)) {
                    for (Lane lane : connection.getConnections_lanes()) {
                        lane.changeTrafficLightState(false, current_time);
                    }
                }
            }
            connection_green_light_counter = (connection_green_light_counter + 1)
                    % intersectionVtlDB.getIntersectionConnections().size();
            String new_connection_green_light = intersectionVtlDB.getIntersectionConnections()
                    .get(connection_green_light_counter);
            for (Connection connection : intersectionVtlDB.getintersectionVtlDB().values()) {
                if (connection.getConnection_id().equals(new_connection_green_light)) {
                    for (Lane lane : connection.getConnections_lanes()) {
                        lane.setLane_stop_time_in_seconds(0);
                        lane.changeTrafficLightState(true, current_time);
                    }
                }
            }
        }
        return current_time;
    }

    /**
     * The function changes the length of traffic lights for a specific lane at an
     * intersection.
     *
     * @param new_lane_green_light The parameter "new_lane_green_light" is a String
     *                             that represents the ID
     *                             of the lane that should have a green traffic
     *                             light.
     * @param current_time         The current time in milliseconds.
     * @param time_to_stop         The parameter `time_to_stop` represents the
     *                             duration in seconds for which the
     *                             traffic light should remain red before changing
     *                             to green for the new lane.
     * @return The method is returning the variable "time_to_stop" of type long.
     */
    public long changeTrafficLightsLength(String new_lane_green_light, long current_time, long time_to_stop) {
        String current_connection_green_light = intersectionVtlDB.getIntersectionConnections()
                .get(connection_green_light_counter);
        for (Connection connection : intersectionVtlDB.getintersectionVtlDB().values()) {
            for (Lane lane : connection.getConnections_lanes()) {
                if (lane.isLane_state_go()) {
                    lane.setLane_state_go(false);
                }
            }
        }
        if (intersectionVtlDB.getintersectionVtlDB().containsKey(current_connection_green_light)) {
            for (Connection connection : intersectionVtlDB.getintersectionVtlDB().values()) {
                if (connection.getConnection_id().equals(current_connection_green_light)) {
                    for (Lane lane : connection.getConnections_lanes()) {
                        lane.changeTrafficLightState(false, current_time);
                        lane.setLane_stop_time_in_seconds(time_to_stop);
                    }
                }
                for (Lane lane : connection.getConnections_lanes()) {
                    if (lane.getLane_id().equals(new_lane_green_light)) {
                        lane.setLane_stop_time_in_seconds(0);
                        lane.changeTrafficLightState(true, current_time);
                    }
                }
            }
        }
        return time_to_stop;
    }

    /**
     * The function "safeToGoBaseOnRoutes" checks if it is safe for a vehicle to go
     * based on the current
     * traffic conditions and routes.
     * 
     * @return The method is returning an object of type Lane.
     */
    public Lane safeToGoBaseOnRoutes() {
        ArrayList<Vehicle> arrayListVehiclesGreenLane = new ArrayList<>();
        boolean should_go = true;
        Vehicle vehicle_0;
        for (Connection connection : intersectionVtlDB.getintersectionVtlDB().values()) {
            if (intersectionVtlDB.getIntersectionConnections().contains(connection.getConnection_id())) {
                for (Lane lane : connection.getConnections_lanes()) {
                    if (lane.isLane_state_go()) {
                        arrayListVehiclesGreenLane.addAll(lane.getLane_list_of_vehicles().subList(0,
                                Math.min(1, lane.getLane_list_of_vehicles().size())));
                    }
                }
            }
        }
        ArrayList<Lane> lanes_in_order = getLaneStopTimesDescending();
        for (Lane lane : lanes_in_order) {
            if (!lane.isLane_state_go()) {
                if (intersectionVtlDB.getIntersectionConnections()
                        .contains(lane.getLane_id().substring(0, lane.getLane_id().length() - 2))
                        && lane.getLane_number_of_vehicles() > 0
                        && lane.getLane_list_of_vehicles().get(0).getDistance_to_intersection() < 20) {
                    if (arrayListVehiclesGreenLane.size() > 0) {
                        vehicle_0 = lane.getLane_list_of_vehicles().get(0);
                        for (Vehicle vehicle_lane_green : arrayListVehiclesGreenLane) {
                            should_go = checkCollision(vehicle_lane_green, vehicle_0);
                            if (should_go) {
                                break;
                            }
                        }
                    }
                }
            }
            if (!should_go) {
                return lane;
            }
        }
        return null;
    }

    /**
     * The function checks for collision between two vehicles based on their routes,
     * blinker status, and
     * segment verification.
     * 
     * @param vehicle1 The parameter `vehicle1` is an instance of the `Vehicle`
     *                 class, representing the
     *                 first vehicle involved in the collision check.
     * @param vehicle2 The above code is a method called `checkCollision` that takes
     *                 two `Vehicle` objects
     *                 as parameters (`vehicle1` and `vehicle2`). It checks for
     *                 collision between the two vehicles based on
     *                 their routes and blinker status.
     * @return The method is returning a boolean value.
     */
    private boolean checkCollision(Vehicle vehicle1, Vehicle vehicle2) {
        int should_go = 0;
        List<String> route1 = formatRoute(vehicle1.getVehicle_route().getConnectionIds());
        List<String> route2 = formatRoute(vehicle2.getVehicle_route().getConnectionIds());
        if (verifySegments(route1.get(1), route2.get(1))) {
            return true;
        }
        if (vehicle1.isIs_blinker_right() && vehicle2.isIs_blinker_right()) {
            return false;
        }
        if (verifySegments(route1.get(0), route2.get(1)) && verifySegments(route1.get(1), route2.get(0))) {
            if (vehicle1.isIs_blinker_right() || vehicle2.isIs_blinker_right()) {
                return false;
            }
        }
        if (verifySegments(route1.get(0), route2.get(1))) {
            should_go++;
        }
        if (verifySegments(route1.get(1), route2.get(0))) {
            should_go++;
        }
        if (should_go == 1) {
            if (vehicle1.isIs_blinker_right() || vehicle2.isIs_blinker_right()) {
                should_go++;
            }
        }
        return should_go != 2;
    }

    /**
     * The function `verifySegments` checks if two strings have the same number of
     * segments and if each
     * segment in the first string is present in the second string.
     *
     * @param str1 A string containing segments separated by underscores (e.g.
     *             "segment1_segment2_segment3").
     * @param str2 The `str2` parameter is a string that will be split into segments
     *             using the underscore
     *             character ("_").
     * @return The method is returning a boolean value.
     */
    private static boolean verifySegments(String str1, String str2) {
        String[] segments1 = str1.split("_");
        String[] segments2 = str2.split("_");

        if (segments1.length != segments2.length) {
            return false;
        }

        for (String segment : segments1) {
            boolean segmentFound = false;
            for (String otherSegment : segments2) {
                if (segment.equals(otherSegment)) {
                    segmentFound = true;
                    break;
                }
            }
            if (!segmentFound) {
                return false;
            }
        }
        return true;
    }

    /**
     * The function formatRoute takes a list of strings representing a route and
     * returns a formatted list
     * of strings that includes only the strings containing a specific intersection
     * ID.
     *
     * @param route A list of strings representing a route. Each string in the list
     *              represents a segment of
     *              the route, with the format "start_end_intersectionId". For
     *              example, "A_B_1" represents a segment
     *              from point A to point B with an intersection ID of 1.
     * @return The method is returning a List of Strings called "routeFormatted".
     */
    private List<String> formatRoute(List<String> route) {
        List<String> routeFormatted = new ArrayList<>();
        boolean found_string = false;
        for (String str : route) {
            String[] parts = str.split("_");
            if (parts.length == 3) {
                if (found_string && parts[parts.length - 2].endsWith(intersectionVtlDB.getIntersectionVtlId())) {
                    routeFormatted.add(str);
                    break;
                }
                if (parts[parts.length - 1].endsWith(intersectionVtlDB.getIntersectionVtlId())) {
                    routeFormatted.add(str);
                    found_string = true;
                }
            }
        }

        return routeFormatted;
    }

    /**
     * The function changes the traffic light state and stop time for a specific
     * lane in a given route.
     *
     * @param state        The state parameter is a boolean value that represents
     *                     the desired state of the traffic
     *                     light for the lane. If state is true, it means the
     *                     traffic light should be turned on (green), and if
     *                     state is false, it means the traffic light should be
     *                     turned off (red).
     * @param lane         The "lane" parameter is an object of the Lane class. It
     *                     represents a specific lane in an
     *                     intersection.
     * @param current_time The current time in milliseconds.
     */
    public void changeLaneStateForRoute(boolean state, Lane lane, long current_time) {
        for (Connection connection : intersectionVtlDB.getintersectionVtlDB().values()) {
            for (Lane lane_map : connection.getConnections_lanes()) {
                if (lane.getLane_id().equals(lane_map.getLane_id())) {
                    lane.setLane_stop_time_in_seconds(0);
                    lane.changeTrafficLightState(state, current_time);
                }
            }
        }
    }

    /**
     * The function checks if any vehicles have passed an intersection route based
     * on the given vehicle's
     * ID and connection ID.
     *
     * @param vehicle_green The parameter "vehicle_green" is of type CAMExtension,
     *                      which represents a
     *                      message containing information about a vehicle in a VTL
     *                      (Vehicle-to-Infrastructure) system. It
     *                      likely contains properties such as the vehicle's ID and
     *                      the ID of the connection it is currently on.
     * @return The method is returning a boolean value. It returns true if there are
     *         vehicles that have
     *         passed the intersection route and have a different connection ID than
     *         the given vehicle_green,
     *         otherwise it returns false.
     */
    public boolean checkIfVehiclesPassedIntersectionRoute(Vehicle vehicle_green) {
        for (Connection connection : intersectionVtlDB.getintersectionVtlDB().values()) {
            for (Lane lane_map : connection.getConnections_lanes()) {
                if (lane_map.getLane_number_of_vehicles() > 0) {
                    ArrayList<Vehicle> listVehicles = lane_map.getLane_list_of_vehicles();
                    for (Vehicle vehicle : listVehicles) {
                        if (vehicle.getVehicle_id().equals(vehicle_green.getVehicle_id())) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * The function sets the stop time for each lane based on the vehicles
     * approaching an intersection.
     */
    private void setScoreLanes() {
        for (Connection connection : intersectionVtlDB.getintersectionVtlDB().values()) {
            for (Lane lane : connection.getConnections_lanes()) {
                if (intersectionVtlDB.getIntersectionConnections()
                        .contains(lane.getLane_id().substring(0, lane.getLane_id().length() - 2)) &&
                        lane.getLane_number_of_vehicles() > 0) {
                    Vehicle vehicle = lane.getLane_list_of_vehicles().get(lane.getLane_list_of_vehicles().size() - 1);
                    lane.setLane_stop_time_in_seconds(calculateTimeToPassTheIntersection(vehicle));
                }
            }
        }
    }

    /**
     * The function calculates the time it takes for a vehicle to travel a certain
     * distance with a given
     * acceleration and current velocity.
     */
    private int calculateTimeToPassTheIntersection(Vehicle vehicle) {
        double current_velocity = vehicle.getCurrent_velocity();
        int intersection_size = 20;
        double distance_to_travel = vehicle.getDistance_to_intersection() + intersection_size;
        double acceleration;
        acceleration = 1.5;
        double max_speed = 50.0 * 1000.0 / 3600.0; // km/h to m/s
        double time_to_max_speed = (max_speed - current_velocity) / acceleration;
        double distance_travelled_until_max_speed = current_velocity * time_to_max_speed
                + 0.5 * acceleration * time_to_max_speed * time_to_max_speed;
        double remaining_distance = distance_to_travel - distance_travelled_until_max_speed;
        if (remaining_distance <= 0) {
            return (int) time_to_max_speed;
        } else {
            double timeAtMaxSpeed = remaining_distance / max_speed;
            return (int) (time_to_max_speed + timeAtMaxSpeed);
        }
    }

    /**
     * The function retrieves a list of lanes from a database, filters out certain
     * lanes, sorts them based
     * on stop time in descending order, and returns the sorted list.
     *
     * @return The method is returning an ArrayList of Lane objects.
     */
    public ArrayList<Lane> getLaneStopTimesDescending() {
        ArrayList<Lane> lanes_in_descending_time_stop = new ArrayList<>();
        setScoreLanes();
        for (Connection connection : intersectionVtlDB.getintersectionVtlDB().values()) {
            for (Lane lane : connection.getConnections_lanes()) {
                if (intersectionVtlDB.getIntersectionConnections().contains(
                        lane.getLane_id().substring(0, lane.getLane_id().length() - 2)) && lane.getLane_length() > 0) {
                    lanes_in_descending_time_stop.add(lane);
                }
            }
        }
        lanes_in_descending_time_stop.sort(Comparator.comparingDouble(Lane::getLane_stop_time_in_seconds).reversed());
        return lanes_in_descending_time_stop;
    }

    /**
     * The function checks if a lane with a given ID is empty in an intersection.
     *
     * @param lane_id The lane_id parameter is a string that represents the unique
     *                identifier of a lane.
     * @return The method is returning a boolean value. It returns true if there is
     *         a lane with the
     *         specified lane_id and its length is 0, indicating that the lane is
     *         empty. Otherwise, it returns
     *         false.
     */
    public boolean checkIfLaneIsEmpty(String lane_id) {
        for (Connection connection : intersectionVtlDB.getintersectionVtlDB().values()) {
            for (Lane lane : connection.getConnections_lanes()) {
                if (lane.getLane_id().equals(lane_id) && lane.getLane_length() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The function `getLaneGreenForLength` returns the lane with the
     * longest stop time that
     * has not had a green light for at least 2 minutes.
     *
     * @param current_time The current time in milliseconds.
     * @return The method is returning a Lane object.
     */
    public Lane getLaneGreenForLength(long current_time) {
        setScoreLanes();
        Lane lane_green = null;
        for (Connection connection : intersectionVtlDB.getintersectionVtlDB().values()) {
            for (Lane lane : connection.getConnections_lanes()) {
                if (intersectionVtlDB.getIntersectionConnections()
                        .contains(lane.getLane_id().substring(0, lane.getLane_id().length() - 2))
                        && lane.getLane_list_of_vehicles().size() > 0) {
                    if (current_time - lane.getLane_last_time_green_light() > 120000) {
                        lane_green = lane;
                        return lane_green;
                    }
                    if (lane_green == null
                            || lane.getLane_length() > lane_green.getLane_length()) {
                        lane_green = lane;
                    }
                }
            }
        }
        assert lane_green != null;
        return lane_green;
    }
}