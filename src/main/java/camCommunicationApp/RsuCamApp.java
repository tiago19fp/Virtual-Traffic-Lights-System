package camCommunicationApp;

import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.AdHocModuleConfiguration;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.os.RoadSideUnitOperatingSystem;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.enums.AdHocChannel;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.lib.objects.v2x.etsi.Cam;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;

/**
 * This is a Java class for a roadside Unit (RSU) Communication Application that
 * sends and receives
 * CAM messages using an AdHoc module.
 */
public class RsuCamApp extends AbstractApplication<RoadSideUnitOperatingSystem> implements CommunicationApplication {

    /**
     * The function sends an ad hoc broadcast by adding an event to the event
     * manager and then calling the
     * sendCam() method of the AdHocModule.
     */
    public void sendAdHocBroadcast() {
        this.getOs().getEventManager().addEvent(this.getOs().getSimulationTime() + TIME.SECOND, this);
        // this.getLog().infoSimTime(this, "Sending out AdHoc broadcast");
        this.getOs().getAdHocModule().sendCam();
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

    @Override
    public void onCamBuilding(CamBuilder camBuilder) {

    }

    @Override
    public void onMessageTransmitted(V2xMessageTransmission v2xMessageTransmission) {

    }

    /**
     * The onStartup function initializes the application, enables the AdHocModule
     * with a specific
     * configuration, activates the WLAN Module, and sends an AdHoc broadcast.
     */
    @Override
    public void onStartup() {
        this.getLog().infoSimTime(this, "Initialize RsuCamApp application");
        this.getOs().getAdHocModule()
                .enable((new AdHocModuleConfiguration()).addRadio().channel(AdHocChannel.CCH).distance(150).create());
        this.getLog().infoSimTime(this, "Activated WLAN Module");
        this.sendAdHocBroadcast();
    }

    @Override
    public void onShutdown() {

    }

    @Override
    public void processEvent(Event event) throws Exception {
        this.sendAdHocBroadcast();
    }
}
