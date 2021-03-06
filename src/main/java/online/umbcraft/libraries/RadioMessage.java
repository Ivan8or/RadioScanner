package online.umbcraft.libraries;

import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import javax.lang.model.UnknownEntityException;

/*
RadioMessage CLass

Represents a single JSON-based text message that can be sent over the network

   - Messages are collections of string key-value pairs
   - the unspoken standard for messages is that:
      - each request message must include a 'reason',
      - each response message must include a 'success',
      - and if the success is false then the 'reason' for that failure
   - RadioMessages can be combined with each other to merge their key-value pairs
   - RadioMessages can be sent, returning a Future containing the response from the receiver
 */

public class RadioMessage {

    private static final Logger logger = Logger.getLogger(RadioMessage.class.getSimpleName());
    private JSONObject message;
    private String RSA_PRIVATE_KEY;
    private String RSA_PUBLIC_KEY;
    private boolean debug;

    public RadioMessage() {
        message = new JSONObject();
    }

    public RadioMessage(String json) {
        message = new JSONObject(json);
    }

    // empties the object
    public RadioMessage clear() {
        if (debug)
            logger.debug("clearing message contents for message " + message);


        message = new JSONObject();
        return this;
    }

    // adds a new key/value pair to the JSON object
    public RadioMessage put(String key, String val) {
        if (debug)
            logger.debug("inserting " + key + " = " + val + " into message " + message);

        message.put(key, val);
        return this;
    }

    // sets the RSA keys that will be used for encryption when this message is sent
    public synchronized RadioMessage setRSAKeys(String public_key, String private_key) {
        if (debug)
            logger.debug("putting RSA keys into message " + message);
        RSA_PUBLIC_KEY = public_key;
        RSA_PRIVATE_KEY = private_key;
        return this;
    }

    // alias for setRSAKeys
    public synchronized RadioMessage setRSAKeys(String[] keys) {
        return setRSAKeys(keys[0], keys[1]);
    }

    // combines with a second message
    // absorbs any new key/value pairs, while not including any keys/value pairs
    // that it already holds a key for
    public RadioMessage merge(RadioMessage other) {
        if (debug)
            logger.debug("merging message " + message + " with message " + other + "...");

        for (String key : other.message.toMap().keySet())
            if (!message.has(key))
                message.put(key, other.get(key));

        if (debug)
            logger.debug("resulting message: " + message);
        return this;
    }

    public RadioMessage enableDebug() {
        logger.debug("debugging enabled for message " + message);

        debug = true;
        return this;
    }

    public RadioMessage disableDebug() {
        logger.debug("debugging disabled for message " + message);

        debug = false;
        return this;
    }

    // gets the value for a certain key
    public String get(String key) {

        try {
            if (debug)
                logger.debug("pulling key " + key + " from message, result is " + message.getString(key));

            return message.getString(key);
        } catch (JSONException e) {
            logger.error("no key " + key + " in message " + message);
            return null;
        }
    }

    // returns the json string
    public String toString() {
        return message.toString();
    }


    // encrypts and sends itself to a certain IP and port
    // can throw an error if the address is unavailable
    @Deprecated
    public Future<RadioMessage> sendE(String IP, int port) {

        if (debug)
            logger.debug("sending message " + message + " to " + IP + ":" + port);



        return WalkieTalkie.sharedExecutor().submit(() -> {

            Socket socket = null;
            try {
                socket = new Socket(IP, port);
            }catch(Exception e) {
                if(debug)
                    logger.error("FAILED TO CONNECT TO "+IP+":"+port+"... RETURNING BLANK MESSAGE");
                return new RadioMessage()
                        .put("success","false")
                        .put("reason",RadioError.FAILED_TO_CONNECT.name());
            }

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            MessageEncryptor encryptor = new MessageEncryptor(RSA_PUBLIC_KEY, RSA_PRIVATE_KEY);
            String AESKey = MessageEncryptor.genAESKey();
            String encryptedMessage = encryptor.encryptAES(message.toString(), AESKey);
            String encryptedKey = encryptor.encryptRSA(AESKey);
            String signature = encryptor.generateSignature(message.toString());

            oos.writeUTF(encryptedKey);
            oos.writeUTF(signature);
            oos.writeUTF(encryptedMessage);

            oos.flush();

            String responseKey_b64 = null;
            String responseSignature = null;
            String responseBody_b64 = null;
            try {
                responseKey_b64 = ois.readUTF();
                responseSignature = ois.readUTF();
                responseBody_b64 = ois.readUTF();
            } catch (Exception e) {
                if(debug)
                    logger.error("SENT MESSAGE RECEIVED BAD RESPONSE... RETURNING BLANK MESSAGE");
                return new RadioMessage()
                        .put("success", "false")
                        .put("reason", RadioError.BAD_NETWORK_RESPONSE.name());
            }

            String resultAESKey = null;
            String resultBody = null;
            boolean validSignature = false;
            try {
                resultAESKey = encryptor.decryptRSA(responseKey_b64);
                resultBody = encryptor.decryptAES(responseBody_b64, resultAESKey);
                validSignature = encryptor.verifySignature(resultBody, responseSignature);
            } catch (Exception e) {
                if(debug)
                    logger.error("SENT MESSAGE USED BAD CRYPT KEY... RETURNING BLANK MESSAGE");
                return new RadioMessage()
                        .put("success", "false")
                        .put("reason", RadioError.MISFORMATTED_RSA_KEY.name());
            }

            if (!validSignature) {
                if(debug)
                    logger.error("SENT MESSAGE HAS BAD SIGNATURE... RETURNING BLANK MESSAGE");
                return new RadioMessage()
                        .put("success", "false")
                        .put("reason", RadioError.INVALID_SIGNATURE.name());
            }

            ois.close();
            oos.close();
            socket.close();

            return new RadioMessage(resultBody);
        });
    }

    //alias for sendE()
    @Deprecated
    public Future<RadioMessage> sendE(String IP, String port) {
        int port_num = 0;
        try {
            port_num = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid address format");
        }
        return sendE(IP, port_num);

    }

    //alias for sendE()
    @Deprecated
    public Future<RadioMessage> sendE(String address) {

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

        return sendE(ip, port);
    }


    // aliases for sendE
    public Future<RadioMessage> send(String ip, int port) {
        return sendE(ip, port);
    }

    public Future<RadioMessage> send(String ip, String port) {
        return sendE(ip, port);
    }

    public Future<RadioMessage> send(String full_address) {
        return sendE(full_address);
    }


}
