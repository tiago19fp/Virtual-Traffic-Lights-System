package virtualTrafficLightsApp.models;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * The Lane class represents a lane, with properties such as lane ID, state (go
 * or
 * stop), number of vehicles, length, last time the green light was on, stop
 * time in seconds, and a
 * list of vehicles in the lane.
 */
public class Lane {
    private String lane_id;
    private boolean lane_state_go;
    private int lane_number_of_vehicles;
    private double lane_length;
    private long lane_last_time_green_light;
    private double lane_stop_time_in_seconds;
    private ArrayList<Vehicle> lane_list_of_vehicles;

    public Lane(String lane_id, boolean lane_state_go, int lane_number_of_vehicles, double lane_length,
            long lane_last_time_green_light, double lane_stop_time_in_seconds,
            ArrayList<Vehicle> lane_list_of_vehicles) {
        this.lane_id = lane_id;
        this.lane_state_go = lane_state_go;
        this.lane_number_of_vehicles = lane_number_of_vehicles;
        this.lane_length = lane_length;
        this.lane_last_time_green_light = lane_last_time_green_light;
        this.lane_stop_time_in_seconds = lane_stop_time_in_seconds;
        this.lane_list_of_vehicles = lane_list_of_vehicles;
    }

    public String getLane_id() {
        return lane_id;
    }

    public void setLane_id(String lane_id) {
        this.lane_id = lane_id;
    }

    public boolean isLane_state_go() {
        return lane_state_go;
    }

    public void setLane_state_go(boolean lane_state_go) {
        this.lane_state_go = lane_state_go;
    }

    public int getLane_number_of_vehicles() {
        return lane_number_of_vehicles;
    }

    public void setLane_number_of_vehicles() {
        this.lane_number_of_vehicles = this.lane_list_of_vehicles.size();
    }

    public double getLane_length() {
        return lane_length;
    }

    public void setLane_length() {
        this.lane_length = 0;
        for (Vehicle vehicle : this.lane_list_of_vehicles) {
            this.lane_length += vehicle.getVehicle_length();
        }
    }

    public long getLane_last_time_green_light() {
        return lane_last_time_green_light;
    }

    public void setLane_last_time_green_light(long lane_last_time_green_light) {
        this.lane_last_time_green_light = lane_last_time_green_light;
    }

    public double getLane_stop_time_in_seconds() {
        return lane_stop_time_in_seconds;
    }

    public void setLane_stop_time_in_seconds(double lane_stop_time_in_seconds) {
        this.lane_stop_time_in_seconds = lane_stop_time_in_seconds;
    }

    public ArrayList<Vehicle> getLane_list_of_vehicles() {
        return lane_list_of_vehicles;
    }

    public void setLane_list_of_vehicles(ArrayList<Vehicle> lane_list_of_vehicles) {
        this.lane_list_of_vehicles = lane_list_of_vehicles;
    }

    public void add_new_vehicle(Vehicle new_vehicle) {
        lane_list_of_vehicles.add(new_vehicle);
        setLane_number_of_vehicles();
        setLane_length();
        lane_list_of_vehicles.sort(Comparator.comparingDouble(Vehicle::getDistance_to_intersection));
    }

    public void changeTrafficLightState(boolean state, long time) {
        this.setLane_state_go(state);
        if (state) {
            setLane_last_time_green_light(time);
        }
    }

    @Override
    public String toString() {
        return "LaneVtlState{" +
                "lane_id='" + lane_id + '\'' + "\n" +
                ", lane_state_go=" + lane_state_go + "\n" +
                // ", lane_number_of_vehicles=" + lane_number_of_vehicles + "\n" +
                // ", lane_length=" + lane_length + "\n" +
                // ", lane_last_time_green_light=" + lane_last_time_green_light + "\n" +
                ", lane_stop_time_in_seconds=" + lane_stop_time_in_seconds + "\n" +
                ", lane_list_of_vehicles=" + lane_list_of_vehicles + "\n" +
                '}' + "\n";
    }
}