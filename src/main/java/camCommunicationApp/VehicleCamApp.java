package camCommunicationApp;

import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.AdHocModuleConfiguration;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.enums.AdHocChannel;
import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.objects.v2x.etsi.Cam;
import org.eclipse.mosaic.lib.objects.v2x.etsi.CamContent;
import org.eclipse.mosaic.lib.objects.v2x.etsi.cam.AwarenessData;
import org.eclipse.mosaic.lib.objects.v2x.etsi.cam.VehicleAwarenessData;
import org.eclipse.mosaic.lib.util.SerializationUtils;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;
import virtualTrafficLightsApp.models.CAMExtension;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

/**
 * The `VehicleCamApp` class is a Java application that sends and receives CAM
 * (Cooperative Awareness
 * Message) messages in a vehicle operating system.
 */
public class VehicleCamApp extends AbstractApplication<VehicleOperatingSystem> implements CommunicationApplication {

    private static final SerializationUtils<CAMExtension> DEFAULT_OBJECT_SERIALIZATION = new SerializationUtils<>();

    /**
     * The function `sendCam()` sends a CAM (Cooperative Awareness Message) using
     * the ad hoc module.
     */
    private void sendCam() {
        // getLog().infoSimTime(this, "Sending CAM");
        getOs().getAdHocModule().sendCam();
    }

    /**
     * The function checks if the received message is a CAM message and logs the
     * appropriate message based
     * on the type of message received.
     * 
     * @param receivedV2xMessage The parameter `receivedV2xMessage` is an object of
     *                           type
     *                           `ReceivedV2xMessage`. It represents a received V2X
     *                           message.
     */
    @Override
    public void onMessageReceived(ReceivedV2xMessage receivedV2xMessage) {
        V2xMessage msg = receivedV2xMessage.getMessage();
        if (msg instanceof Cam) {
            // getLog().infoSimTime(this, "CAM message arrived {}", msg);
        } else {
            // getLog().infoSimTime(this, "Arrived message was not a CAM, but a {} msg from
            // {}", msg.getSimpleClassName(), msg.getRouting().getSource().getSourceName());
        }
    }

    @Override
    public void onAcknowledgementReceived(ReceivedAcknowledgement receivedAcknowledgement) {

    }

    /**
     * The function `onCamBuilding` creates a `CAMExtension` object and serializes
     * it into a byte array,
     * which is then added as a user tagged value to a `CamBuilder` object.
     * 
     * @param camBuilder The `camBuilder` parameter is an instance of the
     *                   `CamBuilder` class. It is used to
     *                   build a Cooperative Awareness Message (CAM) for a vehicle.
     */
    @Override
    public void onCamBuilding(CamBuilder camBuilder) {
        CAMExtension cam_extension = new CAMExtension(
                getOs().getRoadPosition().getConnection().getId()
                        + "_" + getOs().getRoadPosition().getLaneIndex(),
                getOs().getInitialVehicleType().getName(),
                getOs().getNavigationModule().getCurrentRoute(),
                Objects.requireNonNull(getOs().getVehicleData()).getVehicleSignals().isBlinkerRight(),
                Objects.requireNonNull(getOs().getVehicleData()).getVehicleSignals().isBlinkerLeft());
        try {
            byte[] byteArray = DEFAULT_OBJECT_SERIALIZATION.toBytes(cam_extension);
            camBuilder.userTaggedValue(byteArray);
        } catch (IOException ex) {
            getLog().error("Error during a serialization.", ex);
        }
    }

    @Override
    public void onMessageTransmitted(V2xMessageTransmission v2xMessageTransmission) {

    }

    /**
     * The `onStartup` function initializes the application, enables the AdHoc
     * module with specific
     * configuration, activates the WLAN module, and adds an event to the event
     * manager.
     */
    @Override
    public void onStartup() {
        this.getLog().infoSimTime(this, "Initialize VehicleCamApp application");
        this.getOs().getAdHocModule()
                .enable((new AdHocModuleConfiguration()).addRadio().channel(AdHocChannel.CCH).distance(150).create());
        this.getLog().infoSimTime(this, "Activated WLAN Module");
        getOs().getEventManager().addEvent(getOs().getSimulationTime() + (TIME.SECOND) / 10, this);
        this.getLog().infoSimTime(this, "Added an event to send CAM messages after a certain time");
    }

    @Override
    public void onShutdown() {

    }

    @Override
    public void processEvent(Event event) {
        this.sendCam();
        getOs().getEventManager().addEvent(getOs().getSimulationTime() + (TIME.SECOND) / 10, this);
    }
}
