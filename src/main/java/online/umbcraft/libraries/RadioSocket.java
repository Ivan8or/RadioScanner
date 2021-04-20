package online.umbcraft.libraries;

import online.umbcraft.libraries.encrypt.HelpfulAESKey;
import online.umbcraft.libraries.encrypt.MessageEncryptor;

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
    }


    /**
     * Encrypts and sends a message to the destination port
     *
     * @param to_write the body of the message to be transmitted
     */
    public void sendMessage(String to_write) throws InvalidKeyException, SignatureException, IOException {
        HelpfulAESKey AESkey = new HelpfulAESKey();

        oos.writeUTF(MessageEncryptor.encryptRSA(remote_pub, AESkey.key64()));
        oos.writeUTF(MessageEncryptor.encryptAES(AESkey, to_write));
        oos.writeUTF(MessageEncryptor.generateSignature(self_priv, to_write));

        oos.flush();
    }


    /**
     * receives and decrypts a message from the remote port
     *
     * @return the body of the received message
     */
    public String receiveMessage() throws IOException, BadPaddingException, InvalidKeyException {

        HelpfulAESKey aeskey = new HelpfulAESKey(MessageEncryptor.decryptRSA(self_priv, ois.readUTF()));
        return MessageEncryptor.decryptAES(aeskey, ois.readUTF());

    }


    /**
     * reeives and verifies an RSA signature of a body of text
     *
     * @param body the raw body to be verified against the signature
     * @return whether the signature is valid
     */
    public Boolean verifySignature(String body) throws IOException, SignatureException, InvalidKeyException {
        return MessageEncryptor.verifySignature(remote_pub, body, ois.readUTF());
    }


    /**
     * closes all streams / sockets used by this object
     */
    public void close() throws IOException {
        oos.close();
        ois.close();
        socket.close();
    }

}
