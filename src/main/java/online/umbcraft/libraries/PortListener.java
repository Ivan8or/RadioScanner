package online.umbcraft.libraries;

//import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/*
PortListener CLass

A message listener for a single port
can contain multiple responders, one for any unique message reason
 */

public class PortListener extends Thread {

    private static final Logger logger = WalkieTalkie.getLogger();
    private final String RSA_PRIVATE_KEY;
    private final String RSA_PUBLIC_KEY;
    private final int PORT;

    private ServerSocket server_listener;
    private Map<String, ReasonResponder> responders;
    private WalkieTalkie talkie;

    public PortListener(WalkieTalkie talkie, int port, String pub_key_b64, String priv_key_b64) {
        this.talkie = talkie;
        responders = new HashMap<>(5);
        this.PORT = port;
        RSA_PUBLIC_KEY = pub_key_b64;
        RSA_PRIVATE_KEY = priv_key_b64;
    }

    public int getPort() {
        return PORT;
    }

    // adds a new response for a certain RadioMessage reason
    public void addResponder(ReasonResponder responder) {
        String reason = responder.getReason();
        responders.put(reason, responder);
    }

    public Collection<ReasonResponder> getResponders() {
        return responders.values();
    }

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

    private RadioMessage respond(RadioMessage message) {

        if (talkie.isDebugging())
            logger.info("responding to message " + message);

        ReasonResponder responder = responders.get(message.get("reason"));
        RadioMessage response;
        if (responder == null)
            response = new RadioMessage()
                    .put("success", "false")
                    .put("reason", "no_valid_reason");
        else
            response = responder.response(message);

        if (talkie.isDebugging())
            logger.info("response is " + response);

        return response;
    }

    // starts a ServerSocket that continuously listens for radio messages, and automatically replies
    // to any that this has a set response for (or gives a generic response to any it doesnt have)
    @Override
    public void run() {

        try {
            server_listener = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                Socket clientSocket = server_listener.accept();

                if (talkie.isDebugging())
                    logger.info("receiving message from IP " + clientSocket.getInetAddress());

                clientSocket.setSoTimeout(3000);

                WalkieTalkie.sharedExecutor().submit(() -> {

                    ObjectInputStream ois = null;
                    ObjectOutputStream oos = null;

                    boolean still_fine = true;

                    try {
                        ois = new ObjectInputStream(clientSocket.getInputStream());
                        oos = new ObjectOutputStream(clientSocket.getOutputStream());
                    } catch (Exception e) {
                        logger.severe("FAILED TO OPEN INPUT STREAMS");
                        still_fine = false;
                    }


                    String encryptedKey = null;
                    String msgSignature = null;
                    String encryptedMessage = null;

                    if (still_fine) {
                        try {
                            encryptedKey = ois.readUTF();
                            msgSignature = ois.readUTF();
                            encryptedMessage = ois.readUTF();
                        } catch (Exception e) {
                            logger.severe("SENT MESSAGE RECEIVED BAD RESPONSE... NOT RESPONDING");
                            still_fine = false;
                        }
                    }


                    MessageEncryptor encryptor = null;
                    String AESKey = null;
                    String resultBody = null;
                    boolean validSignature = false;

                    if (still_fine) {
                        try {
                            encryptor = new MessageEncryptor(RSA_PUBLIC_KEY, RSA_PRIVATE_KEY);
                            AESKey = encryptor.decryptRSA(encryptedKey);
                            resultBody = encryptor.decryptAES(encryptedMessage, AESKey);
                            validSignature = encryptor.verifySignature(resultBody, msgSignature);
                        } catch (Exception e) {
                            logger.severe("SENT MESSAGE USED BAD CRYPT KEY... NOT RESPONDING");
                            still_fine = false;
                        }
                        if (!validSignature) {
                            logger.severe("SENT MESSAGE HAS BAD SIGNATURE... NOT RESPONDING");
                            still_fine = false;
                        }
                    }


                    if (still_fine) {
                        RadioMessage message = new RadioMessage(resultBody);
                        RadioMessage response = respond(message);

                        String newKey_b64 = MessageEncryptor.genAESKey();
                        String encryptedResponse = encryptor.encryptAES(response.toString(), newKey_b64);
                        String newEncryptedKey = encryptor.encryptRSA(newKey_b64);
                        String newSignature = encryptor.generateSignature(response.toString());

                        try {
                            oos.writeUTF(newEncryptedKey);
                            oos.writeUTF(newSignature);
                            oos.writeUTF(encryptedResponse);
                        } catch (Exception e) {
                            logger.severe("FAILED TO SEND RESPONSE ACROSS");
                        }
                    }

                    try {
                        if (oos != null)
                            oos.flush();
                        if (ois != null)
                            ois.close();
                        if (oos != null)
                            oos.close();

                        clientSocket.close();
                    } catch (Exception e) {
                        logger.severe("FAILED TO CLOSE STREAMS / SOCKET");
                    }
                });

            } catch (Exception e) {
                if (server_listener.isClosed()) {
                    logger.info("server listener is closed!");
                    return;
                }
                e.printStackTrace();
                logger.severe("ERRORED OUT - NO LONGER LISTENING ON PORT " + PORT);
            }
        }
    }
}
