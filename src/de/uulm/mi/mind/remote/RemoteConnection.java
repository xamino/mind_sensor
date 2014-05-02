package de.uulm.mi.mind.remote;

import de.uulm.mi.mind.json.JsonConverter;
import de.uulm.mi.mind.logger.Messenger;
import de.uulm.mi.mind.objects.*;
import de.uulm.mi.mind.objects.enums.API;
import de.uulm.mi.mind.objects.messages.Error;
import de.uulm.mi.mind.objects.messages.Success;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * @author Tamino Hartmann
 *         This class executes a given task with the session and object, and returns the departure object received back if
 *         successful.
 */
public class RemoteConnection {
    /**
     * Instance of log.
     */
    private Messenger log;
    /**
     * Instance of JsonConverter for server communication.
     */
    private JsonConverter<Data> json;
    private final String TAG = "RemoteConnection";
    private final String URL;

    public RemoteConnection(String serverIP) {
        // ready instances
        log = Messenger.getInstance();
        json = new JsonConverter<Data>();
        // register json types
        json.registerType(WifiSensor.class);
        json.registerType(Arrival.class);
        json.registerType(Departure.class);
        json.registerType(SensedDevice.class);
        json.registerType(Success.class);
        json.registerType(Error.class);
        // set variables
        URL = "http://" + serverIP + "/main";

        log.log(TAG, "Created.");
    }

    /**
     * @param task
     * @param object
     * @param session
     * @return
     */
    public Data runTask(API task, Data object, String session) {
        String requestString = json.toJson(new Arrival(session, task.toString(), object));
        if (requestString == null) {
            log.error(TAG, "Failed to convert to JSON! Aborting.");
            return new Error(Error.Type.CAST, "JSON cast failed!");
        }

        try

        {
            HttpClient httpclient = HttpClients.createDefault();
            // create post with URL
            HttpPost httppost = new HttpPost(URL);
            // add json string
            httppost.setEntity(new StringEntity(requestString));
            //Execute and get the response.
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity responseEntity = response.getEntity();

            // read response
            BufferedReader in = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            String result = sb.toString();

            // Convert Response from json, Should be a Departure Object
            Data responseData = json.fromJson(result);
            if (!(responseData instanceof Departure)) {
                return new Error(Error.Type.SERVER, "Server returned invalid response!");
            }
            return ((Departure) responseData).getObject();
        } catch (
                ClientProtocolException e
                )

        {
            log.error(TAG, "Protocol exception!");
            e.printStackTrace();
        } catch (
                UnsupportedEncodingException e
                )

        {
            log.error(TAG, "Encoding exception!");
            e.printStackTrace();
        } catch (
                IOException e
                )

        {
            log.error(TAG, "Failed request! Check your URL!");
            e.printStackTrace();
        }

        return new

                Error(Error.Type.CONNECTION, "Connection to server failed!");
    }
}
