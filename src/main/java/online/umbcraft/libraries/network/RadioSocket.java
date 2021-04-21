package online.umbcraft.libraries.network;

import online.umbcraft.libraries.encrypt.HelpfulAESKey;
import online.umbcraft.libraries.encrypt.MessageEncryptor;
import online.umbcraft.libraries.network.message.RadioMessage;
import online.umbcraft.libraries.network.response.PortListener;

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

    final private MessageData message;
    final private MessageData remote;


    /**
     * Creates a RadioSocket from a Socket, and assigns it a remove public key and own private key to use while encrypting
     *
     * @param socket     Socket object to be used for the transaction
     */
    public RadioSocket(Socket socket) throws IOException {
        this.socket = socket;
        socket.setSoTimeout(3000);
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());

        message = new MessageData();
        remote = new MessageData();
    }


    /**
     * Creates a RadioSocket using an IP and port number
     * assigns it a remove public key and own private key to use while encrypting
     *
     * @param ip         the IP to connect to
     * @param port       the port to connect to
     */
    public RadioSocket(final String ip, final int port) throws IOException {
        this.socket = new Socket(ip, port);
        socket.setSoTimeout(3000);
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());

        message = new MessageData();
        remote = new MessageData();
    }


    /**
     * Encrypts and sends a message to the destination port
     *
     * @param to_write the body of the message to be transmitted
     * @param reason   the reason for the message being sent
     */
    public void setMessage(String to_write, String reason, String public_key) {
        message.body = to_write;
        message.reason = reason;
        message.public_key = public_key;
    }


    /**
     * Encrypts and sends a message to the destination port
     * @param remote_pub remote socket's public key, to be used for encrypting our message
     * @param self_priv  our socket's private key, to be used for decrypting their message
     */
    public void encodeMessage(PublicKey remote_pub, PrivateKey self_priv) throws InvalidKeyException, SignatureException, IOException {
        message.aes_key = new HelpfulAESKey();
        message.key_enc = MessageEncryptor.encryptRSA(remote_pub, message.aes_key.key64());
        message.body_enc = MessageEncryptor.encryptAES(message.aes_key, message.body);
        message.signature = MessageEncryptor.generateSignature(self_priv, message.body);

        oos.flush();
    }


    /**
     * Encrypts and sends a message to the destination port
     */
    public void sendMessage() throws IOException {
        oos.writeUTF(message.reason);
        oos.writeUTF(message.public_key);
        oos.writeUTF(message.key_enc);
        oos.writeUTF(message.signature);
        oos.writeUTF(message.body_enc);
        oos.flush();
    }


    /**
     * receives a message from the remote port
     */
    public void receiveRemote() throws IOException {
        remote.reason = ois.readUTF();
        remote.public_key = ois.readUTF();
        remote.key_enc = ois.readUTF();
        remote.signature = ois.readUTF();
        remote.body_enc = ois.readUTF();
    }


    /**
     * decodes the received message body
     *
     * @param self_priv  our socket's private key, to be used for decrypting their message
     */
    public void decodeRemote(PrivateKey self_priv) throws BadPaddingException, InvalidKeyException {
        remote.aes_key = new HelpfulAESKey(MessageEncryptor.decryptRSA(self_priv, remote.key_enc));
        remote.body = MessageEncryptor.decryptAES(remote.aes_key, remote.body_enc);
    }


    /**
     * verifies an RSA signature of the received body of text
     *
     * @param remote_pub remote socket's public key, to be used for encrypting our message
     *
     * @return whether the signature is valid
     */
    public Boolean verifyRemoteSignature(PublicKey remote_pub) throws SignatureException, InvalidKeyException {
        return MessageEncryptor.verifySignature(remote_pub, remote.body, remote.signature);
    }

    public String getRemoteBody() {
        return remote.body;
    }

    public String getRemoteReason() {
        return remote.reason;
    }

    public String getRemotePub() {
        return remote.public_key;
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

        private HelpfulAESKey aes_key;
        private String key_enc;

        private String public_key;

        private String signature;
    }

}