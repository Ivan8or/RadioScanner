package online.umbcraft.libraries;

import online.umbcraft.libraries.encrypt.HelpfulAESKey;
import online.umbcraft.libraries.encrypt.MessageEncryptor;
import online.umbcraft.libraries.message.RadioMessage;

import javax.crypto.BadPaddingException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;


/**
 * Handles socket reads and writes for the {@link RadioMessage} and {@link PortListener} classes
 * <p>
 * A single socket used for a single transaction between client and server
 * encrypts the message as it sends it across
 */
public class RadioSocket {

    final private Socket socket;
    final private ObjectOutputStream oos;
    final private ObjectInputStream ois;

    final private PublicKey remote_pub;
    final private PrivateKey self_priv;

    final private MessageData message;
    final private MessageData response;


    /**
     * Creates a RadioSocket from a Socket, and assigns it a remove public key and own private key to use while encrypting
     *
     * @param socket     Socket object to be used for the transaction
     * @param remote_pub remote socket's public key, to be used for encrypting our message
     * @param self_priv  our socket's private key, to be used for decrypting their message
     */
    public RadioSocket(Socket socket, PublicKey remote_pub, PrivateKey self_priv) throws IOException {
        this.socket = socket;
        socket.setSoTimeout(3000);
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
        this.remote_pub = remote_pub;
        this.self_priv = self_priv;

        message = new MessageData();
        response = new MessageData();
    }


    /**
     * Creates a RadioSocket using an IP and port number
     * assigns it a remove public key and own private key to use while encrypting
     *
     * @param ip         the IP to connect to
     * @param port       the port to connect to
     * @param remote_pub remote socket's public key, to be used for encrypting our message
     * @param self_priv  our socket's private key, to be used for decrypting their message
     */
    public RadioSocket(final String ip, final int port, PublicKey remote_pub, PrivateKey self_priv) throws IOException {
        this.socket = new Socket(ip, port);
        socket.setSoTimeout(3000);
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
        this.remote_pub = remote_pub;
        this.self_priv = self_priv;

        message = new MessageData();
        response = new MessageData();
    }


    /**
     * Encrypts and sends a message to the destination port
     *
     * @param to_write the body of the message to be transmitted
     * @param reason   the reason for the message being sent
     */
    public void setMessage(String to_write, String reason) {
        message.body = to_write;
        message.reason = reason;
    }


    /**
     * Encrypts and sends a message to the destination port
     */
    public void encodeMessage() throws InvalidKeyException, SignatureException, IOException {
        message.key = new HelpfulAESKey();
        message.key_enc = MessageEncryptor.encryptRSA(remote_pub, message.key.key64());
        message.body_enc = MessageEncryptor.encryptAES(message.key, message.body);
        message.signature = MessageEncryptor.generateSignature(self_priv, message.body);

        oos.flush();
    }


    /**
     * Encrypts and sends a message to the destination port
     */
    public void sendMessage() throws IOException {

        oos.writeUTF(message.reason);
        oos.writeUTF(message.key_enc);
        oos.writeUTF(message.signature);
        oos.writeUTF(message.body_enc);
        oos.flush();
    }


    /**
     * receives a message from the remote port
     */
    public void receiveResponse() throws IOException {
        response.reason = ois.readUTF();
        response.key_enc = ois.readUTF();
        response.signature = ois.readUTF();
        response.body_enc = ois.readUTF();
    }


    /**
     * decodes the received message body
     */
    public void decodeResponse() throws BadPaddingException, InvalidKeyException {
        response.key = new HelpfulAESKey(MessageEncryptor.decryptRSA(self_priv, response.key_enc));
        response.body = MessageEncryptor.decryptAES(response.key, response.body_enc);
    }


    /**
     * verifies an RSA signature of the received body of text
     *
     * @return whether the signature is valid
     */
    public Boolean verifySignature() throws SignatureException, InvalidKeyException {
        return MessageEncryptor.verifySignature(remote_pub, response.body, response.signature);
    }

    public String getResponse() {
        return response.body;
    }

    /**
     * closes all streams / sockets used by this object
     */
    public void close() throws IOException {
        oos.close();
        ois.close();
        socket.close();
    }


    private class MessageData {
        private String reason;

        private String body;
        private String body_enc;

        private HelpfulAESKey key;
        private String key_enc;

        private String signature;
    }

}
