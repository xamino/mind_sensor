package de.uulm.mi.mind.sensor;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.SensedDevice;
import de.uulm.mi.mind.objects.WifiSensor;
import de.uulm.mi.mind.objects.enums.API;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.remote.RemoteConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
    private final String TCPDUMP_CMD;

    private RemoteConnection connection;
    private WifiSensor sensorUser;
    private String session;

    /**
     * Constructor. Sets the sleep time, gets important instances, and prepares the connection to the server.
     *
     * @param ip        The server IP to use.
     * @param name      The login name of the wifisensor.
     * @param password  The login password of the wifisensor.
     * @param sleepTime The time in seconds between scans and consequent upload to the server.
     */
    public WifiSense(String name, String password, String ip, String port, String device, int sleepTime) {
        // get instances
        log = Messenger.getInstance();
        // set vars
        SLEEP_TIME = sleepTime * 1000;
        TCPDUMP_CMD = "tcpdump -l -n -i " + device + " -I 'dst host " + ip + " && tcp port " + port + "'";
        connection = new RemoteConnection(ip + ":" + port);
        sensorUser = new WifiSensor(name, password);

        log.log(TAG, "Created.");
    }

    /**
     * Executing function that runs every SLEEP_TIME ms. Contains all the important logic.
     */
    @Override
    public void run() {

        // Prepare tcpdumprocess
        Process tcpdumpProcess = null;
        try {
            tcpdumpProcess = Runtime.getRuntime().exec(TCPDUMP_CMD);
        } catch (IOException e) {
            log.error(TAG, "Failed to start TCPDUMP! Aborting.");
            e.printStackTrace();
            return;
        }
        // prepare reader of output
        BufferedReader br = new BufferedReader(new InputStreamReader(tcpdumpProcess.getInputStream()));
        // run loop
        while (true) {
            //check session
            if (!checkSession()) {
                log.error(TAG, "Session could not be established!");
                return;
            }
            log.log(TAG, "Beginning scan...");
            // must read all lines (not just one!)
            try {
                // read line for line, convert to device, and append to list
                String line = br.readLine();
                DataList<SensedDevice> devices = new DataList<SensedDevice>();
                while (line != null) {
                    devices.add(readDevice(line));
                }
                if (!devices.isEmpty()) {
                    // send list to server
                    Data retData = connection.runTask(API.WIFI_SENSOR_UPDATE, devices, session);
                    System.out.println(retData.toString());
                    if (retData instanceof de.uulm.mi.mind.objects.messages.Error) {
                        log.error(TAG, "Failed to send list to server! " + ((Error) retData).getDescription());
                    }
                } else {
                    // ready == false, so nothing happened since we last read it
                    log.log(TAG, "Sensed nothing of importance.");
                }
            } catch (IOException e) {
                // br.ready exception, just try again
                log.error(TAG, "Failed read from TCPDUMP process!");
                e.printStackTrace();
            }
            log.log(TAG, "Finished scan.");
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

    private SensedDevice readDevice(String line) {
        String parts[] = line.split(" ");
        int levelValue = 0;
        String ipAddress = "";

        System.out.println(line);

        for (String part : parts) {
            System.out.println(part);
            if (part.endsWith("dB")) {
                levelValue = Integer.parseInt(part.substring(0, part.length() - 2));
            } else if (part.startsWith("134.60.")) {
                ipAddress = part.substring(0, part.lastIndexOf("."));
            }
        }
        if (levelValue >= 0 || ipAddress.isEmpty()) {
            return null;
        }
        return new SensedDevice(ipAddress, levelValue);
    }
}