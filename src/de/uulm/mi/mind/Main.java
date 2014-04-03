package de.uulm.mi.mind;

import de.uulm.mi.mind.sensor.WifiSense;

public class Main {

    public static void main(String[] args) {
        // todo parse arguments (if any?)
        // – IP address of server!
        // – sleep time of thread

        // todo 2 start & run scanning service
        WifiSense sensorThread = new WifiSense(5000);
        new Thread(sensorThread).run();
    }
}
