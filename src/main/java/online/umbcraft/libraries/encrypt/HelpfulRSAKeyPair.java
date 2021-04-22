package online.umbcraft.libraries.encrypt;

import org.apache.commons.codec.binary.Base64;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


/**
 * <p>Wrapper class for a single RSA keypair</p>
 */
public class HelpfulRSAKeyPair {


    private final PublicKey PUBLIC_KEY;
    private final PrivateKey PRIVATE_KEY;


    /**
     * Creates a {@link HelpfulRSAKeyPair} containing a randomly generated RSA keypair
     */
    public HelpfulRSAKeyPair() {

        KeyPairGenerator generator = null;

        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        generator.initialize(2048);
        KeyPair secKey = generator.generateKeyPair();
        PUBLIC_KEY = secKey.getPublic();
        PRIVATE_KEY = secKey.getPrivate();
    }


    /**
     * Creates a {@link HelpfulRSAKeyPair} containing the specified keyset
     *
     * @param pub_key_b64  the public RSA key encoded in base64
     * @param priv_key_b64 the private RSA key encoded in base64
     * @throws InvalidKeySpecException if either key is not valid RSA
     */
    public HelpfulRSAKeyPair(String pub_key_b64, String priv_key_b64) throws InvalidKeySpecException {

        PUBLIC_KEY = publicFrom64(pub_key_b64);
        PRIVATE_KEY = privateFrom64(priv_key_b64);
    }


    /**
     * Creates a {@link HelpfulRSAKeyPair} containing the specified keyset
     *
     * @param pub_key  the public RSA key
     * @param priv_key the private RSA key
     */
    public HelpfulRSAKeyPair(PublicKey pub_key, PrivateKey priv_key) {

        PUBLIC_KEY = pub_key;
        PRIVATE_KEY = priv_key;
    }


    /**
     * Creates an RSA {@link PublicKey} from a base64 public key string
     *
     * @param pub_b64 the public RSA key encoded in base64
     * @return the created PublicKey object
     * @throws InvalidKeySpecException if the key is not a valid RSA key
     */
    public static PublicKey publicFrom64(String pub_b64) throws InvalidKeySpecException {

        PublicKey newPublic = null;

        try {
            newPublic = KeyFactory.getInstance("RSA").generatePublic(
                    new X509EncodedKeySpec(Base64.decodeBase64(pub_b64)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return newPublic;
    }


    /**
     * Creates an RSA {@link PrivateKey} from a base64 public key string
     *
     * @param priv_b64 the private RSA key encoded in base64
     * @return the created PrivateKey object
     * @throws InvalidKeySpecException if the key is not a valid RSA key
     */
    public static PrivateKey privateFrom64(String priv_b64) throws InvalidKeySpecException {

        PrivateKey newPrivate = null;

        try {

            newPrivate = KeyFactory.getInstance("RSA").generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.decodeBase64(priv_b64)));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return newPrivate;
    }


    /**
     * returns the base64 encoded representation of the contained public RSA key
     *
     * @return the base64 encoded public key
     */
    public String pub64() {
        return Base64.encodeBase64String(PUBLIC_KEY.getEncoded());
    }


    /**
     * returns the base64 encoded representation of the contained private RSA key
     *
     * @return the base64 encoded private key
     */
    public String priv64() {
        return Base64.encodeBase64String(PRIVATE_KEY.getEncoded());
    }


    /**
     * returns the contained RSA public key
     *
     * @return the public key
     */
    public PublicKey pub() {
        return PUBLIC_KEY;
    }


    /**
     * returns the contained RSA private key
     *
     * @return the private key
     */
    public PrivateKey priv() {
        return PRIVATE_KEY;
    }
}
