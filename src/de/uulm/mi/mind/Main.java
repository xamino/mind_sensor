package de.uulm.mi.mind;

import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.WifiSensor;
import de.uulm.mi.mind.objects.enums.API;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;
import de.uulm.mi.mind.remote.RemoteConnection;

public class Main {

    public static void main(String[] args) {
        // todo parse arguments (if any?)
        // – IP address of server!
        // – sleep time of thread

        RemoteConnection remoteConnection = new RemoteConnection("127.0.0.1:8080");
        Data received = remoteConnection.runTask(API.CHECK, new WifiSensor("test", "password"), null);
        if (received == null) {
            System.out.println("Failed!");
            return;
        }
        if (received instanceof de.uulm.mi.mind.objects.messages.Error) {
            Error error = ((Error) received);
            System.out.println(error.getType().toString() + "::" + error.getDescription());
        } else if (received instanceof Success) {
            System.out.println(((Success) received).getDescription());
        } else {
            System.out.println("Yup! " + received.toString());
        }
        /*
        // todo 2 start & run scanning service
        WifiSense sensorThread = new WifiSense(5000);
        new Thread(sensorThread).run();
        */
    }
}
