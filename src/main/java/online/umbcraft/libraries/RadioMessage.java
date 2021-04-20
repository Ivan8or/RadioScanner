package online.umbcraft.libraries;

import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;
import online.umbcraft.libraries.encrypt.HelpfulRSAKeyPair;
import online.umbcraft.libraries.errors.RadioError;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.logging.Logger;


/**
 * A key-value based text message that can be sent over the network <p>
 * <p>
 * Messages are collections of string key-value pairs<p>
 * The unspoken standard for messages is that:<p>
 * - each request message must include a 'reason',<p>
 * - each response message must include a 'success',<p>
 * - and if the success is false then the 'reason' for that failure<p>
 * when {@link RadioMessage}s are sent they return a Future containing the response from the receiver
 *
 * @see ReasonResponder
 * @see WalkieTalkie
 */
public class RadioMessage {

    private static final Logger logger = WalkieTalkie.getLogger();
    private JSONObject message;
    private HelpfulRSAKeyPair RSA_PAIR;
    private boolean debug;


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
     * empties the RadioMessage of all key-value pairs<p>
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
     * sets the RSA keys that will be used for encryption when this message is sent
     *
     * @param public_key  public RSA key in base64
     * @param private_key private RSA key in base64
     * @return itself
     */
    public synchronized RadioMessage setRSAKeys(String public_key, String private_key) {
        return setRSAKeys(new HelpfulRSAKeyPair(public_key, private_key));
    }


    /**
     * sets the RSA keys that will be used for encryption when this message is sent
     *
     * @param keys base64 RSA keypair array (public key in index 0, private key in index 1)
     * @return itself
     */
    public synchronized RadioMessage setRSAKeys(String[] keys) {
        return setRSAKeys(new HelpfulRSAKeyPair(keys[0], keys[1]));
    }


    /**
     * sets the RSA keys that will be used for encryption when this message is sent
     *
     * @param keys RSA keypair
     * @return itself
     */
    public synchronized RadioMessage setRSAKeys(HelpfulRSAKeyPair keys) {
        if (debug) logger.info("putting RSA keys into message " + message);

        RSA_PAIR = keys;
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
    public String toString() {
        return message.toString();
    }


    /**
     * encrypts and sends itself to a specified IP and port
     *
     * @param IP   the destination IPv4 address
     * @param port the destination port
     * @return A {@link Future}bcontaining the reply sent by the {@link ReasonResponder} which received the message
     */
    public Future<RadioMessage> send(String IP, int port) {

        ProcessTimer timer = new ProcessTimer();

        if (debug)
            logger.info("sending message " + message + " to " + IP + ":" + port);


        return WalkieTalkie.sharedExecutor().submit(() -> {

            RadioMessage toReturn;
            RadioSocket job = null;
            String responseBody = "";

            RadioError error = RadioError.FAILED_TO_CONNECT;

            try {
                job = new RadioSocket(IP, port, RSA_PAIR.pub(), RSA_PAIR.priv());

                error = RadioError.BAD_NETWORK_WRITE;
                job.sendMessage(message.toString());

                error = RadioError.BAD_NETWORK_READ;
                responseBody = job.receiveMessage();

                error = RadioError.INVALID_SIGNATURE;
                job.verifySignature(responseBody);

                if (debug) logger.info("message to " + IP + ":" + port + " took " + timer.time() + " ms");

                error = RadioError.INVALID_JSON;
                toReturn = new RadioMessage(responseBody);

            } catch (Exception e) {
                if (debug) logger.severe(error.name());

                toReturn = new RadioMessage()
                    .put("TRANSMIT_ERROR", error.name());

                if(error == RadioError.INVALID_JSON)
                    toReturn.put("body", responseBody);
            }

            try {
                if(job != null) job.close();
            }catch(IOException e) { e.printStackTrace(); }

            return toReturn;
        });
    }


    /**
     * encrypts and sends itself to a specified IP and port
     *
     * @param IP   the destination IPv4 address
     * @param port the destination port
     * @return A {@link Future} containing the reply sent by the {@link ReasonResponder} which received the message
     */
    public Future<RadioMessage> send(String IP, String port) {
        int port_num = 0;
        try {
            port_num = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid address format");
        }
        return send(IP, port_num);

    }


    /**
     * encrypts and sends itself to a specified IP and port
     *
     * @param address the destination IPv4 address, made up of IP:port
     * @return A {@link Future} containing the reply sent by the {@link ReasonResponder} which received the message
     */
    public Future<RadioMessage> send(String address) {

        String[] split = address.split(":");

        if (split.length != 2)
            throw new IllegalArgumentException("Invalid address format");

        String ip = split[0];
        int port = 0;
        try {
            port = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid address format");
        }

        return send(ip, port);
    }
}
