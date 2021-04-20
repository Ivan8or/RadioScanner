package online.umbcraft.libraries.message;

import kong.unirest.json.JSONObject;
import online.umbcraft.libraries.ProcessTimer;
import online.umbcraft.libraries.RadioSocket;
import online.umbcraft.libraries.ReasonResponder;
import online.umbcraft.libraries.WalkieTalkie;
import online.umbcraft.libraries.encrypt.HelpfulRSAKeyPair;
import online.umbcraft.libraries.errors.RadioError;

import java.io.IOException;
import java.util.concurrent.Future;

public class ReasonMessage extends RadioMessage {



    /**
     * Creates a blank RadioMessage
     */
    public ReasonMessage() {
        super();
    }

    /**
     * Creates a RadioMessage from a JSON String
     *
     * @param json JSON string from which the message is generated
     */
    public ReasonMessage(String json) {
        super(json);
    }

    /**
     * sets the reason for this message being sent
     *
     * @param reason the new message reason
     * @return itself
     */
    public ReasonMessage setReason(String reason) {
        if (debug) logger.info("setting reason " + reason);
        message.put("reason", reason);
        return this;
    }


    /**
     * sets the reason for this message being sent
     *
     * @return the reason for this message
     */
    public String getReason() {
        return message.getString("reason");
    }

    @Override
    public ReasonMessage put(String key, String val) {
        if(key.equals("reason")) throw new IllegalArgumentException("reserved key");
        super.put(key, val);
        return this;
    }

    @Override
    public ReasonMessage clear() {
        super.clear();
        return this;
    }

    @Override
    public synchronized ReasonMessage setRSAKeys(String public_key, String private_key) {
        super.setRSAKeys(public_key, private_key);
        return this;
    }

    @Override
    public synchronized ReasonMessage setRSAKeys(HelpfulRSAKeyPair keys) {
        super.setRSAKeys(keys);
        return this;
    }

    @Override
    public ReasonMessage merge(RadioMessage other) {
        super.merge(other);
        return this;
    }


    @Override
    public ReasonMessage enableDebug() {
        super.enableDebug();
        return this;
    }


    @Override
    public ReasonMessage disableDebug() {
        super.disableDebug();
        return this;
    }


    /**
     * encrypts and sends itself to a specified IP and port
     *
     * @param IP   the destination IPv4 address
     * @param port the destination port
     * @return A {@link Future} containing the reply sent by the {@link ReasonResponder} which received the message
     */
    public Future<ResponseMessage> send(String IP, int port) {

        ProcessTimer timer = new ProcessTimer();

        if (debug)
            logger.info("sending message " + message + " to " + IP + ":" + port);

        if(getReason() == null) {
            throw new IllegalStateException("NO MESSAGE REASON SPECIFIED");
        }
        if(RSA_PAIR == null) {
            throw new IllegalStateException("NO RSA KEYPAIR SPECIFIED");
        }

        return WalkieTalkie.sharedExecutor().submit(() -> {

            ResponseMessage toReturn;
            RadioSocket job = null;

            RadioError error = RadioError.FAILED_TO_CONNECT;

            try {
                job = new RadioSocket(IP, port, RSA_PAIR.pub(), RSA_PAIR.priv());
                job.setMessage(message.toString(), getReason());

                error = RadioError.BAD_CRYPT_KEY;
                job.encodeMessage();

                error = RadioError.BAD_NETWORK_WRITE;
                job.sendMessage();

                error = RadioError.BAD_NETWORK_READ;
                job.receiveResponse();

                error = RadioError.BAD_CRYPT_KEY;
                job.decodeResponse();

                error = RadioError.INVALID_SIGNATURE;
                job.verifySignature();

                if (debug) logger.info("message to " + IP + ":" + port + " took " + timer.time() + " ms");

                error = RadioError.INVALID_JSON;
                toReturn = new ResponseMessage(job.getResponse());

            } catch (Exception e) {
                if (debug) logger.severe(error.name());

                toReturn = new ResponseMessage()
                        .put("TRANSMIT_ERROR", error.name());

                if (error == RadioError.INVALID_JSON)
                    toReturn.put("body", job.getResponse());
            }

            try {
                if (job != null) job.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

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
    public Future<ResponseMessage> send(String IP, String port) {
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
    public Future<ResponseMessage> send(String address) {

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
