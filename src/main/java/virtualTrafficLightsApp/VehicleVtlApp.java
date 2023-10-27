package virtualTrafficLightsApp;

import org.eclipse.mosaic.fed.application.ambassador.navigation.RoadPositionFactory;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.objects.road.IRoadPosition;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.objects.v2x.etsi.Spatm;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Objects;

import static org.eclipse.mosaic.lib.enums.VehicleStopMode.STOP;

/**
 * The `VehicleVtlApp` class is a Java application that handles V2X messages
 * related to virtual traffic lights
 * and controls the behavior of a vehicle based on these messages.
 */
public class VehicleVtlApp extends AbstractApplication<VehicleOperatingSystem>
        implements VehicleApplication, CommunicationApplication {

    // These are private instance variables of the `VehicleVtlApp` class in the Java
    // code.
    private boolean stop_on_vtl = false;
    private long time_rec_vtl_inf;
    private long time_to_stop;

    /**
     * The `onMessageReceived` function processes received V2x messages,
     * specifically handling SPATM
     * messages related to traffic lights.
     * 
     * @param receivedV2xMessage The receivedV2xMessage parameter is an object of
     *                           type ReceivedV2xMessage,
     *                           which represents a received V2X message.
     */
    @Override
    public void onMessageReceived(ReceivedV2xMessage receivedV2xMessage) {
        V2xMessage msg = receivedV2xMessage.getMessage();
        if (msg instanceof Spatm) {
            String intersection_id_of_msg = getOs().getNavigationModule()
                    .getClosestNode(((Spatm) msg).getSenderPosition()).getId();
            if (check_intersection_to_stop(intersection_id_of_msg)) {
                if ((((Spatm) msg).isPhaseGreen())) {
                    getLog().info("TA verde");
                    time_rec_vtl_inf = ((Spatm) msg).getTime();
                    goAtTheIntersection();
                }
                if ((((Spatm) msg).isPhaseRed())) {
                    getLog().info("TA red");
                    time_rec_vtl_inf = ((Spatm) msg).getTime();
                    time_to_stop = ((Spatm) msg).getPhaseDurationRed();
                    stopAtTheIntersection();
                    stop_on_vtl = true;
                }
                if ((((Spatm) msg).isPhaseYellow())) {
                    getLog().info("TA yellow");
                    time_rec_vtl_inf = ((Spatm) msg).getTime();
                    slowDownAtTheIntersection(((Spatm) msg).getPhaseDurationYellow());
                }
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

    /**
     * The function `goAtTrafficLight()` checks if the vehicle is stopped and
     * resumes it if it is,
     * if not stopped the vehicle will continue to move.
     */
    private void goAtTheIntersection() {
        if (Objects.requireNonNull(getOs().getVehicleData()).isStopped()) {
            getOs().resume();
        } else {
            IRoadPosition roadPosition = RoadPositionFactory.createAtEndOfRoute(
                    Collections.singletonList(getOs().getRoadPosition().getConnection().getId()), 0);
            getOs().stop(roadPosition, STOP, 0L);
            stop_on_vtl = false;
        }
    }

    /**
     * The function `stopAtTrafficLight()` stops the vehicle at a traffic light for
     * a specified amount of
     * time.
     */
    private void stopAtTheIntersection() {
        IRoadPosition roadPosition = RoadPositionFactory.createAtEndOfRoute(
                Collections.singletonList(getOs().getRoadPosition().getConnection().getId()), 0);
        getOs().stop(roadPosition, STOP, time_to_stop * TIME.SECOND);
    }

    /**
     * The function slows down the operating system at the intersection for a
     * specified amount of time.
     * 
     * @param time_to_slow_down The time in milliseconds that the vehicle should
     *                          take to slow down at the
     *                          intersection.
     */
    private void slowDownAtTheIntersection(long time_to_slow_down) {
        getOs().slowDown(30 / 3.6f, time_to_slow_down);
    }

    /**
     * The function checks if the current road connection's last part matches a
     * given intersection ID.
     * 
     * @return The method is returning a boolean value.
     */
    private boolean check_intersection_to_stop(String intersection_id_of_msg) {
        String[] current_connection = getOs().getRoadPosition().getConnection().getId().split("_");
        if (intersection_id_of_msg != null) {
            if (current_connection.length >= 1) {
                String lastPart = current_connection[current_connection.length - 1];
                return lastPart.equals(intersection_id_of_msg);
            }
        }
        return false;
    }

    /**
     * The function checks if the red light time has passed and then calls
     * `goAtTheIntersection()` if it is.
     * 
     * @param vehicleData  This parameter is of type VehicleData and is nullable,
     *                     meaning it can be null. It
     *                     represents the previous state of the vehicle data.
     * @param vehicleData1 The parameter vehicleData1 is of type VehicleData and is
     *                     annotated with the
     * @Nonnull annotation, which means it cannot be null.
     */
    @Override
    public void onVehicleUpdated(@Nullable VehicleData vehicleData, @Nonnull VehicleData vehicleData1) {
        if (stop_on_vtl) {
            if (getOs().getSimulationTimeMs() - time_rec_vtl_inf > time_to_stop) {
                goAtTheIntersection();
            }
        }
    }

    @Override
    public void onStartup() {
        getLog().info("Initialize VehicleVtlApp application");
        getLog().info(getOs().getInitialVehicleType().toString());
    }

    @Override
    public void onShutdown() {

    }

    @Override
    public void processEvent(Event event) throws Exception {

    }
}