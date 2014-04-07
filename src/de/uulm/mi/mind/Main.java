package de.uulm.mi.mind;

import de.uulm.mi.mind.sensor.WifiSense;

public class Main {

    private static final String HELP = "" +
            "This program scans via monitor mode and reports all devices' IP and their perceived\n" +
            "strength to the server. All data is temporary and not stored!\n\n" +
            "The following values can be set (defaults shown in []):\n" +
            " – ip=[127.0.0.1:8080] :: The IP address and port of the server.\n" +
            " – name=[test]         :: The username of the sensor to login with.\n" +
            " – password=[test]     :: The password to use.\n" +
            " – sleep=[15]          :: Time in seconds between scans.\n" +
            " – help                :: Prints this text.\n\n" +
            "Example: java -jar mind_sensor.jar sleep=35 password=439578744\n\n" +
            "Project: MIND      Author: Tamino Hartmann";
    private static final String DEFAULT = "NOTE: Running program with default values! Try the option " +
            "<help> to see what values can be set.";
    private static final String PARSE = "Failed to parse values! Must be of format \"<name>=<value>\"!";
    private static final String UNKNOWN = "Unknown key=value pair! Ignoring...";

    /**
     * Standard IP.
     */
    private static String ip = "127.0.0.1:8080";
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

        // start & run scanning service
        WifiSense sensorThread = new WifiSense(ip, name, password, sleep);
        new Thread(sensorThread).run();
        // todo make sure no memory leaks are taking place
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
                case "sleep":
                    sleep = Integer.parseInt(value);
                default:
                    System.out.println(UNKNOWN);
            }
        }
        return true;
    }
}
