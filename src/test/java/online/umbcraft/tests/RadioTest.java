package online.umbcraft.tests;

import online.umbcraft.libraries.network.response.ReasonResponder;
import online.umbcraft.libraries.network.response.WalkieTalkie;
import online.umbcraft.libraries.encrypt.HelpfulRSAKeyPair;
import online.umbcraft.libraries.network.message.ReasonMessage;
import online.umbcraft.libraries.network.message.ResponseMessage;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class RadioTest {

    @Test
    public void testServerMessage() {

        HelpfulRSAKeyPair server_keys = new HelpfulRSAKeyPair();
        HelpfulRSAKeyPair client_keys = new HelpfulRSAKeyPair();

        WalkieTalkie walkie = new WalkieTalkie();

        ReasonResponder responder = new ReasonResponder("testsuite", server_keys) {

            @Override
            public ResponseMessage response(ReasonMessage message) {
                int value = Integer.parseInt(message.get("value"));
                return new ResponseMessage()
                        .put("returnval", value * 2 + "")
                        .setSuccess(true);
            }
        };
        responder.addKnown(client_keys.pub64());

        walkie.addResponse(24000, responder);

        String answer = null;
        boolean success = false;

        try {
            ResponseMessage sending = new ReasonMessage()
                    .setReason("testsuite")
                    .put("value", "2")
                    .setRSAKeys(client_keys)
                    .setRemoteKey(server_keys.pub())
                    .send("127.0.0.1:24000")
                    .get();
            answer = sending.get("returnval");
            success = sending.getSuccess();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(success);
        Assert.assertEquals(answer, "4");

    }
}
