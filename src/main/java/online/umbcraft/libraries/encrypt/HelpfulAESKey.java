package online.umbcraft.libraries.encrypt;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;


/**
 * Wrapper class for a single AES key<p>
 */
public class HelpfulAESKey {


    private final SecretKey AES_KEY;


    /**
     * Creates a {@link HelpfulRSAKeyPair} containing a randomly generated key
     */
    public HelpfulAESKey() {
        KeyGenerator generator = null;
        try {
            generator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        generator.init(128);
        AES_KEY = generator.generateKey();
    }


    /**
     * Creates a {@link HelpfulAESKey} object containing an AES key
     *
     * @param key_b64 base64 encoded AES key
     */
    public HelpfulAESKey(String key_b64) {
        AES_KEY = keyFrom64(key_b64);
    }


    /**
     * Creates a {@link HelpfulAESKey} object containing an AES key
     *
     * @param key the AES key
     */
    public HelpfulAESKey(SecretKey key) {
        AES_KEY = key;
    }


    /**
     * Creates a {@link SecretKey} object from a base64 encoded AES key
     *
     * @param key_b64 base64 encoded AES key
     * @return the resulting SecretKey
     */
    public SecretKey keyFrom64(String key_b64) {
        byte[] key_bytes = Base64.decodeBase64(key_b64);
        return new SecretKeySpec(key_bytes, 0, key_bytes.length, "AES");
    }


    /**
     * returns the base64 encoded representation of the contained AES key
     *
     * @return the base64 encoded key
     */
    public String key64() {
        return Base64.encodeBase64String(AES_KEY.getEncoded());
    }

    /**
     * returns the contained AES key
     *
     * @return the AES key
     */
    public SecretKey key() {
        return AES_KEY;
    }
}
