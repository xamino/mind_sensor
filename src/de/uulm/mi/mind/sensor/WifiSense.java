package de.uulm.mi.mind.sensor;

import de.uulm.mi.mind.logger.Messenger;

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

    /**
     * Constructor. Sets the sleep time, gets important instances, and prepares the connection to the server.
     *
     * @param sleepTime The time in ms that the the thread sleeps between scans.
     */
    public WifiSense(long sleepTime) {
        log = Messenger.getInstance();
        // set defaults
        SLEEP_TIME = sleepTime;

        log.log(TAG, "Created.");
    }

    /**
     * Executing function that runs every SLEEP_TIME ms. Contains all the important logic.
     */
    @Override
    public void run() {
        while (true) {
            // todo check session and if not valid, try login
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                // todo do the correct thing here (which is... ?)
                log.error(TAG, "Interrupted!!!");
                e.printStackTrace();
            }
        }
    }
}
