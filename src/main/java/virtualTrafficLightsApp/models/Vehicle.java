package virtualTrafficLightsApp.models;

import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;

/**
 * The Vehicle class represents a vehicle with various attributes such as
 * vehicle ID, class, length,
 * width, current velocity, distance to intersection, connection ID, vehicle
 * route, and blinker status.
 */
public class Vehicle {
    private String vehicle_id;
    private String vehicle_class;
    private double vehicle_length;
    private double vehicle_width;
    private double current_velocity;
    private boolean is_vehicle_stopped;
    private double distance_to_intersection;
    private String connection_id;
    private VehicleRoute vehicle_route;
    private boolean is_blinker_right;
    private boolean is_blinker_left;

    public Vehicle(String vehicle_id, String vehicle_class, double vehicle_length, double vehicle_width,
            double current_velocity, boolean is_vehicle_stopped, double distance_to_intersection, String connection_id,
            VehicleRoute vehicle_route, boolean is_blinker_right, boolean is_blinker_left) {
        this.vehicle_id = vehicle_id;
        this.vehicle_class = vehicle_class;
        this.vehicle_length = vehicle_length;
        this.vehicle_width = vehicle_width;
        this.current_velocity = current_velocity;
        this.is_vehicle_stopped = is_vehicle_stopped;
        this.distance_to_intersection = distance_to_intersection;
        this.connection_id = connection_id;
        this.vehicle_route = vehicle_route;
        this.is_blinker_right = is_blinker_right;
        this.is_blinker_left = is_blinker_left;
    }

    public String getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(String vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public String getVehicle_class() {
        return vehicle_class;
    }

    public void setVehicle_class(String vehicle_class) {
        this.vehicle_class = vehicle_class;
    }

    public double getVehicle_length() {
        return vehicle_length;
    }

    public void setVehicle_length(double vehicle_length) {
        this.vehicle_length = vehicle_length;
    }

    public double getVehicle_width() {
        return vehicle_width;
    }

    public void setVehicle_width(double vehicle_width) {
        this.vehicle_width = vehicle_width;
    }

    public double getCurrent_velocity() {
        return current_velocity;
    }

    public void setCurrent_velocity(double current_velocity) {
        this.current_velocity = current_velocity;
    }

    public boolean isIs_vehicle_stopped() {
        return is_vehicle_stopped;
    }

    public void setIs_vehicle_stopped(boolean is_vehicle_stopped) {
        this.is_vehicle_stopped = is_vehicle_stopped;
    }

    public double getDistance_to_intersection() {
        return distance_to_intersection;
    }

    public void setDistance_to_intersection(double distance_to_intersection) {
        this.distance_to_intersection = distance_to_intersection;
    }

    public String getConnection_id() {
        return connection_id;
    }

    public void setConnection_id(String connection_id) {
        this.connection_id = connection_id;
    }

    public VehicleRoute getVehicle_route() {
        return vehicle_route;
    }

    public void setVehicle_route(VehicleRoute vehicle_route) {
        this.vehicle_route = vehicle_route;
    }

    public boolean isIs_blinker_right() {
        return is_blinker_right;
    }

    public void setIs_blinker_right(boolean is_blinker_right) {
        this.is_blinker_right = is_blinker_right;
    }

    public boolean isIs_blinker_left() {
        return is_blinker_left;
    }

    public void setIs_blinker_left(boolean is_blinker_left) {
        this.is_blinker_left = is_blinker_left;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "vehicle_id='" + vehicle_id + '\'' +
                /*
                 * ", vehicle_class='" + vehicle_class + '\'' +
                 * ", vehicle_length=" + vehicle_length +
                 * ", vehicle_width=" + vehicle_width +
                 */
                ", current_velocity=" + current_velocity +
                /* ", is_vehicle_stopped=" + is_vehicle_stopped + */
                ", distance_to_intersection=" + distance_to_intersection +
                /*
                 * ", connection_id='" + connection_id + '\'' +
                 * ", vehicle_route=" + vehicle_route +
                 * ", is_blinker_right=" + is_blinker_right +
                 * ", is_blinker_left=" + is_blinker_left +
                 */
                '}';
    }
}
