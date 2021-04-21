package online.umbcraft.libraries.network;

import online.umbcraft.libraries.encrypt.HelpfulAESKey;
import online.umbcraft.libraries.encrypt.MessageEncryptor;
import online.umbcraft.libraries.network.message.RadioMessage;
import online.umbcraft.libraries.network.response.PortListener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;


/**
 * <p> Handles socket reads and writes for the {@link RadioMessage} and {@link PortListener} classes </p>
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
     * @param socket Socket object to be used for the transaction
     * @throws IOException if something went wrong when creating the input/output streams
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
     * @param ip   the IP to connect to
     * @param port the port to connect to
     * @throws IOException if something went wrong creating the socket or creating the input/output streams
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
     * @param to_write       the body of the message to be transmitted
     * @param reason         the reason for the message being sent
     * @param public_key_b64 the local RSA public key to be sent along with the message
     */
    public void setMessage(String to_write, String reason, String public_key_b64) {
        message.body = to_write;
        message.reason = reason;
        message.public_key = public_key_b64;
    }


    /**
     * Encrypts and sends a message to the destination port
     *
     * @param remote_pub remote socket's public key, to be used for encrypting our message
     * @param self_priv  our socket's private key, to be used for decrypting their message
     * @throws InvalidKeyException if the remote RSA key is bad
     * @throws SignatureException  if something goes wrong signing the message (bad private key?)
     */
    public void encodeMessage(PublicKey remote_pub, PrivateKey self_priv) throws InvalidKeyException, SignatureException {
        message.aes_key = new HelpfulAESKey();
        message.aeskey_enc = MessageEncryptor.encryptRSA(remote_pub, message.aes_key.key64());
        message.body_enc = MessageEncryptor.encryptAES(message.aes_key, message.body);
        message.signature = MessageEncryptor.generateSignature(self_priv, message.body_enc);
    }


    /**
     * Encrypts and sends a message to the destination port
     *
     * @throws IOException if an error was encountered writing to the remote socket
     */
    public void sendMessage() throws IOException {
        oos.writeUTF(message.reason);
        oos.writeUTF(message.public_key);
        oos.writeUTF(message.aeskey_enc);
        oos.writeUTF(message.signature);
        oos.writeUTF(message.body_enc);
        oos.flush();
    }


    /**
     * receives a message from the remote port
     *
     * @throws IOException if an error was encountered reading from the remote socket
     */
    public void receiveRemote() throws IOException {
        remote.reason = ois.readUTF();
        remote.public_key = ois.readUTF();
        remote.aeskey_enc = ois.readUTF();
        remote.signature = ois.readUTF();
        remote.body_enc = ois.readUTF();
    }


    /**
     * decodes the received message body
     *
     * @param self_priv our socket's private key, to be used for decrypting their message
     * @throws InvalidKeyException if our private key didn't work to decode the message
     */
    public void decodeRemote(PrivateKey self_priv) throws InvalidKeyException {
        remote.aes_key = new HelpfulAESKey(MessageEncryptor.decryptRSA(self_priv, remote.aeskey_enc));
        remote.body = MessageEncryptor.decryptAES(remote.aes_key, remote.body_enc);
    }


    /**
     * verifies an RSA signature of the received body of text
     *
     * @param remote_pub remote socket's public key, to be used for encrypting our message
     * @return whether the signature is valid
     * @throws SignatureException if the signature couldn't be validated
     * @throws InvalidKeyException if the public key is bad
     */
    public Boolean verifyRemoteSignature(PublicKey remote_pub) throws SignatureException, InvalidKeyException {
        return MessageEncryptor.verifySignature(remote_pub, remote.body_enc, remote.signature);
    }

    /**
     * get the remote body
     * @return the body of the remote message
     */
    public String getRemoteBody() {
        return remote.body;
    }

    /**
     * get the remote reason
     * @return the reason for the remote message being sent
     */
    public String getRemoteReason() {
        return remote.reason;
    }

    /**
     * get the remote public key
     * @return the public key received from the remote message
     */
    public String getRemotePub() {
        return remote.public_key;
    }

    /**
     * closes all streams / sockets used by this object
     * @throws IOException if something goes wrong while closing the socket / io streams
     */
    public void close() throws IOException {
        oos.close();
        ois.close();
        socket.close();
    }


    /**
     * all information pertaining to a single message
     */
    private class MessageData {

        /**
         * the reason this message was/is being sent
         */
        private String reason;

        /**
         * the plaintext body of the message
         */
        private String body;

        /**
         * the AES-encoded body of the message
         */
        private String body_enc;

        /**
         * the AES key with which the body of this message was encoded
         */
        private HelpfulAESKey aes_key;

        /**
         * this message's AES key encoded by the public RSA key of the recipient (known in advance)
         */
        private String aeskey_enc;

        /**
         * the public RSA key of the sender, to be used for verification / knowing how to encrypt the response
         */
        private String public_key;

        /**
         * a signature generated by the sender using their private key
         */
        private String signature;
    }

}
