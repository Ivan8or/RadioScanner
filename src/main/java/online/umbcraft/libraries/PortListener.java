package online.umbcraft.libraries;

import online.umbcraft.libraries.encrypt.HelpfulAESKey;
import online.umbcraft.libraries.encrypt.MessageEncryptor;
import online.umbcraft.libraries.encrypt.HelpfulRSAKeyPair;

import javax.crypto.BadPaddingException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
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
     * starts listening for {@link RadioMessage}<p>
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
                clientSocket.setSoTimeout(3000);
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

                final ObjectInputStream ois;
                final ObjectOutputStream oos;

                boolean still_fine = true;

                try {
                    ois = new ObjectInputStream(clientSocket.getInputStream());
                    oos = new ObjectOutputStream(clientSocket.getOutputStream());
                } catch (IOException e) {
                    logger.severe("FAILED TO OPEN INPUT STREAMS");
                    still_fine = false;
                    e.printStackTrace();
                    return;
                }


                String encryptedKey = null;
                String msgSignature = null;
                String encryptedMessage = null;

                if (still_fine) {
                    try {
                        encryptedKey = ois.readUTF();
                        msgSignature = ois.readUTF();
                        encryptedMessage = ois.readUTF();
                    } catch (IOException e) {
                        logger.severe("INPUT STREAM CANT READ MESSAGE");
                        still_fine = false;
                        e.printStackTrace();
                    }
                }

                HelpfulAESKey AESkey = null;
                String resultBody = null;
                boolean validSignature = true;

                if (still_fine) {
                    try {
                        AESkey = new HelpfulAESKey(MessageEncryptor.decryptRSA(RSA_PAIR, encryptedKey));
                        resultBody = MessageEncryptor.decryptAES(AESkey, encryptedMessage);
                        validSignature = MessageEncryptor.verifySignature(RSA_PAIR, resultBody, msgSignature);
                    } catch (InvalidKeyException | BadPaddingException e) {
                        logger.severe("SENT MESSAGE USED BAD CRYPT KEY");
                        still_fine = false;
                    } catch (SignatureException e) {
                        validSignature = false;
                    }

                    if (!validSignature) {
                        logger.severe("SENT MESSAGE HAS BAD SIGNATURE");
                        still_fine = false;
                    }
                }

                if (still_fine) {
                    RadioMessage message = new RadioMessage(resultBody);
                    RadioMessage response = respond(message);
                    HelpfulAESKey newKey = new HelpfulAESKey();
                    String encryptedResponse = "";
                    String newEncryptedKey = "";
                    String newSignature = "";

                    try {
                        encryptedResponse = MessageEncryptor.encryptAES(newKey, response.toString());
                        newEncryptedKey = MessageEncryptor.encryptRSA(RSA_PAIR, newKey.key64());
                        newSignature = MessageEncryptor.generateSignature(RSA_PAIR, response.toString());
                    } catch (InvalidKeyException | SignatureException e) {
                        logger.severe("ERROR ENCRYPTING OUR MESSAGE");
                        still_fine = false;
                        e.printStackTrace();
                    }

                    try {
                        oos.writeUTF(newEncryptedKey);
                        oos.writeUTF(newSignature);
                        oos.writeUTF(encryptedResponse);
                    } catch (Exception e) {
                        logger.severe("FAILED TO SEND RESPONSE ACROSS");
                        e.printStackTrace();
                    }
                }

                try {

                    oos.flush();
                    ois.close();
                    oos.close();

                    clientSocket.close();

                } catch (Exception e) {
                    logger.severe("FAILED TO CLOSE STREAMS / SOCKET");
                    e.printStackTrace();

                }
            });


        }
    }
}
