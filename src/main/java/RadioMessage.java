import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Future;

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

    private JSONObject message;

    public RadioMessage() {
        message = new JSONObject();
    }

    public RadioMessage(String json) {
        message = new JSONObject(json);
    }

    public RadioMessage(byte[] encrypted_json) {
        //String plain_json = new String(ByteEncryptor.decryptRSA(encrypted_json));
        //message = new JSONObject(plain_json);
    }

    // empties the object
    public RadioMessage clear() {
        message = new JSONObject();
        return this;
    }

    // adds a new key/value pair to the JSON object
    public RadioMessage put(String key, String val) {
        message.put(key, val);
        return this;
    }

    // combines with a second message
    // absorbs any new key/value pairs, while not including any keys/value pairs
    // that it already holds a key for
    public RadioMessage merge(RadioMessage other) {

        for (String key : other.message.toMap().keySet())
            if (!message.has(key))
                message.put(key, other.get(key));

        return this;
    }

    // gets the value for a certain key
    public String get(String key) {
        try {
            return message.getString(key);
        } catch (JSONException e) {
            return null;
        }
    }

    // returns the json string
    public String toString() {
        return message.toString();
    }

    @Deprecated
    // returns the legacy string of the format used to send messages in v1.0 of the plugin
    public String toLegacyString() {
        String toReturn = "";

        for (String key : message.toMap().keySet()) {
            toReturn += key + "=" + message.getString(key).replaceAll(" ", "%@%") + " ";
        }
        return toReturn;
    }

    @Deprecated
    // creates a json object from a legacy string
    public RadioMessage fromLegacyString(String legacy_string) {
        String[] items = legacy_string.split(" ");
        for (String item : items) {
            String[] pair = item.split("=");
            if (pair.length == 2)
                message.put(pair[0], pair[1].replaceAll("%@%", " "));
        }
        return this;
    }

    // sends itself to a certain IP and port
    // can throw an error if the address is unavailable
    @Deprecated
    public Future<String> send(String IP, int port) {

        return WalkieTalkie.sharedExecutor().submit(() -> {
            Socket socket = new Socket(IP, port);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            oos.writeUTF(message.toString());
            oos.flush();

            String response = ois.readUTF();

            ois.close();
            oos.close();
            socket.close();
            return response;
        });
    }
    // encrypts and sends itself to a certain IP and port
    // can throw an error if the address is unavailable
    public Future<String> sendE(String IP, int port) {

        return WalkieTalkie.sharedExecutor().submit(() -> {
            Socket socket = new Socket(IP, port);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            String AESKey = MessageEncryptor.genAESKey();
            String encryptedMessage = MessageEncryptor.encryptAES(message.toString(),AESKey);
            String encryptedKey = MessageEncryptor.encryptRSA(AESKey);
            String signature = MessageEncryptor.generateSignature(message.toString());

            oos.writeUTF(encryptedKey);
            oos.writeUTF(signature);
            oos.writeUTF(encryptedMessage);

            oos.flush();

            String responseKey_b64 = ois.readUTF();
            String responseSignature = ois.readUTF();
            String responseBody_b64 = ois.readUTF();

            String resultAESKey = MessageEncryptor.decryptRSA(responseKey_b64);
            String resultBody = MessageEncryptor.decryptAES(responseBody_b64, resultAESKey);
            boolean validSignature = MessageEncryptor.verifySignature(resultBody, responseSignature);
            if(!validSignature) {
                System.err.println("INVALID SIGNATURE!");
                return null;
            }

            ois.close();
            oos.close();
            socket.close();
            return resultBody;
        });
    }

    //alias for send()
    public Future<String> send(String IP, String port) {
        return send(IP, Integer.parseInt(port));
    }

    //alias for send()
    public Future<String> send(String address) {

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
