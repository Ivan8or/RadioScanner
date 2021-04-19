package online.umbcraft.libraries.encrypt;

import org.apache.commons.codec.binary.Base64;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;


/**
 * Wrapper class for a single RSA keypair<p>
 */
public class HelpfulRSAKeyPair {


    private final PublicKey PUBLIC_KEY;
    private final PrivateKey PRIVATE_KEY;


    /**
     * Creates a {@link HelpfulRSAKeyPair} containing a randomly generated keypair
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
     * Creates a {@link HelpfulRSAKeyPair} containing the specified keypair
     *
     * @param keypair       an RSA keypair array encoded in base64 (index 0 is public, index 1 is private)
     */
    public HelpfulRSAKeyPair(String[] keypair) {

        PublicKey newPublic = null;
        PrivateKey newPrivate = null;

        try {
            newPublic = KeyFactory.getInstance("RSA").generatePublic(
                    new X509EncodedKeySpec(Base64.decodeBase64(keypair[0])));
            newPrivate = KeyFactory.getInstance("RSA").generatePrivate(
                    new X509EncodedKeySpec(Base64.decodeBase64(keypair[1])));

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        PUBLIC_KEY = newPublic;
        PRIVATE_KEY = newPrivate;
    }


    /**
     * Creates a {@link HelpfulRSAKeyPair} containing the specified keyset
     *
     * @param pub_key_b64       the public RSA key encoded in base64
     * @param priv_key_b64      the private RSA key encoded in base64
     */
    public HelpfulRSAKeyPair(String pub_key_b64, String priv_key_b64) {

        PublicKey newPublic = null;
        PrivateKey newPrivate = null;

        try {
            newPublic = KeyFactory.getInstance("RSA").generatePublic(
                    new X509EncodedKeySpec(Base64.decodeBase64(pub_key_b64)));
            newPrivate = KeyFactory.getInstance("RSA").generatePrivate(
                    new X509EncodedKeySpec(Base64.decodeBase64(priv_key_b64)));

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        PUBLIC_KEY = newPublic;
        PRIVATE_KEY = newPrivate;
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
