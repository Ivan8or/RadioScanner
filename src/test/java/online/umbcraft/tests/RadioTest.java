package online.umbcraft.tests;

import online.umbcraft.libraries.message.RadioMessage;
import online.umbcraft.libraries.ReasonResponder;
import online.umbcraft.libraries.WalkieTalkie;
import online.umbcraft.libraries.encrypt.HelpfulRSAKeyPair;
import online.umbcraft.libraries.message.ReasonMessage;
import online.umbcraft.libraries.message.ResponseMessage;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class RadioTest {

    @Test
    public void testServerMessage() {

        HelpfulRSAKeyPair keys = new HelpfulRSAKeyPair();
        WalkieTalkie walkie = new WalkieTalkie();

        walkie.addResponse(24000, new ReasonResponder("testsuite", keys) {

            @Override
            public ResponseMessage response(ReasonMessage message) {
                int value = Integer.parseInt(message.get("value"));
                return new ResponseMessage()
                        .put("returnval",value*2+"")
                        .put("success","true");
            }
        });

        String answer = null;

        for(int i = 0; i < 5; i++) {
            try {
                ResponseMessage sending = new ReasonMessage()
                        .setReason("testsuite")
                        .put("value", ""+i)
                        .setRSAKeys(keys)
                        .send("127.0.0.1:24000")
                        .get();
                answer = sending.get("returnval");
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            Assert.assertEquals(answer, ""+i*2);
        }
    }
}
