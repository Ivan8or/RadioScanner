import org.apache.commons.codec.binary.Base64;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MessageEncryptor {

    private static final String PUBLIC_KEY_B64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0b8eOiR6EFHlAdPxabPaLJ1Ca8mi0JuYIdFjlvm1WIxp0X6YNBe24mQke1TZam+8jkjVnVvh/z9oNqrTa4s6O6hHfbBS4Hz3IqXGUg3sHE3vB7tbw1IcaF0/ps6z7LCLT6vwUqtNowoUbfMrnUxvwx7A01zoFy5MkxOWo1BANoPhW2o3bDOcNXlFBCXJrPqDOjov0YcHN5dkbXf5TkkKR4PF7SmbC1UaE10pLcFI2DvV9uOT9HF3Y6SGIYHqZdC5txxWPbxtDTL+JnhG94mAsxB1+grRjsIKcybk2gGITJSqXSgMzODsRwxI+BUcmz/ehA0I7WMs3mBCkId5T25sWwIDAQAB";
    private static final String PRIVATE_KEY_B64 = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDRvx46JHoQUeUB0/Fps9osnUJryaLQm5gh0WOW+bVYjGnRfpg0F7biZCR7VNlqb7yOSNWdW+H/P2g2qtNrizo7qEd9sFLgfPcipcZSDewcTe8Hu1vDUhxoXT+mzrPssItPq/BSq02jChRt8yudTG/DHsDTXOgXLkyTE5ajUEA2g+FbajdsM5w1eUUEJcms+oM6Oi/Rhwc3l2Rtd/lOSQpHg8XtKZsLVRoTXSktwUjYO9X245P0cXdjpIYhgepl0Lm3HFY9vG0NMv4meEb3iYCzEHX6CtGOwgpzJuTaAYhMlKpdKAzM4OxHDEj4FRybP96EDQjtYyzeYEKQh3lPbmxbAgMBAAECggEBAKLqJ7hRjN4QPihlTyYjJFty6px+SZBuyoO59/jUjB7zMV4fTmln4g2Gay/Af/9EOtjpicJoE+oGfhO4K+9kfR7JXn4p7rWfWvX2fUxOtA5oGjmPNX9Iz/fgNgiClQ5LMmn3yvrrQ6pXsfaU4lOTg1uzD2MczsjylVfH50GCm926uJULgP/hUow1FB31E5qD0hGHlKhoSANPRo4gF8QccjukNoFKdv1gQta5iLvweUPJYS0rVM455naOCP1Md1L4rSaP205M6TFOe5fxElLiOycadpKrDuDF6+mb6M+3IMlwRzmZ0PeStSKJsKOdYsHGzn5Q+De/DHI8bnn2j5LqL8ECgYEA/C7v6rRAX9zqxvW6WNacDSAwl/DlWNlFMwdPvf4NF7NbVpmtcXxvIQbMiK122TJaPdTREOq2OzQpNkwnUMC179/TjA31wzhNXpbtcuHWzRziSyR0SGiIYr221LMINOV0lsXr037T3ARw3pbDo+p4EfpCuC+zu3lDWkUJObNa+B0CgYEA1OvDXzViOamq2tFCmMpXo5PvFs+1g4Cu63KYiA5aK6sZLQkMwcJHdJF4sPgOn9NAE19x0B2nLPmrB636n0fD2F5mgyUnTf1VFUoJgm7UoKFggo5JpLrHsV3uyJm3tDMZZQMX9hwF/8dXH/ahn3qemXUSvc7T/kh2gHZvrTIifNcCgYBTXazgOOBDEIPoa9lDfwatbCPERtV6jrDKkrMwyqhHWnpqYXkt2AXgtB+vWzC70mJ0qELIxd6iKbcqBPjGQD6k4qhLV14UQCuLhndOkAvzWIYScyWhvjS/95lWLS0cV3I4WYuBKh8dT7aETvCz4lH5F3Mw8kwHQKocUFAhbAI5nQKBgGMgS/nUUaGE0w1CZR3a/ggixCm7k8bgLw9gb5DQFbzE0Fi+INlICJpFa4oAQla4M9mREtyQIZd3uN8/aTGkaJIgCNu/fVf6vBTIPjsiPA14dAT0F2cAqh1yHMv5fKQBMi5rzUj/7O8SsAYqfT5RCOtqrMn/M4Tr2XefLAjXEWVLAoGABvKTjzYlGSosaxKtcXrUyElU5fJfbHMlcEiLv5yjANfKRMKllnJ6899LCgGUo6qvk+G+YILEKzm25Rygq2XApk1BasCYfxxO0deRO6T9J4tXfOWNc/m4fBZ/qvHwOe4QImJTGXCCV7SUGoo8/wke/OYGxyt7oF84dAVeRT+dVlA=";

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

    public static String generateSignature(String input) {
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

    public static boolean verifySignature(String input, String signature_b64) {
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

    public static String encryptRSA(String input) {

        PublicKey public_key = makePubKey(PUBLIC_KEY_B64);
        PrivateKey private_key = makePrivKey(PRIVATE_KEY_B64);
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

    public static String decryptAES(String input_b64, String key_b64) {

        SecretKey key = makeAESKey(key_b64);
        byte[] plainText = new byte[0];

        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.DECRYPT_MODE, key);

            plainText = cipher.doFinal(Base64.decodeBase64(input_b64));

        } catch (Exception e) {
            System.err.println("Error decrypting... returning empty array!");
            e.printStackTrace();
            System.err.println("Error decrypting... returned empty array!");
        }
        return new String(plainText);
    }

    public static String decryptRSA(String input_b64) {

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

    private static SecretKey makeAESKey(byte[] key_bytes) {
        return new SecretKeySpec(key_bytes, 0, key_bytes.length, "AES");
    }

    private static SecretKey makeAESKey(String key_b64) {
        byte[] key_bytes = Base64.decodeBase64(key_b64);
        return makeAESKey(key_bytes);
    }
}