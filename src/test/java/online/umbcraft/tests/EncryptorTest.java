package online.umbcraft.tests;

import online.umbcraft.libraries.encrypt.HelpfulAESKey;
import online.umbcraft.libraries.encrypt.HelpfulRSAKeyPair;
import online.umbcraft.libraries.encrypt.MessageEncryptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import java.security.InvalidKeyException;
import java.security.SignatureException;

public class EncryptorTest {

    protected String aeskey = null;

    protected String public_key = null;
    protected String private_key = null;
    protected String message = null;
    protected String message_sig = null;
    protected String message_rsa = null;
    protected String message_aes = null;


    @Before
    public void setUp() {

        aeskey = "aI/wxO4rdTANsqQ5kv0arQ==";

        public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoosCcFarrRCPXaN2Q2bSLy86w7tsz/s+PhWPPoO+Ffg6tV6uhYjIXGpFs5UAXJaz3zvimcicVAonQOLUs0Oo2xLbnje4fG+2EoxP3jhuP3ItPVddzHCRVSZWX2pSJI9iJ5RyV+tvkAb8SqdlaLWGxI8He/Jvxu7zzXYwyQzpWiA9y9lXY7QPvL6mXZDCCBcNi5FCeph1LJrmnSaQ6GcjNlpsYd5dFS+Qyczp2ZRpj7b2roeY24kdY6rklkwIxLe1TFecmkEftZlFv30XNU/uSIAUxoV9GK7ZmOaibs9MXIT3ggx3h/74AS6HDQS27mw1/eCw0WTXRCV3TiI0P4hLyQIDAQAB";
        private_key = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCiiwJwVqutEI9do3ZDZtIvLzrDu2zP+z4+FY8+g74V+Dq1Xq6FiMhcakWzlQBclrPfO+KZyJxUCidA4tSzQ6jbEtueN7h8b7YSjE/eOG4/ci09V13McJFVJlZfalIkj2InlHJX62+QBvxKp2VotYbEjwd78m/G7vPNdjDJDOlaID3L2VdjtA+8vqZdkMIIFw2LkUJ6mHUsmuadJpDoZyM2Wmxh3l0VL5DJzOnZlGmPtvauh5jbiR1jquSWTAjEt7VMV5yaQR+1mUW/fRc1T+5IgBTGhX0YrtmY5qJuz0xchPeCDHeH/vgBLocNBLbubDX94LDRZNdEJXdOIjQ/iEvJAgMBAAECggEANm6s62fq5/MCv83s3deCDbEYUdHjN6cgifP4W2wM7RiKuJAzi55p0qD4JJKCY39ITzqjwPIwf+hCivUrW7kNxAeFqm9ohat3YrDAUl+9c9vNkJxAWiVcVhKXnC5jwcCOk0igHhdG7gmY1XtHd5BKyGpJjFV83zbISIcLAVkkkDxuGnpLNrVjuMUMsJxER4sQ0MuO5w4NLSlQQm0GQbF9qTexak5Yz8nTucC43F83lXWeyXBst/HE6oXepLpQBBqa9E+rSllboHL1BykEgGdVmTPjonS8fVCVZIJMDZnKhyuKoVu3gfV0X/rFJ0mseaYqodPIunN4XpSi0AJeUoS0LQKBgQD8XPXiA+gEbcHldOAna6zhYcS3XwLgosSQ3uQLW95aQRUp7K/LJ7zl5smFqkol82tiNeOdWja/jK8A3VtWIEJ+IqWJ/TWIDGE0LFY9+J4qtlG02oCve7t0Ce6LGZXTrS+xu3GkP4mC0OeYHpbQg1AxMiw+93W9+N5ysXfMZZr6OwKBgQCk4q1QzluDFs1g+QJKnknnh/w1OhBngtgyQ1AmQ+cTjkTp+Juzjfp+hSHl/1kmAogpjdgV9LNwmsBLOdVEF+rK5+yQH3VF00wvLYfIww8sRtKxo5/w8bQzug+uimGbev5r2Ys4RxsuvefC67CukK9TcwPckRZLIr5LU3sORP6tywKBgEvdznWR4xBhxqaqijCPqDthXvgZcg4CIMHEoB3iEmhjpG8kHn0ehyU+SlsKpJvgH+o4dSL62faf3oSOB0VPEW5GECn5mzw5LIe4xHyA31vLz6hk/8bBlcr+qV3b5TTrnaj6xuEv0LEpCFas633n3BXOFYsgbZ8c0BL+0xr5eKu9AoGAGt61z81RLHOy1HK/5NkWwhxvBV2I/tVb1hUSIqo+JfmxUU+qUAB7KdMYSxrptEAFYIA1Dp7p/xe47olmL8qWAKr66iG2QsLET4vj7NucxWJlQ32euu0oqKVRmOgEMApDG+A/x9QOofdgpWszR79cUwUTRAaA04295RidnR2xQzECgYBx+lnYetSe5HNrwWE72A0an3yHUBcQhqVAOBqHtwm8ZDRbkWy/7M12nv0R4sI9pJSD0S9TGRxzGC9FOkZ1CG+YcAKZzhfQ9mRlTIlPaIffGDX3bCKJsWzRjtRgBylWh75CUBdTwhnnz6r9F6FAWRuLbcxv456LMqdL5+Zy/Vhqtg==";
        message = "apples are delicious!";
        message_sig = "TPUQQSmuZ1zBzzCzAwr4HSqHHrnnbvSypUuIrb1ATuyMW+T3DGsnPEndbGpJ4DRhtZIiJFDFkDxSfyZzmiHpn2RQhWgv1J5uJOZyi7qOsy6OdELQCCQ5YjscWMQCQBvoWSkkF10uedmDqHFm8gIxeLgmcaOSKHF04CKQikbMRlrZ4tT5ws7Bcz2XWMK5iPfdM2Y/RfjLjLqq6dbyjd+M4BkkDjDYxF8hXF1qb2nUqvipZQWYbha5YBs4E9sdE0hTaSdFwOgDeKQBA6VwgIpIBn0IdsjdMGqtoWo6tC3k+OuEpjwGB4UErbEk/0tugdSFX1wt1kGmzb9wWmWbbJVraw==";

        message_rsa = "lYNYk1pTpxK6FoaHYI0CaWQ3lDroHM/4dlIA32xvvPiqnNLdmim3S+U7KVszEB+j4366pdn1nXEBuvmr3j2jYOcZizRwTXeQpqSafhWZci6ge6KB5u9kINXTProo0VeZrVVJs9dA1Ob345QxP/rKnRsRKIlHzAgaPgkBRYH+ziP+eviS193nnfb+HEnxB3lu2/xDkxz0NVq0LIpfIvRs5tAYvv5NiVBB35EcPijVD1y03Su1pEneyaModFds46pz13D17FvX/blz+NX/Eg3dfjv7EcL0sl+f1Fa9MCmBkbH6c9SyJ3KFnqacQQ78rufxPH3jcNrRS+wh5lZXHDeshg==";
        message_aes = "RqejPU37AmorFazoac5cJziBq3Hth9Ae3wf2PyAbu7A=";

    }


    @Test
    public void initHelpfuls() {
        HelpfulRSAKeyPair pair = new HelpfulRSAKeyPair(public_key, private_key);
        HelpfulAESKey key = new HelpfulAESKey(aeskey);

        Assert.assertEquals(pair.pub64(), public_key);
        Assert.assertEquals(pair.priv64(), private_key);
        Assert.assertEquals(key.key64(), aeskey);
    }


    @Test
    public void AESencrypt() {

        HelpfulAESKey aesk = new HelpfulAESKey(aeskey);

        String result = null;
        try {
            result = MessageEncryptor.encryptAES(aesk, message);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(result, message_aes);
    }


    @Test
    public void AESdecrypt() {

        HelpfulAESKey aesk = new HelpfulAESKey(aeskey);

        String result = null;
        try {
            result = MessageEncryptor.decryptAES(aesk, message_aes);
        } catch (InvalidKeyException | BadPaddingException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(result, message);
    }


    @Test
    public void RSAdecrypt() {

        HelpfulRSAKeyPair pair = new HelpfulRSAKeyPair(public_key, private_key);

        String result = null;
        try {
            result = MessageEncryptor.decryptRSA(pair, message_rsa);
        } catch (InvalidKeyException | BadPaddingException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(result, message);
    }


    @Test
    public void RSAencryptDecrypt() {
        HelpfulRSAKeyPair pair = new HelpfulRSAKeyPair(public_key, private_key);

        String result = null;
        try {
            String encrypted = MessageEncryptor.encryptRSA(pair, message);
            result = MessageEncryptor.decryptRSA(pair, encrypted);
        } catch (InvalidKeyException | BadPaddingException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(result, message);
    }


    @Test
    public void signatureGenerate() {
        HelpfulRSAKeyPair pair = new HelpfulRSAKeyPair(public_key, private_key);

        String signature = null;
        try {
            signature = MessageEncryptor.generateSignature(pair, message);
        } catch (InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(signature, message_sig);
    }

    @Test
    public void signatureCheck() {

        HelpfulRSAKeyPair pair = new HelpfulRSAKeyPair(public_key, private_key);

        boolean result = false;
        try {
            result = MessageEncryptor.verifySignature(pair, message, message_sig);
        } catch (InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(result);
    }

}