package online.umbcraft.libraries;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*
WalkieTalkie CLass

Responsible for holding all responses to messages sent to the plugin across the network
Also owns the ExecutorService which the other network-related classes use to run asynchronously
 */

public class WalkieTalkie {

    private static final Logger logger = Logger.getLogger(WalkieTalkie.class.getSimpleName());
    private static ExecutorService executor = Executors.newCachedThreadPool();
    private static String RSA_PUBLIC_B64;
    private static String RSA_PRIVATE_B64;
    private Map<Integer, PortListener> scanners;
    private boolean debug;

    public WalkieTalkie(String pub_key_b64, String priv_key_b64) {

        if (debug)
            logger.debug("creating WalkieTalkie");

        scanners = new HashMap<>(2);
        RSA_PUBLIC_B64 = pub_key_b64;
        RSA_PRIVATE_B64 = priv_key_b64;
    }


    public static void main(String[] args) {

        BasicConfigurator.configure();

        // creating a new keypair... DO NOT DO THIS IN PRODUCTION!
        // this is for test purposes, you would normally want to create a single keypair and store it
        // instead of making a new one each time
        String[] keypair = MessageEncryptor.genRSAKeyPair();

        // creating the server side
        WalkieTalkie talkie = new WalkieTalkie(keypair[0], keypair[1]);
        talkie.enableDebug();

        // adding a response to the main object
        // also setting port on which this responder listens
        talkie.addResponse(25540,
                new ReasonResponder("do_i_have_enough_animals") {
                    @Override
                    public RadioMessage response(RadioMessage message) {
                        RadioMessage toReturn = new RadioMessage();

                        String how_many_cats = message.get("cats_owned");
                        String how_many_dogs = message.get("dogs_owned");

                        int cats_int = 0;
                        int dogs_int = 0;
                        try {
                            cats_int = Integer.parseInt(how_many_cats);
                            dogs_int = Integer.parseInt(how_many_dogs);

                        } catch (Exception e) {
                            toReturn.put("success", "false");
                            toReturn.put("fail-reason", "number convert error");
                            return toReturn;
                        }
                        if (cats_int > 1)
                            toReturn.put("enough_cats", "yes");
                        else
                            toReturn.put("enough_cats", "no");

                        if (dogs_int > 1)
                            toReturn.put("enough_dogs", "yes");
                        else
                            toReturn.put("enough_dogs", "no");
                        return toReturn;
                    }
                }
        );

        // sending a 'do_i_have_enough_animals' message from a client
        RadioMessage toSend = new RadioMessage();
        toSend.enableDebug();

        // setting reason (key val must be 'reason'), it's special!!!
        toSend.put("reason", "do_i_have_enough_animals");

        // setting other relevant data
        toSend.put("cats_owned", "2");
        toSend.put("dogs_owned", "6");

        // giving rsa keypair to message so it knows how to encrypt it
        toSend.setRSAKeys(keypair[0], keypair[1]);

        // sending the message to the server and getting a future back
        Future<RadioMessage> response = toSend.send("127.0.0.1", 25540);

        // getting the value from the future! (
        // will block thread, so do only if you're okay with
        // lag since you're waiting for stuff to go over the network)
        RadioMessage response_msg = null;
        try {
            response_msg = response.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // displaying results to user
        String enough_cats = response_msg.get("enough_cats");
        String enough_dogs = response_msg.get("enough_dogs");

        System.out.println("response body!: " + response_msg);
        System.out.println("enough cats? " + enough_cats);
        System.out.println("enough dogs? " + enough_dogs);


        // stop server since it will otherwise run forever
        talkie.stopListening();

    }

    // gives the executorservice which may hold any hot threads,
    // using which would allow for better use of resources compared to making a new thread :)
    public static ExecutorService sharedExecutor() {
        return executor;
    }

    public void enableDebug() {
        logger.debug("debugging enabled for WalkieTalkie");
        debug = true;
    }

    public void disableDebug() {
        logger.debug("debugging disabled for WalkieTalkie");
        debug = false;
    }

    public synchronized boolean isDebugging() {
        return debug;
    }

    // halts all listening ports
    public void stopListening() {
        if (debug)
            logger.debug("stopping listening for all listeners in WalkieTalkie");
        for (PortListener listener : scanners.values()) {
            listener.stopListening();
        }
        executor.shutdown();
    }

    public void addResponse(int port, ReasonResponder responder) {

        if (debug)
            logger.debug("adding ReasonResponder to WalkieTalkie with reason " + responder.getReason());

        if (scanners.get(port) == null) {
            PortListener listener = new PortListener(this, port, RSA_PUBLIC_B64, RSA_PRIVATE_B64);
            listener.start();
            scanners.put(port, listener);
        }
        scanners.get(port).addResponder(responder);
    }
}
