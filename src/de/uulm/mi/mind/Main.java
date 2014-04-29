package de.uulm.mi.mind;

import de.uulm.mi.mind.threads.SenderThread;
import de.uulm.mi.mind.threads.WifiThread;

public class Main {

    private static final String HELP = "" +
            "This program scans via monitor mode and reports all devices' IP and their" +
            " perceived strength to the server. All data is temporary and not stored! Note" +
            " that the program must be run with root rights (so sudo it or su it)!\n\n" +
            "The following values can be set (defaults shown in []):\n" +
            " – ip=[127.0.0.1]      :: The IP address of the server and (if not explicitly set) the listenip.\n" +
            " – port=[8080]         :: The port for the server communication.\n" +
            " – name=[test]         :: The username of the threads to login with.\n" +
            " – password=[test]     :: The password to use.\n" +
            " – sleep=[15]          :: Time in seconds between scans.\n" +
            " – interface=[wlan0]   :: The interface to use for scanning.\n" +
            " – listenip=[ip]       :: If used the alternative IP to listen for.\n" +
            " – help                :: Prints this text.\n\n" +
            "Example: sudo java -jar mind_sensor.jar sleep=35 password=439578744\n\n" +
            "Project: MIND      Author: Tamino Hartmann";
    private static final String DEFAULT = "NOTE: Running program with default values! Try the option " +
            "<help> to see what values can be set.";
    private static final String PARSE = "Failed to parse values! Must be of format \"<name>=<value>\"!";
    private static final String UNKNOWN = "Unknown key=value pair! Ignoring...";

    /**
     * Standard server IP.
     */
    private static String serverIp = "127.0.0.1";
    /**
     * IP to sense for (usually the same as serverIP).
     */
    private static String senseIp = "127.0.0.1";
    /**
     * Standard port of the server.
     */
    private static String port = "8080";
    /**
     * Standard interface used to scan.
     */
    private static String interfaceDevice = "wlan0";
    /**
     * Default name.
     */
    private static String name = "test";
    /**
     * Default password.
     */
    private static String password = "test";
    /**
     * Default sleep time in seconds.
     */
    private static int sleep = 15;

    public static void main(String[] args) {
        // check whether we can set valid parameters
        if (!setupArguments(args)) {
            return;
        }
        System.out.println("Beginning program.");

        // todo can we set up the monitoring stuff here?
        // then we wouldn't need external scripts...

        // scanning service
        WifiThread sensorThread = new WifiThread(senseIp, port, interfaceDevice, name);
        // communication service
        SenderThread senderThread = new SenderThread(name, password, serverIp, port, sleep);
        // create threads
        Thread one = new Thread(sensorThread);
        Thread two = new Thread(senderThread);
        // start them
        one.start();
        two.start();
        System.out.println("Started all services.");
        // check if one of the two goes wrong
        // todo does this work how I think it works?
        try {
            one.join();
            two.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Exiting.");
    }

    /**
     * Method for parsing command line arguments.
     *
     * @param args The list of arguments given to the program.
     * @return True if we should continue executing the program, else false.
     */
    private static boolean setupArguments(String[] args) {
        if (args.length == 0) {
            System.out.println(DEFAULT);
            return true;
        }
        // parse arguments and offer help
        for (String argument : args) {
            if (argument.equals("help")) {
                System.out.println(HELP);
                return false;
            }
            String[] keyValuePair = argument.split("=");
            if (keyValuePair.length != 2) {
                System.out.println(PARSE);
                return false;
            }
            boolean alreadySet = false;
            argument = keyValuePair[0];
            String value = keyValuePair[1];
            switch (argument) {
                case "name":
                    name = value;
                    break;
                case "password":
                    password = value;
                    break;
                case "ip":
                    serverIp = value;
                    if (!alreadySet) {
                        senseIp = value;
                    }
                    break;
                case "port":
                    port = value;
                    break;
                case "sleep":
                    sleep = Integer.parseInt(value);
                    break;
                case "interface":
                    interfaceDevice = value;
                    break;
                case "listenip":
                    senseIp = value;
                    break;
                default:
                    System.out.println(UNKNOWN);
            }
        }
        return true;
    }
}
