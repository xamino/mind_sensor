package de.uulm.mi.mind.sensor;

import de.uulm.mi.mind.json.JsonConverter;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.WifiSensor;

/**
 * @author Tamino Hartmann
 *         This class runs as a thread continously. It connects to the server and periodically tells it, which devices it can
 *         sense with which strength.
 */
public class WifiSense implements Runnable {

    /**
     * Instance of log.
     */
    private Messenger log;
    /**
     * Instance of JsonConverter for server communication.
     */
    private JsonConverter json;
    /**
     * Time between scans.
     */
    private final long SLEEP_TIME;

    /**
     * Constructor. Sets the sleep time, gets important instances, and prepares the connection to the server.
     *
     * @param sleepTime The time in ms that the the thread sleeps between scans.
     */
    public WifiSense(long sleepTime) {
        log = Messenger.getInstance();
        json = JsonConverter.getInstance();
        // todo register json types
        json.registerType(WifiSensor.class);
        // set defaults
        SLEEP_TIME = sleepTime;
    }

    @Override
    public void run() {
        while (true) {
            // todo check session and if not valid, try login
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
