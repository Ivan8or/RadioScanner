package online.umbcraft.libraries;

import online.umbcraft.libraries.encrypt.HelpfulRSAKeyPair;
import online.umbcraft.libraries.errors.RadioError;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Passes any incoming message on a single port to the appropriate {@link ReasonResponder}
 * <p>
 * The server listener for a single port
 * can contain multiple responders, one for any unique message reason
 */
public class PortListener extends Thread {

    private static final Logger logger = WalkieTalkie.getLogger();

    private final HelpfulRSAKeyPair RSA_PAIR;
    private final int PORT;

    private ServerSocket server_listener;
    private Map<String, ReasonResponder> responders;
    private WalkieTalkie talkie;


    /**
     * Creates an empty PortListener<p>
     *
     * @param talkie       the {@link WalkieTalkie} instance this belongs to<p>
     * @param port         the port this listens on
     * @param pub_key_b64  the public RSA key to encrypt {@link RadioMessage} replies
     * @param priv_key_b64 the private RSA key to encrypt replies
     */
    public PortListener(WalkieTalkie talkie, int port, String pub_key_b64, String priv_key_b64) {
        this.talkie = talkie;
        responders = new HashMap<>(5);
        this.PORT = port;
        RSA_PAIR = new HelpfulRSAKeyPair(pub_key_b64, priv_key_b64);
    }


    /**
     * Creates an empty PortListener<p>
     *
     * @param talkie the {@link WalkieTalkie} instance this belongs to<p>
     * @param port   the port this listens on
     * @param pair   the RSA keypair used to encrypt {@link RadioMessage} replies
     */
    public PortListener(WalkieTalkie talkie, int port, HelpfulRSAKeyPair pair) {
        this.talkie = talkie;
        responders = new HashMap<>(5);
        this.PORT = port;
        RSA_PAIR = pair;
    }


    /**
     * Gets the port this is listening on
     *
     * @return the listening port
     */
    public int getPort() {
        return PORT;
    }


    /**
     * Adds a {@link ReasonResponder} allowing it to reply to message recieved by this
     *
     * @param responder the {@link ReasonResponder} to be added
     */
    public void addResponder(ReasonResponder responder) {
        String reason = responder.getReason();
        responders.put(reason, responder);
    }


    /**
     * Gets all {@link ReasonResponder} this currently holds
     *
     * @return all contained {@link ReasonResponder}
     */
    public Collection<ReasonResponder> getResponders() {
        return responders.values();
    }


    /**
     * Closes the server socket on this port<p>
     * this cannot be undone
     */
    public void stopListening() {

        if (talkie.isDebugging())
            logger.info("stopping listening on port " + PORT);

        if (server_listener != null) {

            try {
                server_listener.close();
            } catch (IOException ignored) {
            }
            super.interrupt();
        }
    }


    /**
     * Picks out the appropriate {@link ReasonResponder}
     * and generates a reply to the incoming {@link RadioMessage}
     *
     * @param message the incoming {@link RadioMessage}
     * @return the {@link ReasonResponder} produced response
     */
    private RadioMessage respond(RadioMessage message) {

        if (talkie.isDebugging())
            logger.info("responding to message " + message);

        ReasonResponder responder = responders.get(message.get("reason"));
        RadioMessage response;
        if (responder == null)
            response = new RadioMessage()
                    .put("success", "false")
                    .put("TRANSMIT_ERROR", RadioError.NO_VALID_REASON.name());
        else
            response = responder.response(message);

        if (talkie.isDebugging())
            logger.info("response is " + response);

        return response;
    }


    /**
     * starts listening for {@link RadioMessage}s and responds with {@link ReasonResponder}s<p>
     */
    @Override
    public void run() {

        try {
            server_listener = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {


            final Socket clientSocket;
            try {
                clientSocket = server_listener.accept();
            } catch (IOException e) {

                e.printStackTrace();
                logger.severe("issue receiving client on port " + PORT);

                if (server_listener.isClosed()) {
                    logger.severe("NO LONGER LISTENING ON PORT " + PORT);
                    return;
                }
                continue;
            }

            if (talkie.isDebugging())
                logger.info("receiving message from IP " + clientSocket.getInetAddress());

            WalkieTalkie.sharedExecutor().submit(() -> {

                final RadioSocket job;
                RadioError error = RadioError.FAILED_TO_CONNECT;
                try {
                    job = new RadioSocket(clientSocket, RSA_PAIR.pub(), RSA_PAIR.priv());

                    error = RadioError.BAD_NETWORK_READ;
                    String messageBody = job.receiveMessage();

                    error = RadioError.INVALID_SIGNATURE;
                    boolean validSignature = job.verifySignature(messageBody);
                    if (!validSignature) throw new InvalidKeyException();

                    error = RadioError.ERROR_ON_RESPONSE;
                    RadioMessage response = respond(new RadioMessage(messageBody));

                    error = RadioError.BAD_NETWORK_WRITE;
                    job.sendMessage(response.toString());

                    error = RadioError.NO_VALID_REASON;
                    job.close();

                } catch (Exception e) {
                    logger.severe(error.name() + " - " + e.getClass().getSimpleName());
                }
            });
        }
    }
}
