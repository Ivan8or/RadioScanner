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

public class RadioSocket {

    final private Socket socket;
    final private ObjectOutputStream oos;
    final private ObjectInputStream ois;

    final private PublicKey remote_pub;
    final private PrivateKey self_priv;


    public RadioSocket(final String ip, final int port, PublicKey remote_pub, PrivateKey self_priv) throws IOException {
        this.socket = new Socket(ip, port);
        socket.setSoTimeout(3000);
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
        this.remote_pub = remote_pub;
        this.self_priv = self_priv;
    }


    public void sendMessage(String to_write) throws InvalidKeyException, SignatureException, IOException {
        HelpfulAESKey AESkey = new HelpfulAESKey();

        oos.writeUTF( MessageEncryptor.encryptRSA(remote_pub, AESkey.key64()) );
        oos.writeUTF( MessageEncryptor.encryptAES(AESkey, to_write) );
        oos.writeUTF( MessageEncryptor.generateSignature(self_priv, to_write) );

        oos.flush();
    }


    public String receiveMessage() throws IOException, BadPaddingException, InvalidKeyException {

        HelpfulAESKey aeskey = new HelpfulAESKey(MessageEncryptor.decryptRSA(self_priv, ois.readUTF()));
        return MessageEncryptor.decryptAES(aeskey, ois.readUTF());

    }


    public Boolean verifySignature(String body) throws IOException, SignatureException, InvalidKeyException {
        return MessageEncryptor.verifySignature(remote_pub, body, ois.readUTF());
    }

    public void close() throws IOException {
        oos.close();
        ois.close();
        socket.close();
    }

}
