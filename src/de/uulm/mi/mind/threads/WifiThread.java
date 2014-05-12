package de.uulm.mi.mind.threads;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.DataList;
import de.uulm.mi.mind.objects.SensedDevice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * @author Tamino Hartmann
 *         This class runs as a thread continously. It connects to the server and periodically tells it, which devices it can
 *         sense with which strength.
 */
public class WifiThread implements Runnable {

    /**
     * Instance of log.
     */
    private Messenger log;
    /**
     * Tag for logging with log.
     */
    private final String TAG = "WifiSense";
    /**
     * Stores command used to sniff for devices.
     */
    private final String SNIFFING_CMD;
    /**
     * Stores all sensed devices uniquely.
     */
    private static HashMap<String, SensedDevice> devices = new HashMap<>();
    /**
     * Name of WifiSensor used.
     */
    private final String name;

    /**
     * Constructor. Sets the sleep time, gets important instances, and prepares the connection to the server.
     *
     * @param ip The IP address to listen for.
     */
    public WifiThread(String ip, String port, String device, String name) {
        // get instances
        log = Messenger.getInstance();
        // set vars
        // todo use tcpdump + change parsing to fit
        // TCPDUMP_CMD = "tcpdump -l -n -i " + device + " -I 'dst host " + ip + " && tcp port " + port + "'";
        SNIFFING_CMD = "tshark -i " + device + " -R ip.dst==" + ip + " -R tcp.port==" + port + " -T fields -E separator=? -e wlan.sa -e radiotap.dbm_antsignal -e ip.src";
        log.log(TAG, "Using CMD: " + SNIFFING_CMD);
        this.name = name;
        log.log(TAG, "Created.");
    }

    /**
     * Executing function that runs every SLEEP_TIME ms. Contains all the important logic.
     */
    @Override
    public void run() {
        try {
            // create new tcpdump process
            Process tcpdumpProcess = Runtime.getRuntime().exec(SNIFFING_CMD);
            BufferedReader br = new BufferedReader(new InputStreamReader(tcpdumpProcess.getInputStream()));
            String line;

            while (true) {
                line = br.readLine();
                if (line != null) {
                    SensedDevice dev = readDevice(line);
                    devices.put(dev.getIpAddress(), dev);
                }
            }
        } catch (IOException e) {
            log.error(TAG, "Some process error! Aborting...");
            e.printStackTrace();
        }
    }

    /**
     * Method that reads each line and returns the device from it, if applicable.
     *
     * @param line The line to parse.
     * @return Null or the device instance if valid.
     */
    private SensedDevice readDevice(String line) {
        String[] parts = line.split("\\?");
        // get level
        int levelValue = Integer.parseInt(parts[1]);
        // get ip
        String ipAddress = parts[2];

        if (levelValue >= 0 || ipAddress.isEmpty()) {
            return null;
        }
        return new SensedDevice(name, ipAddress, levelValue);
    }

    /**
     * Method via which external threads can get the sensor information collected.
     *
     * @return A DataList containing the most current version of each SensedDevice
     */
    public synchronized static DataList<SensedDevice> pullDevices() {
        DataList<SensedDevice> push = new DataList<>();
        for (SensedDevice device : devices.values()) {
            push.add(device);
        }
        devices.clear();
        return push;
    }
}
