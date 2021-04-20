package online.umbcraft.libraries.encrypt;

import org.apache.commons.codec.binary.Base64;

import java.security.*;
import javax.crypto.*;

/**
 * Handles AES, RSA encrypting, decrypting, and RSA signing Strings<p>
 */
public class MessageEncryptor {


    /**
     * Signs a string using the stored RSA private key
     *
     * @param pair  the RSA keypair from which the private key is used to make the signature
     * @param input the raw string input
     * @return the signature encoded in base64
     */
    public static String generateSignature(HelpfulRSAKeyPair pair, String input) throws InvalidKeyException, SignatureException {
        return generateSignature(pair.priv(), input);
    }


    /**
     * Signs a string using the stored RSA private key
     *
     * @param key   the private RSA key used to make the signature
     * @param input the raw string input
     * @return the signature encoded in base64
     */
    public static String generateSignature(PrivateKey key, String input) throws InvalidKeyException, SignatureException {
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(key);
            sign.update(input.getBytes());
            byte[] sig_bytes = sign.sign();

            return Base64.encodeBase64String(sig_bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Verifies an RSA signature for a string using its RSA keyset
     *
     * @param pair          the RSA keypair from which the public key is used to verify the signature
     * @param input         the raw string which was signed
     * @param signature_b64 the signature in question (encoded in base64)
     * @return whether or not the signature is valid
     */
    public static boolean verifySignature(HelpfulRSAKeyPair pair, String input, String signature_b64) throws InvalidKeyException, SignatureException {
        return verifySignature(pair.pub(), input, signature_b64);
    }


    /**
     * Verifies an RSA signature for a string using its RSA keyset
     *
     * @param key           the public key used to verify the signature
     * @param input         the raw string which was signed
     * @param signature_b64 the signature in question (encoded in base64)
     * @return whether or not the signature is valid
     */
    public static boolean verifySignature(PublicKey key, String input, String signature_b64) throws InvalidKeyException, SignatureException {
        try {
            Signature verifying = Signature.getInstance("SHA256withRSA");
            verifying.initVerify(key);
            verifying.update(input.getBytes());

            return verifying.verify(Base64.decodeBase64(signature_b64));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Encrypts a string using a base64 AES key
     *
     * @param AESkey the base64 encoded AES key to be used
     * @param input  the raw text input
     * @return the encrypted string (encoded in base64)
     */
    public static String encryptAES(HelpfulAESKey AESkey, String input) throws InvalidKeyException {
        SecretKey key = AESkey.key();
        byte[] cipherText = new byte[0];

        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            cipherText = cipher.doFinal(input.getBytes());

        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                BadPaddingException | IllegalBlockSizeException e) {
            System.err.println("Error encrypting... returning empty array!");
            e.printStackTrace();
            System.err.println("Error encrypting... returned empty array!");
        }
        return Base64.encodeBase64String(cipherText);
    }


    /**
     * Encrypts a string using the internal RSA keyset
     *
     * @param input the raw text input
     * @param pair  the RSA keypair from which the public key will be used to encrypt
     * @return the encrypted string (encoded in base64)
     */
    public static String encryptRSA(HelpfulRSAKeyPair pair, String input) throws InvalidKeyException {
        return encryptRSA(pair.pub(), input);
    }


    /**
     * Encrypts a string using the internal RSA keyset
     *
     * @param input the raw text input
     * @param key   the RSA key used to encrypt the message
     * @return the encrypted string (encoded in base64)
     */
    public static String encryptRSA(PublicKey key, String input) throws InvalidKeyException {

        PublicKey public_key = key;
        byte[] cipherText = new byte[0];

        try {

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, public_key);
            cipher.update(input.getBytes());

            cipherText = cipher.doFinal();

        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return Base64.encodeBase64String(cipherText);
    }


    /**
     * Decrypts a string using an AES key
     *
     * @param AESkey    the AES key to be used to decrypt
     * @param input_b64 the base64 encoded encrypted string
     * @return the raw decrypted string
     */
    public static String decryptAES(HelpfulAESKey AESkey, String input_b64) throws InvalidKeyException, BadPaddingException {

        SecretKey key = AESkey.key();
        byte[] plainText = new byte[0];

        try {
            Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.DECRYPT_MODE, key);

            plainText = cipher.doFinal(Base64.decodeBase64(input_b64));

        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return new String(plainText);
    }


    /**
     * Decrypts a string using the internal RSA keyset
     *
     * @param pair      the keypair containing the private key to be used to decrypt
     * @param input_b64 the base64 encoded encrypted string
     * @return the raw decrypted string
     */
    public static String decryptRSA(HelpfulRSAKeyPair pair, String input_b64) throws InvalidKeyException, BadPaddingException {
        return decryptRSA(pair.priv(), input_b64);
    }


    /**
     * Decrypts a string using the internal RSA keyset
     *
     * @param key       the private RSA key to be used to decrypt
     * @param input_b64 the base64 encoded encrypted string
     * @return the raw decrypted string
     */
    public static String decryptRSA(PrivateKey key, String input_b64) throws InvalidKeyException, BadPaddingException {

        PrivateKey private_key = key;
        byte[] plainText = new byte[0];

        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, private_key);

            plainText = cipher.doFinal(Base64.decodeBase64(input_b64));

        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | IllegalBlockSizeException e) {

            e.printStackTrace();
        }

        return new String(plainText);
    }
}
