package de.uulm.mi.mind.threads;

import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.SensedDevice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

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
    private final String TCPDUMP_CMD;
    private static ArrayList<SensedDevice> devices = new ArrayList<>();

    /**
     * Constructor. Sets the sleep time, gets important instances, and prepares the connection to the server.
     *
     * @param ip The IP address to listen for.
     */
    public WifiThread(String ip, String port, String device) {
        // get instances
        log = Messenger.getInstance();
        // set vars
        // TCPDUMP_CMD = "tcpdump -l -n -i " + device + " -I 'dst host " + ip + " && tcp port " + port + "'";
        TCPDUMP_CMD = "tshark -i " + device + " -R ip.dst==" + ip + " -R tcp.port==" + port + "-T fields -E separator=? -e wlan.sa -e radiotap.dbm_antsignal -e ip.src";
        log.log(TAG, "Created.");
    }

    /**
     * Executing function that runs every SLEEP_TIME ms. Contains all the important logic.
     */
    @Override
    public void run() {
        try {
            // create new tcpdump process
            Process tcpdumpProcess = Runtime.getRuntime().exec(TCPDUMP_CMD);
            BufferedReader br = new BufferedReader(new InputStreamReader(tcpdumpProcess.getInputStream()));
            String line;

            while (true) {
                line = br.readLine();
                if (line != null) {
                    devices.add(readDevice(line));
                } else {
                    // log.log(TAG, "Sensed nothing...");
                    // TODO: original restarted the service in this case – why?
                }
            }
        } catch (IOException e) {
            log.error(TAG, "Some process error! Aborting...");
            e.printStackTrace();
        }
    }

    private SensedDevice readDevice(String line) {
        int levelValue = 0;
        String ipAddress = "";

        // todo maybe create sensed device when data is pulled?
        ArrayList<String> parts = new ArrayList<>();
        for (String stub : line.split(" ")) {
            System.out.println(stub);
        }


        return new SensedDevice(ipAddress, levelValue);
    }

    /**
     * @return
     */
    public synchronized static ArrayList<SensedDevice> pullDevices() {
        ArrayList<SensedDevice> push = devices;
        devices = new ArrayList<SensedDevice>();
        return push;
    }
}