package online.umbcraft.libraries.network.message;

import online.umbcraft.libraries.network.RadioSocket;
import online.umbcraft.libraries.network.response.ReasonResponder;
import online.umbcraft.libraries.network.response.WalkieTalkie;
import online.umbcraft.libraries.encrypt.HelpfulRSAKeyPair;
import online.umbcraft.libraries.network.errors.RadioError;

import java.io.IOException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.Future;

public class ReasonMessage extends RadioMessage {


    protected HelpfulRSAKeyPair keypair;
    protected PublicKey remotePub;

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
     * @return the reason for this message
     */
    public String getReason() {
        return message.getString("reason");
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
     * sets the RSA keys that will be used for encryption when this message is sent
     *
     * @param public_key  public RSA key in base64
     * @param private_key private RSA key in base64
     * @return itself
     * @throws InvalidKeySpecException if either of the provided keys are not valid RSA
     */
    public ReasonMessage setRSAKeys(String public_key, String private_key) throws InvalidKeySpecException {
        return setRSAKeys(new HelpfulRSAKeyPair(public_key, private_key));
    }


    /**
     * sets the RSA keys that will be used for encryption when this message is sent
     *
     * @param keys RSA keypair
     * @return itself
     */
    public ReasonMessage setRSAKeys(HelpfulRSAKeyPair keys) {
        if (debug) logger.info("putting RSA keys into message " + message);

        keypair = keys;
        return this;

    }


    public ReasonMessage setRemoteKey(PublicKey remote) {
        if (debug) logger.info("setting remote key for message " + message);

        remotePub = remote;
        return this;
    }


    /**
     * <p> encrypts and sends itself to a {@link ReasonResponder} at a specified IP and port </p>
     *
     * @param IP   the destination IPv4 address
     * @param port the destination port
     * @return A {@link Future} containing the reply sent by the {@link ReasonResponder} which received the message
     */
    public Future<ResponseMessage> send(String IP, int port) {

        if (debug)
            logger.info("sending message " + message + " to " + IP + ":" + port);

        if (getReason() == null) {
            throw new IllegalStateException("NO MESSAGE REASON SPECIFIED");
        }
        if (keypair == null) {
            throw new IllegalStateException("NO RSA KEYPAIR SPECIFIED");
        }
        if (remotePub == null) {
            throw new IllegalStateException("NO REMOTE KEY SPECIFIED");
        }

        return WalkieTalkie.sharedExecutor().submit(() -> {

            ResponseMessage toReturn;
            RadioSocket job = null;

            RadioError error = RadioError.FAILED_TO_CONNECT;

            try {
                job = new RadioSocket(IP, port);
                job.setMessage(message.toString(), getReason(), keypair.pub64());

                error = RadioError.BAD_CRYPT_KEY;
                job.encodeMessage(remotePub, keypair.priv());

                error = RadioError.BAD_NETWORK_WRITE;
                job.sendMessage();

                error = RadioError.BAD_NETWORK_READ;
                job.receiveRemote();

                error = RadioError.INVALID_SIGNATURE;
                job.verifyRemoteSignature(remotePub);

                error = RadioError.BAD_CRYPT_KEY;
                job.decodeRemote(keypair.priv());

                error = RadioError.INVALID_JSON;
                toReturn = new ResponseMessage(job.getRemoteBody());

            } catch (Exception e) {
                if (debug) logger.severe(error.name());

                toReturn = new ResponseMessage()
                        .put("TRANSMIT_ERROR", error.name());

                if (error == RadioError.INVALID_JSON)
                    toReturn.put("body", job.getRemoteBody());
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


    /**
     * <p> adds a new key/value pair to the message </p>
     * reserved keys: 'reason'
     *
     * @param key message key
     * @param val message value
     * @return itself
     */
    @Override
    public ReasonMessage put(String key, String val) {
        if (key.equals("reason")) throw new IllegalArgumentException("reserved key");
        super.put(key, val);
        return this;
    }


    @Override
    public ReasonMessage clear() {
        super.clear();
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
}
