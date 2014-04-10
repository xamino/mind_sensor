package de.uulm.mi.mind.objects;

import java.util.Date;

/**
 * @author Tamino Hartmann
 *         This class implements the user object for the wifi-sniffing program. It authenticates the sensors.
 */
// TODO store the last tcpdump in the session or here? --> I think session would be better...
public class WifiSensor implements Authenticated, Data {

    /**
     * Unique string that identifies this WifiSense and its location.
     */
    private String position;
    /**
     * The token with which the WifiSense authenticates itself to the server.
     */
    private String tokenHash;
    /**
     * Last time the threads logged in.
     */

    private Date lastAccess;

    public WifiSensor(String position, String tokenHash) {
        this.position = position;
        this.tokenHash = tokenHash;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    @Override
    public String readIdentification() {
        return this.position;
    }

    @Override
    public String readAuthentication() {
        return this.tokenHash;
    }

    @Override
    public Date getAccessDate() {
        return this.lastAccess;
    }

    @Override
    public void setAccessDate(Date accessDate) {
        this.lastAccess = accessDate;
    }

    @Override
    public String getKey() {
        return this.position;
    }
}
