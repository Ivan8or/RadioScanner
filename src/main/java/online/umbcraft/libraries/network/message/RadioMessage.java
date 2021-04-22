package online.umbcraft.libraries.network.message;

import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;
import online.umbcraft.libraries.network.response.ReasonResponder;
import online.umbcraft.libraries.network.response.WalkieTalkie;

import java.util.logging.Logger;


/**
 * <p>A key-value based text message that can be sent over the network </p>
 *
 * <p> Messages are collections of string key-value pairs </p>
 * <p> The unspoken standard for messages is that: </p>
 * <p> - each request message must include a 'reason', </p>
 * <p> - each response message must include a 'success', </p>
 * <p> - and if the success is false then the 'reason' for that failure </p>
 * when {@link RadioMessage}s are sent they return a Future containing the response from the receiver
 *
 * @see ReasonResponder
 * @see WalkieTalkie
 */
public class RadioMessage {

    final static protected Logger logger = WalkieTalkie.getLogger();
    protected JSONObject message;
    protected boolean debug;


    /**
     * Creates a blank RadioMessage
     */
    public RadioMessage() {
        message = new JSONObject();
    }


    /**
     * Creates a RadioMessage from a JSON String
     *
     * @param json JSON string from which the message is generated
     */
    public RadioMessage(String json) {
        message = new JSONObject(json);
    }


    /**
     * <p> empties the RadioMessage of all key-value pairs </p>
     * does not reset the RSA keys or debug status
     *
     * @return itself
     */
    public RadioMessage clear() {
        if (debug)
            logger.info("clearing message contents for message " + message);

        message = new JSONObject();
        return this;
    }


    /**
     * adds a new key/value pair to the message
     *
     * @param key message key
     * @param val message value
     * @return itself
     */
    public RadioMessage put(String key, String val) {

        if (debug) logger.info("inserting " + key + " = " + val + " into message " + message);
        message.put(key, val);
        return this;
    }


    /**
     * combines itself with a second {@link RadioMessage}
     * absorbs any new key/value pairs for which this did not yet contain the key
     * ignores any keys/value pairs this already holds a key for
     *
     * @param other A second {@link RadioMessage} from which this copies all key-value pairs
     * @return itself
     */
    public RadioMessage merge(RadioMessage other) {
        if (debug) logger.info("merging message " + message + " with message " + other + "...");

        for (String key : other.message.toMap().keySet())
            if (!message.has(key))
                message.put(key, other.get(key));

        if (debug) logger.info("resulting message: " + message);

        return this;
    }


    /**
     * Enables logger output for most actions performed by this object
     *
     * @return itself
     */
    public RadioMessage enableDebug() {
        logger.info("debugging enabled for message " + message);

        debug = true;
        return this;
    }


    /**
     * Disables logger output for this object
     *
     * @return itself
     */
    public RadioMessage disableDebug() {
        logger.info("debugging disabled for message " + message);

        debug = false;
        return this;
    }


    /**
     * Gets the value of a certain key in this message
     * returns null if no value exists
     *
     * @param key the string key of a key-value pair inside this message
     * @return the key's associated value (or null if there is no value)
     */
    public String get(String key) {

        try {
            if (debug)
                logger.info("pulling key " + key + " from message, result is " + message.getString(key));

            return message.getString(key);
        } catch (JSONException e) {
            logger.severe("no key " + key + " in message " + message);
            return null;
        }
    }


    /**
     * Gives the content of this message in the form of a JSON string
     *
     * @return the exact JSON form of this message
     */
    public String json() {
        return message.toString();
    }


    /**
     * Gives the content of this message in the form of a JSON string
     *
     * @return the exact JSON form of this message
     */
    public String toString() {
        return message.toString();
    }
}
