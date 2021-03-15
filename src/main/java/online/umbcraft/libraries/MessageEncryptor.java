package online.umbcraft.libraries;

import org.apache.commons.codec.binary.Base64;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/*
MessageEncryptor CLass

A class to assist in encrypting strings via AES and via RSA, as well as creating RSA signatures
can also generate RSA keypairs as an added benefit

holds a single RSA keypair
 */



/**
 * Handles AES, RSA encrypting, decrypting, and RSA signing Strings<p>
 * can also generate RSA keypairs
 *
 * instances hold a single RSA keypair for use in encryption
 */
public class MessageEncryptor {

    private final String PUBLIC_KEY_B64;
    private final String PRIVATE_KEY_B64;


    /**
     * Creates a {@link MessageEncryptor} which uses the specified RSA keyset
     *
     * @param keyset_b64 array containing the base64 RSA keyset <p>(index 0 is public, index 1 is private)
     */
    public MessageEncryptor(String[] keyset_b64) {
        this.PUBLIC_KEY_B64 = keyset_b64[0];
        this.PRIVATE_KEY_B64 = keyset_b64[1];
    }


    /**
     * Creates a {@link MessageEncryptor} which uses the specified RSA keyset
     *
     * @param pub_key_b64       the public RSA key in base64
     * @param priv_key_b64      the private RSA key in base64
     */
    public MessageEncryptor(String pub_key_b64, String priv_key_b64) {
        this.PUBLIC_KEY_B64 = pub_key_b64;
        this.PRIVATE_KEY_B64 = priv_key_b64;
    }


    /**
     * Generates a random 2048 sized RSA keypair
     *
     * @return an array containing the generated keypair
     */
    public static String[] genRSAKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair secKey = generator.generateKeyPair();

            String public_key = Base64.encodeBase64String(secKey.getPublic().getEncoded());
            String private_key = Base64.encodeBase64String(secKey.getPrivate().getEncoded());

            return new String[]{public_key, private_key};

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Generates a base64 random 128 bit AES key
     *
     * @return the generated base64 AES key
     */
    public static String genAESKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(128);
            SecretKey secKey = generator.generateKey();
            return Base64.encodeBase64String(secKey.getEncoded());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Signs a string using the stored RSA private key
     *
     * @param input         the raw string input
     *
     * @return the          signature encoded in base64
     */
    public String generateSignature(String input) {
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(makePrivKey(PRIVATE_KEY_B64));
            sign.update(input.getBytes());
            byte[] sig_bytes = sign.sign();

            return Base64.encodeBase64String(sig_bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Verifies an RSA signature for a string using its RSA keyset
     *
     * @param input             the raw string which was signed
     * @param signature_b64     the signature in question (encoded in base64)
     *
     * @return whether or not the signature is valid
     */
    public boolean verifySignature(String input, String signature_b64) {
        try {
            Signature verifying = Signature.getInstance("SHA256withRSA");
            verifying.initVerify(makePubKey(PUBLIC_KEY_B64));
            verifying.update(input.getBytes());

            return verifying.verify(Base64.decodeBase64(signature_b64));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Encrypts a string using a base64 AES key
     *
     * @param input         the raw text input
     * @param AESKey_b64    the base64 encoded AES key to be used
     *
     * @return the encrypted string (encoded in base64)
     */
    public static String encryptAES(String input, String AESKey_b64) {
        SecretKey key = makeAESKey(AESKey_b64);
        byte[] cipherText = new byte[0];

        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            cipherText = cipher.doFinal(input.getBytes());

        } catch (Exception e) {
            System.err.println("Error encrypting... returning empty array!");
            e.printStackTrace();
            System.err.println("Error encrypting... returned empty array!");
        }
        return Base64.encodeBase64String(cipherText);
    }


    /**
     * Encrypts a string using the internal RSA keyset
     *
     * @param input         the raw text input
     *
     * @return the encrypted string (encoded in base64)
     */
    public String encryptRSA(String input) {

        PublicKey public_key = makePubKey(PUBLIC_KEY_B64);
        byte[] cipherText = new byte[0];

        try {

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, public_key);
            cipher.update(input.getBytes());

            cipherText = cipher.doFinal();

        } catch (Exception e) {
            System.err.println("Error encrypting... returning empty array!");
            e.printStackTrace();
            System.err.println("Error encrypting... returned empty array!");
        }

        return Base64.encodeBase64String(cipherText);
    }


    /**
     * Decrypts a string using an AES key
     *
     * @param input_b64         the base64 encoded encrypted string
     * @param key_b64           the base64 encoded AES key
     *
     * @return the raw decrypted string
     */
    public static String decryptAES(String input_b64, String key_b64) {

        SecretKey key = makeAESKey(key_b64);
        byte[] plainText = new byte[0];

        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.DECRYPT_MODE, key);

            plainText = cipher.doFinal(Base64.decodeBase64(input_b64));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(plainText);
    }


    /**
     * Decrypts a string using the internal RSA keyset
     *
     * @param input_b64         the base64 encoded encrypted string
     *
     * @return the raw decrypted string
     */
    public String decryptRSA(String input_b64) {

        PrivateKey private_key = makePrivKey(PRIVATE_KEY_B64);
        byte[] plainText = new byte[0];

        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

            cipher.init(Cipher.DECRYPT_MODE, private_key);

            plainText = cipher.doFinal(Base64.decodeBase64(input_b64));

        } catch (Exception e) {
            System.err.println("Error decrypting... returning empty array!");
            e.printStackTrace();
            System.err.println("Error decrypting... returned empty array!");
        }
        return new String(plainText);
    }


    /**
     * Creates a {@link PublicKey} object from a base64 encoded RSA public key
     *
     * @param pub_key_b64       the base64 encoded public RSA key
     *
     * @return the generated {@link PublicKey}
     */
    private static PublicKey makePubKey(String pub_key_b64) {
        try {
            KeyFactory.getInstance("RSA");
            PublicKey publicKey =
                    KeyFactory.getInstance("RSA").generatePublic(
                            new X509EncodedKeySpec(Base64.decodeBase64(pub_key_b64)));
            return publicKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Creates a {@link PrivateKey} object from a base64 encoded RSA private key
     *
     * @param priv_key_b64       the base64 encoded private RSA key
     *
     * @return the generated {@link PrivateKey}
     */
    private static PrivateKey makePrivKey(String priv_key_b64) {
        try {
            KeyFactory.getInstance("RSA");
            PrivateKey privateKey =
                    KeyFactory.getInstance("RSA").generatePrivate(
                            new PKCS8EncodedKeySpec(Base64.decodeBase64(priv_key_b64)));
            return privateKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Creates a {@link SecretKey} object from a byte array AES key
     *
     * @param key_bytes         the AES key in the form of a byte array
     *
     * @return the generated {@link SecretKey}
     */
    private static SecretKey makeAESKey(byte[] key_bytes) {
        return new SecretKeySpec(key_bytes, 0, key_bytes.length, "AES");
    }


    /**
     * Creates a {@link SecretKey} object from a base64 encoded AES key
     *
     * @param key_b64           base64 encoded AES key
     *
     * @return the generated {@link SecretKey}
     */
    private static SecretKey makeAESKey(String key_b64) {
        byte[] key_bytes = Base64.decodeBase64(key_b64);
        return makeAESKey(key_bytes);
    }
}
