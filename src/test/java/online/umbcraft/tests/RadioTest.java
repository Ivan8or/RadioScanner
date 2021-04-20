package online.umbcraft.tests;

import online.umbcraft.libraries.RadioMessage;
import online.umbcraft.libraries.ReasonResponder;
import online.umbcraft.libraries.WalkieTalkie;
import online.umbcraft.libraries.encrypt.HelpfulRSAKeyPair;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class RadioTest {

    @Test
    public void testServerMessage() {

        HelpfulRSAKeyPair keys = new HelpfulRSAKeyPair();
        WalkieTalkie walkie = new WalkieTalkie(keys);

        walkie.addResponse(24000, new ReasonResponder("testsuite", keys) {

            @Override
            public RadioMessage response(RadioMessage message) {

                return new RadioMessage()
                        .put("success","true");
            }
        });

        String answer = null;

        try {
            RadioMessage sending = new RadioMessage()
                    .put("reason","testsuite")
                    .setRSAKeys(keys)
                    .send("127.0.0.1:24000")
                    .get();
            answer = sending.get("success");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(answer,"true");
    }
}
