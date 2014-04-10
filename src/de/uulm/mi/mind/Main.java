package de.uulm.mi.mind;

import de.uulm.mi.mind.threads.SenderThread;
import de.uulm.mi.mind.threads.WifiThread;

public class Main {

    private static final String HELP = "" +
            "This program scans via monitor mode and reports all devices' IP and their" +
            " perceived strength to the server. All data is temporary and not stored! Note" +
            " that the program must be run with root rights (so sudo it or su it)!\n\n" +
            "The following values can be set (defaults shown in []):\n" +
            " – ip=[127.0.0.1:]     :: The IP address of the server.\n" +
            " – port=[8080]         :: The port of the server.\n" +
            " – name=[test]         :: The username of the threads to login with.\n" +
            " – password=[test]     :: The password to use.\n" +
            " – sleep=[15]          :: Time in seconds between scans.\n" +
            " – interface=[wlan0]   :: The interface to use for scanning.\n" +
            " – help                :: Prints this text.\n\n" +
            "Example: sudo java -jar mind_sensor.jar sleep=35 password=439578744\n\n" +
            "Project: MIND      Author: Tamino Hartmann";
    private static final String DEFAULT = "NOTE: Running program with default values! Try the option " +
            "<help> to see what values can be set.";
    private static final String PARSE = "Failed to parse values! Must be of format \"<name>=<value>\"!";
    private static final String UNKNOWN = "Unknown key=value pair! Ignoring...";

    /**
     * Standard IP.
     */
    private static String ip = "127.0.0.1";
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
        // todo use log
        System.out.println("Beginning program.");

        // todo can we set up the monitoring stuff here?
        // then we wouldn't need external scripts...

        // scanning service
        WifiThread sensorThread = new WifiThread(ip, port, interfaceDevice);
        // communication service
        SenderThread senderThread = new SenderThread(name, password, ip, port, sleep);
        new Thread(sensorThread).start();
        new Thread(senderThread).start();
        // todo make sure no memory leaks are taking place
        // todo sensible thread handling for when something goes wrong... :P
        System.out.println("Started all services.");
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
                    ip = value;
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
                default:
                    System.out.println(UNKNOWN);
            }
        }
        return true;
    }
}
