package virtualTrafficLightsApp;

import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.os.RoadSideUnitOperatingSystem;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.enums.AdHocChannel;
import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.objects.v2x.etsi.Cam;
import org.eclipse.mosaic.lib.objects.v2x.etsi.CamContent;
import org.eclipse.mosaic.lib.objects.v2x.etsi.Spatm;
import org.eclipse.mosaic.lib.objects.v2x.etsi.SpatmContent;
import org.eclipse.mosaic.lib.objects.v2x.etsi.cam.VehicleAwarenessData;
import org.eclipse.mosaic.lib.util.SerializationUtils;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;
import virtualTrafficLightsApp.controllers.IntersectionVtlController;
import virtualTrafficLightsApp.models.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

/**
 * The RsuVTLApp class is a Java application that simulates a road traffic
 * system and controls traffic
 * lights based on the traffic state of the roads.
 */

public class RsuVtlApp extends AbstractApplication<RoadSideUnitOperatingSystem> implements CommunicationApplication {

    public static final SerializationUtils<CAMExtension> DEFAULT_OBJECT_SERIALIZATION = new SerializationUtils<>();

    IntersectionVtlController intersectionVtlController;

    //////////////////// routes
    Lane current_lane_green_route = null;
    Vehicle current_vehicle_green_route;
    boolean chose_another_vehicle_to_go_route = true;
    long last_time_vtl_msg_send_route;
    ////////////////// static
    int red_light_duration_in_s = 60;
    int current_lane_green_index = 0;
    ///////////////// lengthWithoutCycle
    Lane current_lane_green;
    //////////////// all
    private long red_light_duration_for_msg_vtl_in_ms = 0;
    private long timestamp_creation_of_msg_vtl_in_ms = 0;
    private boolean calculate_new_lanes_order = false;
    long timestamp_last_sent_msg_vtl_in_ms;

    public RsuVtlApp() {

    }

    /**
     * The function sends a message to nearby vehicles.
     */
    public void sendYellow() {
        for (Connection connection : intersectionVtlController.getIntersectionVtlDB().getintersectionVtlDB().values()) {
            if (intersectionVtlController.getIntersectionVtlDB().getIntersectionConnections()
                    .contains(connection.getConnection_id())) {
                for (Lane lane_map : connection.getConnections_lanes()) {
                    if (lane_map.isLane_state_go()) {
                        for (Vehicle vehicle : lane_map.getLane_list_of_vehicles()) {
                            MessageRouting routing = getOs().getAdHocModule().createMessageRouting()
                                    .viaChannel(AdHocChannel.CCH).topoCast(vehicle.getVehicle_id(), 1);
                            SpatmContent spatmContent = new SpatmContent(
                                    getOs().getSimulationTimeMs(),
                                    getOs().getPosition(),
                                    false,
                                    true,
                                    false,
                                    0,
                                    5000,
                                    0,
                                    0);
                            Spatm spatm = new Spatm(routing, spatmContent, 200);
                            getOs().getAdHocModule().sendV2xMessage(spatm);
                        }
                    }
                }
            }
        }
    }

    /**
     * The function sends traffic information to vehicles in a given intersection.
     *
     * @param phase_time_vtl_ms The `traffic_state` parameter represents the current
     *                          state of traffic. It is a
     *                          long value that indicates the level of traffic
     *                          congestion or flow.
     */
    public void sendTrafficInformation(long phase_time_vtl_ms) {
        for (Connection connection : intersectionVtlController.getIntersectionVtlDB().getintersectionVtlDB().values()) {
            if (intersectionVtlController.getIntersectionVtlDB().getIntersectionConnections()
                    .contains(connection.getConnection_id())) {
                for (Lane lane : connection.getConnections_lanes()) {
                    if (lane.isLane_state_go()) {
                        for (Vehicle vehicle : lane.getLane_list_of_vehicles()) {
                            MessageRouting routing = getOs().getAdHocModule().createMessageRouting()
                                    .viaChannel(AdHocChannel.CCH).topoCast(vehicle.getVehicle_id(), 1);
                            SpatmContent spatmContent = new SpatmContent(
                                    timestamp_creation_of_msg_vtl_in_ms,
                                    getOs().getPosition(),
                                    false,
                                    false,
                                    true,
                                    0,
                                    0,
                                    (long) phase_time_vtl_ms,
                                    (long) phase_time_vtl_ms
                                            - (getOs().getSimulationTimeMs() - timestamp_creation_of_msg_vtl_in_ms));
                            Spatm spatm = new Spatm(routing, spatmContent, 200);
                            getOs().getAdHocModule().sendV2xMessage(spatm);
                        }
                    }
                    if (!lane.isLane_state_go()) {
                        for (Vehicle vehicle : lane.getLane_list_of_vehicles()) {
                            MessageRouting routing = getOs().getAdHocModule().createMessageRouting()
                                    .viaChannel(AdHocChannel.CCH).topoCast(vehicle.getVehicle_id(), 10);
                            SpatmContent spatmContent = new SpatmContent(
                                    timestamp_creation_of_msg_vtl_in_ms,
                                    getOs().getPosition(),
                                    true,
                                    false,
                                    false,
                                    (long) phase_time_vtl_ms,
                                    0,
                                    0,
                                    (long) phase_time_vtl_ms
                                            - (getOs().getSimulationTimeMs() - timestamp_creation_of_msg_vtl_in_ms));
                            Spatm spatm = new Spatm(routing, spatmContent, 200);
                            getOs().getAdHocModule().sendV2xMessage(spatm);
                        }
                    }
                }
            }
        }
    }

    /**
     * This function sends a green ligth to a vehicle.
     * 
     * @param vehicle The `vehicle` parameter is an object of the `Vehicle` class.
     */
    public void sendRoute(Vehicle vehicle) {
        MessageRouting routing = getOs().getAdHocModule().createMessageRouting()
                .viaChannel(AdHocChannel.CCH).topoCast(vehicle.getVehicle_id(), 1);
        SpatmContent spatmContent = new SpatmContent(
                getOs().getSimulationTimeMs(),
                getOs().getPosition(),
                false,
                false,
                true,
                (long) 5,
                0,
                0,
                (long) 5 - (getOs().getSimulationTimeMs() - getOs().getSimulationTimeMs()));
        Spatm spatm = new Spatm(routing, spatmContent, 200);
        getOs().getAdHocModule().sendV2xMessage(spatm);
    }

    /**
     * This function controls the virtual traffic lights assigning 60 seconds to
     * each road.
     */
    public void changeTrafficLightStatic() {
        if (intersectionVtlController.getIntersectionVtlDB().getIntersectionConnections().size() >= 2) {
            if (getOs().getSimulationTimeMs() - timestamp_creation_of_msg_vtl_in_ms > (red_light_duration_in_s - 1)
                    * 1000L) {
                getLog().infoSimTime(this, "Novo VTL");
                timestamp_creation_of_msg_vtl_in_ms = getOs().getSimulationTimeMs();
                red_light_duration_for_msg_vtl_in_ms = intersectionVtlController
                        .changeTrafficLightsStatic(timestamp_creation_of_msg_vtl_in_ms, red_light_duration_in_s);
                sendTrafficInformation(red_light_duration_for_msg_vtl_in_ms);
            } else if (red_light_duration_for_msg_vtl_in_ms > 0
                    && (getOs().getSimulationTimeMs() - timestamp_last_sent_msg_vtl_in_ms >= 100)) {
                timestamp_last_sent_msg_vtl_in_ms = getOs().getSimulationTimeMs();
                sendTrafficInformation(red_light_duration_for_msg_vtl_in_ms);
            } else if (getOs().getSimulationTimeMs()
                    - timestamp_creation_of_msg_vtl_in_ms > (red_light_duration_in_s - 5) * 1000L) {
                sendYellow();
            }
        }
    }

    /**
     * The function changes the virtual traffic lights based on the length of the
     * lanes.
     */
    public void changeTrafficLightBasedOnLength() {
        if (!calculate_new_lanes_order
                && intersectionVtlController.getIntersectionVtlDB().getIntersectionConnections().size() >= 2) {
            current_lane_green = intersectionVtlController.getLaneGreenForLength(getOs().getSimulationTimeMs());
            if (current_lane_green != null) {
                red_light_duration_in_s = (int) current_lane_green.getLane_stop_time_in_seconds();
                timestamp_creation_of_msg_vtl_in_ms = getOs().getSimulationTimeMs();
                red_light_duration_for_msg_vtl_in_ms = intersectionVtlController
                        .changeTrafficLightsLength(current_lane_green.getLane_id(), timestamp_creation_of_msg_vtl_in_ms,
                                red_light_duration_in_s);
                calculate_new_lanes_order = true;
            } else {
                red_light_duration_for_msg_vtl_in_ms = 0;
            }
        }
        if (current_lane_green != null && red_light_duration_for_msg_vtl_in_ms > 0
                && (getOs().getSimulationTimeMs() - timestamp_last_sent_msg_vtl_in_ms >= 100)) {
            timestamp_last_sent_msg_vtl_in_ms = getOs().getSimulationTimeMs();
            sendTrafficInformation(red_light_duration_for_msg_vtl_in_ms);
        }
        if (current_lane_green != null && red_light_duration_for_msg_vtl_in_ms > 0) {
            if (intersectionVtlController.checkIfLaneIsEmpty(current_lane_green.getLane_id())) {
                calculate_new_lanes_order = false;
            }
        }
        if (getOs().getSimulationTimeMs() - timestamp_creation_of_msg_vtl_in_ms > (red_light_duration_in_s - 5)
                * 1000L) {
            sendYellow();
        }
        if (getOs().getSimulationTimeMs() - timestamp_creation_of_msg_vtl_in_ms > (red_light_duration_in_s - 0.8)
                * 1000L) {
            calculate_new_lanes_order = false;
        }
    }

    /**
     * The function changes the virtual traffic lights based on the length of the
     * lanes and the routes of vehicles.
     */
    public void changeTrafficLightBasedOnRoutes() {
        if (intersectionVtlController.getIntersectionVtlDB().getIntersectionConnections().size() >= 2) {
            if (!calculate_new_lanes_order) {
                if (current_lane_green_route != null) {
                    intersectionVtlController.changeLaneStateForRoute(false, current_lane_green_route,
                            getOs().getSimulationTimeMs());
                } else {
                    chose_another_vehicle_to_go_route = true;
                }
                current_lane_green = intersectionVtlController
                        .getLaneGreenForLength(getOs().getSimulationTimeMs());
                if (current_lane_green != null && current_lane_green.getLane_stop_time_in_seconds() > 0) {
                    red_light_duration_in_s = (int) current_lane_green.getLane_stop_time_in_seconds();
                    timestamp_creation_of_msg_vtl_in_ms = getOs().getSimulationTimeMs();
                    red_light_duration_for_msg_vtl_in_ms = intersectionVtlController.changeTrafficLightsLength(
                            current_lane_green.getLane_id(), timestamp_creation_of_msg_vtl_in_ms,
                            red_light_duration_in_s) * 1000;
                    calculate_new_lanes_order = true;
                    chose_another_vehicle_to_go_route = true;
                } else {
                    red_light_duration_for_msg_vtl_in_ms = 0;
                }
            }
        }
        if (current_lane_green != null && red_light_duration_for_msg_vtl_in_ms > 0) {
            if (intersectionVtlController.checkIfLaneIsEmpty(current_lane_green.getLane_id())) {
                calculate_new_lanes_order = false;
            }
        }
        if (current_lane_green_route != null) {
            if (!chose_another_vehicle_to_go_route && current_lane_green_route.getLane_number_of_vehicles() > 0
                    && intersectionVtlController.checkIfVehiclesPassedIntersectionRoute(current_vehicle_green_route)) {
                last_time_vtl_msg_send_route = getOs().getSimulationTimeMs();
                chose_another_vehicle_to_go_route = true;
                current_lane_green_route = null;
                current_vehicle_green_route = null;
            }
        }
        if (chose_another_vehicle_to_go_route) {
            current_lane_green_route = intersectionVtlController.safeToGoBaseOnRoutes();
            if (current_lane_green_route != null) {
                current_vehicle_green_route = current_lane_green_route.getLane_list_of_vehicles().get(0);
                getLog().info(current_vehicle_green_route.toString());
                sendRoute(current_vehicle_green_route);
                chose_another_vehicle_to_go_route = false;
            }
        }
        if (current_lane_green != null && red_light_duration_for_msg_vtl_in_ms > 0
                && (getOs().getSimulationTimeMs() - timestamp_last_sent_msg_vtl_in_ms >= 100)) {
            timestamp_last_sent_msg_vtl_in_ms = getOs().getSimulationTimeMs();
            if (current_vehicle_green_route != null) {
                sendRoute(current_vehicle_green_route);
            }
            sendTrafficInformation(red_light_duration_for_msg_vtl_in_ms);
            if ((getOs().getSimulationTimeMs() - timestamp_creation_of_msg_vtl_in_ms > (red_light_duration_in_s - 5)
                    * 1000L)) {
                sendYellow();
            }
        }
        if (getOs().getSimulationTimeMs() - timestamp_creation_of_msg_vtl_in_ms > (red_light_duration_in_s - 0.8)
                * 1000L) {
            calculate_new_lanes_order = false;
            chose_another_vehicle_to_go_route = true;
            current_lane_green_route = null;
            current_vehicle_green_route = null;
        }
    }

    /**
     * The function "changeLightsEvent" is used to change the traffic lights based
     * on different criteria.
     */
    public void changeLightsEvent() {
        // this.getLog().infoSimTime(this, "Change traffic light");
        // this.changeTrafficLightStatic();
        // this.changeTrafficLightBasedOnLength();
        this.changeTrafficLightBasedOnRoutes();
    }

    /**
     * The `onStartup` function initializes the RsuVtlApp application, sets up an
     * intersection VTL
     * controller, and adds an event to control the traffic.
     */
    public void onStartup() {
        this.getLog().infoSimTime(this, "Initialize RsuVtlApp application");
        intersectionVtlController = new IntersectionVtlController(new IntersectionVtlDB(new HashMap<>(),
                getOs().getRoutingModule().getClosestNode(getOs().getPosition()).getId()));
        timestamp_last_sent_msg_vtl_in_ms = getOs().getSimulationTimeMs();
        this.getLog().infoSimTime(this, "Added an event to control the traffic");
        this.getOs().getEventManager().addEvent(this.getOs().getSimulationTime() + (TIME.SECOND) / 10, this);
    }

    /**
     * The function `onMessageReceived` processes a received V2x message, extracts
     * information from the
     * message, decodes the payload, and creates a new `Vehicle` object to be added
     * to the intersection's
     * database.
     * 
     * @param receivedV2xMessage The receivedV2xMessage parameter is an object of
     *                           type ReceivedV2xMessage.
     *                           It represents a received V2X message.
     */
    public void onMessageReceived(ReceivedV2xMessage receivedV2xMessage) {
        V2xMessage msg = receivedV2xMessage.getMessage();
        if (msg instanceof Cam) {
            try {
                CAMExtension CAMExtension = DEFAULT_OBJECT_SERIALIZATION.fromBytes(
                        Objects.requireNonNull(((Cam) msg).getUserTaggedValue()), this.getClass().getClassLoader());
                EncodedPayload encodedMessage = msg.getPayLoad();
                CamContent decodedCam = encodedMessage.decodePayload();
                VehicleAwarenessData decodedAwarenessData = (VehicleAwarenessData) decodedCam.getAwarenessData();
                boolean is_stopped = (decodedAwarenessData.getSpeed() == 0);
                Vehicle new_vehicle = new Vehicle(
                        msg.getRouting().getSource().getSourceName(),
                        CAMExtension.getVehicle_class(),
                        decodedAwarenessData.getLength(),
                        decodedAwarenessData.getWidth(),
                        decodedAwarenessData.getSpeed(),
                        is_stopped,
                        ((Cam) msg).getPosition().distanceTo(getOs().getPosition()),
                        CAMExtension.getConnection_id(),
                        CAMExtension.getVehicle_route(),
                        CAMExtension.isIs_blinker_right(),
                        CAMExtension.isIs_blinker_left());
                intersectionVtlController.getIntersectionVtlDB().addVehicleToIntersection(new_vehicle);
            } catch (ClassNotFoundException | IOException e) {
                getLog().error("An error occurred", e);
            }
        }
    }

    @Override
    public void onAcknowledgementReceived(ReceivedAcknowledgement receivedAcknowledgement) {

    }

    @Override
    public void onCamBuilding(CamBuilder camBuilder) {

    }

    @Override
    public void onMessageTransmitted(V2xMessageTransmission v2xMessageTransmission) {

    }

    public void onShutdown() {
        this.getLog().infoSimTime(this, "Shutdown application");
    }

    /**
     * The processEvent function changes the lights event and adds a new event to
     * the event manager.
     * 
     * @param event The `event` parameter is an object of type `Event`.
     */
    public void processEvent(Event event) {
        this.changeLightsEvent();
        this.getOs().getEventManager().addEvent(this.getOs().getSimulationTime() + (TIME.SECOND) / 100, this);
    }
}