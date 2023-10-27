package virtualTrafficLightsApp.models;

import org.eclipse.mosaic.lib.objects.vehicle.VehicleRoute;

import java.io.*;

/**
 * The CAMExtension class represents an extension of a CAM (Cooperative
 * Awareness Message) in a
 * vehicle-to-vehicle communication system, containing information about the
 * vehicle's connection,
 * class, route, and blinker status.
 */
public class CAMExtension implements Serializable {
    private String connection_id;
    private String vehicle_class;
    private VehicleRoute vehicle_route;
    private boolean is_blinker_right;
    private boolean is_blinker_left;

    public CAMExtension(String connection_id, String vehicle_class, VehicleRoute vehicle_route,
            boolean is_blinker_right, boolean is_blinker_left) {
        this.connection_id = connection_id;
        this.vehicle_class = vehicle_class;
        this.vehicle_route = vehicle_route;
        this.is_blinker_right = is_blinker_right;
        this.is_blinker_left = is_blinker_left;
    }

    public String getConnection_id() {
        return connection_id;
    }

    public void setConnection_id(String connection_id) {
        this.connection_id = connection_id;
    }

    public String getVehicle_class() {
        return vehicle_class;
    }

    public void setVehicle_class(String vehicle_class) {
        this.vehicle_class = vehicle_class;
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
}
