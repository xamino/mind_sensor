package de.uulm.mi.mind.threads;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.SensedDevice;
import de.uulm.mi.mind.objects.WifiSensor;
import de.uulm.mi.mind.objects.enums.API;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.remote.RemoteConnection;

/**
 * Created by tamino on 4/10/14.
 */
public class SenderThread implements Runnable {

    /**
     * Instance of log.
     */
    private Messenger log;
    private final String TAG = "SenderThread";

    private RemoteConnection connection;
    private WifiSensor sensorUser;
    private String session;
    /**
     * Time between scans.
     */
    private final long SLEEP_TIME;

    /**
     * @param name      The login name of the wifisensor.
     * @param password  The login password of the wifisensor.
     * @param ip        The ip of the server to connect to.
     * @param port
     * @param sleepTime The time in seconds between scans and consequent upload to the server.
     */
    public SenderThread(String name, String password, String ip, String port, int sleepTime) {
        log = Messenger.getInstance();
        SLEEP_TIME = sleepTime * 1000;
        connection = new RemoteConnection(ip + ":" + port);
        sensorUser = new WifiSensor(name, password);
    }

    @Override
    public void run() {
        while (true) {
            //check session
            if (!checkSession()) {
                log.error(TAG, "Session could not be established!");
                return;
            }

            DataList<SensedDevice> devices = WifiThread.pullDevices();
            if (devices.isEmpty()) {
                log.log(TAG, "No devices found to send to server.");
            } else {
                log.log(TAG, "Oh, there's something there!");
                /*
                // todo send & what to do on response
                for (SensedDevice device : devices) {
                    Data data = connection.runTask(API.WIFI_SENSOR_UPDATE, device, session);
                }
                */
            }

            // sleep
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                // todo do the correct thing here (which is... ?)
                log.error(TAG, "Interrupted!!!");
                e.printStackTrace();
                return;
            }
        }
    }

    /**
     * This method makes sure that the threads is logged in. If not, it will try to get a session.
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
