package online.umbcraft.libraries;

import online.umbcraft.libraries.encrypt.HelpfulRSAKeyPair;
import online.umbcraft.libraries.errors.RadioError;
import online.umbcraft.libraries.message.RadioMessage;
import online.umbcraft.libraries.message.ReasonMessage;
import online.umbcraft.libraries.message.ResponseMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
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

    private final int PORT;

    private ServerSocket server_listener;
    private Map<String, ReasonResponder> responders;
    private WalkieTalkie talkie;


    /**
     * Creates an empty PortListener<p>
     *
     * @param talkie       the {@link WalkieTalkie} instance this belongs to<p>
     * @param port         the port this listens on
     */
    public PortListener(WalkieTalkie talkie, int port) {
        this.talkie = talkie;
        responders = new HashMap<>(5);
        this.PORT = port;
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
    private ResponseMessage respond(ReasonMessage message) {

        if (talkie.isDebugging())
            logger.info("responding to message " + message);

        ReasonResponder responder = responders.get(message.get("reason"));
        ResponseMessage response = responder.response(message);

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
                    job = new RadioSocket(clientSocket);

                    error = RadioError.BAD_NETWORK_READ;
                    job.receiveRemote();

                    error = RadioError.NO_VALID_REASON;
                    if (!responders.containsKey(job.getRemoteReason())) throw new IllegalStateException();

                    HelpfulRSAKeyPair selfPair = responders.get(job.getRemoteReason()).getKeypair();
                    PublicKey remotePub = HelpfulRSAKeyPair.publicFrom64(job.getRemotePub());

                    error = RadioError.BAD_CRYPT_KEY;
                    job.decodeRemote(selfPair.priv());

                    error = RadioError.INVALID_SIGNATURE;
                    if (!job.verifyRemoteSignature(remotePub)) throw new InvalidKeyException();

                    error = RadioError.INVALID_JSON;
                    ReasonMessage message = new ReasonMessage(job.getRemoteBody());

                    error = RadioError.ERROR_ON_RESPONSE;
                    ResponseMessage response = respond(message);

                    error = RadioError.INVALID_JSON;
                    job.setMessage(response.json(), "", selfPair.pub64());

                    error = RadioError.BAD_CRYPT_KEY;
                    job.encodeMessage(remotePub, selfPair.priv());

                    error = RadioError.BAD_NETWORK_WRITE;
                    job.sendMessage();

                    error = RadioError.FAILED_TO_CONNECT;
                    job.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe(error.name() + " - " + e.getClass().getSimpleName());
                }
            });
        }
    }
}
