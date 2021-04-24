package online.umbcraft.libraries.network.response;

import online.umbcraft.libraries.network.RadioSocket;
import online.umbcraft.libraries.encrypt.HelpfulRSAKeyPair;
import online.umbcraft.libraries.network.errors.RadioError;
import online.umbcraft.libraries.network.message.RadioMessage;
import online.umbcraft.libraries.network.message.ReasonMessage;
import online.umbcraft.libraries.network.message.ResponseMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;


/**
 * <p> Passes any incoming message on a single port to the appropriate {@link ReasonResponder} </p>
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
     * <p> Creates an empty PortListener </p>
     *
     * @param talkie <p> the {@link WalkieTalkie} instance this belongs to </p>
     * @param port   the port this listens on
     */
    public PortListener(WalkieTalkie talkie, int port) {
        this.talkie = talkie;
        responders = new TreeMap<>();
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
     * <p> Closes the server socket on this port </p>
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
     * <p> starts listening for {@link RadioMessage}s and responds with {@link ReasonResponder}s </p>
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
                    ReasonResponder responder = responders.get(job.getRemoteReason());
                    if (responder == null) throw new IllegalStateException("no valid reason specified");


                    error = RadioError.UNKNOWN_HOST;
                    if (!responder.isKnown(job.getRemotePub64()))
                        throw new IllegalStateException("host key is not recognized");

                    PublicKey remotePub = HelpfulRSAKeyPair.publicFrom64(job.getRemotePub64());

                    error = RadioError.INVALID_SIGNATURE;
                    if (!job.verifyRemoteSignature(remotePub))
                        throw new InvalidKeyException("message signature is invalid");

                    HelpfulRSAKeyPair selfPair = responder.getKeypair();

                    error = RadioError.BAD_CRYPT_KEY;
                    job.decodeRemote(selfPair.priv());

                    error = RadioError.INVALID_JSON;
                    ReasonMessage message = new ReasonMessage(job.getRemoteBody());

                    error = RadioError.REASON_MISMATCH;
                    if(!message.getReason().equals(job.getRemoteReason()))
                        throw new IllegalStateException("plaintext reason and encrypted reason do not match");

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
                    logger.severe("ERROR VALUE: " + error.name() + " - " + e.getClass().getSimpleName());
                }
            });
        }
    }
}
