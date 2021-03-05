package online.umbcraft.libraries;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/*
PortListener CLass

A message listener for a single port
can contain multiple responders, one for any unique message reason
 */

public class PortListener extends Thread {

    private String RSA_PRIVATE_KEY;
    private String RSA_PUBLIC_KEY;

    private int port;
    private boolean running;
    private ServerSocket server_listener;
    private Map<String, ReasonResponder> responders;

    public PortListener(int port, String pub_key_b64, String priv_key_b64) {
        responders = new HashMap<>(5);
        this.port = port;
        RSA_PUBLIC_KEY = pub_key_b64;
        RSA_PRIVATE_KEY = priv_key_b64;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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
        running = false;
        if (server_listener != null) {
            try {
                server_listener.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private RadioMessage respond(RadioMessage message) {
        ReasonResponder responder = responders.get(message.get("reason"));
        RadioMessage response;
        if (responder == null)
            response = new RadioMessage()
                    .put("success", "false")
                    .put("reason", "invalid_reason");
        else
            response = responder.response(message);

        return response;
    }

    @Override
    // starts a ServerSocket that continuously listens for radio messages, and automatically replies
    // to any that this has a set response for (or gives a generic response to any it doesnt have)
    public void run() {
        running = true;
        try {
            server_listener = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (running) {
            try {
                Socket clientSocket = server_listener.accept();
                System.out.println("receiving message from IP " + clientSocket.getInetAddress());
                clientSocket.setSoTimeout(3000);
                WalkieTalkie.sharedExecutor().submit(() -> {
                    try {

                        ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                        ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());

                        String encryptedKey = null;
                        String msgSignature = null;
                        String encryptedMessage = null;
                        try {
                            encryptedKey = ois.readUTF();
                            msgSignature = ois.readUTF();
                            encryptedMessage = ois.readUTF();
                        } catch (Exception e) {
                            System.err.println("BAD MESSAGE FORMAT");
                            return;
                        }


                        MessageEncryptor encryptor = null;
                        String AESKey = null;
                        String resultBody = null;
                        boolean validSignature = false;
                        try {
                            encryptor = new MessageEncryptor(RSA_PUBLIC_KEY, RSA_PRIVATE_KEY);
                            AESKey = encryptor.decryptRSA(encryptedKey);
                            resultBody = encryptor.decryptAES(encryptedMessage, AESKey);
                            validSignature = encryptor.verifySignature(resultBody, msgSignature);
                        } catch (Exception e) {
                            System.err.println("BAD KEY - DECRYPTION FAILED!");
                        }

                        if (!validSignature) {
                            System.err.println("SIGNATURE INVALID - IGNORING MESSAGE!");
                            return;
                        }

                        RadioMessage message = new RadioMessage(resultBody);
                        RadioMessage response = respond(message);

                        String newKey_b64 = MessageEncryptor.genAESKey();
                        String encryptedResponse = encryptor.encryptAES(response.toString(), newKey_b64);
                        String newEncryptedKey = encryptor.encryptRSA(newKey_b64);
                        String newSignature = encryptor.generateSignature(response.toString());

                        oos.writeUTF(newEncryptedKey);
                        oos.writeUTF(newSignature);
                        oos.writeUTF(encryptedResponse);

                        oos.flush();

                        ois.close();
                        oos.close();
                        clientSocket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                if(server_listener.isClosed())
                    System.out.println("closed listener on port "+port);
                else {
                    e.printStackTrace();
                    System.err.println("errored out - no longer listening on port " + port);
                }
            }
        }
    }
}
