package de.uulm.mi.mind.sensor;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.WifiSensor;
import de.uulm.mi.mind.objects.enums.API;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.remote.RemoteConnection;

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
     * Time between scans.
     */
    private final long SLEEP_TIME;
    /**
     * Tag for logging with log.
     */
    private final String TAG = "WifiSense";

    private RemoteConnection connection;
    private WifiSensor sensorUser;
    private String session;

    /**
     * Constructor. Sets the sleep time, gets important instances, and prepares the connection to the server.
     *
     * @param ip
     * @param name
     * @param password
     * @param sleepTime
     */
    public WifiSense(String ip, String name, String password, int sleepTime) {
        // get instances
        log = Messenger.getInstance();
        // set vars
        SLEEP_TIME = sleepTime * 1000;
        connection = new RemoteConnection(ip);
        sensorUser = new WifiSensor(name, password);

        log.log(TAG, "Created.");
    }

    /**
     * Executing function that runs every SLEEP_TIME ms. Contains all the important logic.
     */
    @Override
    public void run() {
        while (true) {
            //check session
            if (!checkSession()) {
                log.error(TAG, "Session could not be established!");
                break;
            }
            log.log(TAG, "Beginning scan...");
            // todo implement
            log.log(TAG, "Finished scan.");
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                // todo do the correct thing here (which is... ?)
                log.error(TAG, "Interrupted!!!");
                e.printStackTrace();
            }
        }
        log.log(TAG, "Terminated.");
    }

    /**
     * This method makes sure that the sensor is logged in. If not, it will try to get a session.
     */
    private boolean checkSession() {
        // start with check
        Data data = connection.runTask(API.CHECK, null, session);
        // if okay, return true
        if (data instanceof Success) {
            return true;
        }
        // otherwise try login
        data = connection.runTask(API.LOGIN, sensorUser, null);
        if (data instanceof Success) {
            log.log(TAG, "Successfully logged in.");
            session = ((Success) data).getDescription();
            return true;
        }
        return false;
    }
}
