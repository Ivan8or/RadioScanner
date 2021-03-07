package online.umbcraft.libraries;

//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/*
WalkieTalkie CLass

Responsible for holding all responses to messages sent to the plugin across the network
Also owns the ExecutorService which the other network-related classes use to run asynchronously
 */

public class WalkieTalkie {

    private static Logger logger = Logger.getLogger(WalkieTalkie.class.getSimpleName());
    private static ExecutorService executor = Executors.newCachedThreadPool();
    private static String RSA_PUBLIC_B64;
    private static String RSA_PRIVATE_B64;
    private Map<Integer, PortListener> scanners;
    private boolean debug;

    public WalkieTalkie(String pub_key_b64, String priv_key_b64) {

        if (debug)
            logger.info("creating WalkieTalkie");

        scanners = new HashMap<>(2);
        RSA_PUBLIC_B64 = pub_key_b64;
        RSA_PRIVATE_B64 = priv_key_b64;
    }

    public static void main(String[] args) {
        RadioMessage m = new RadioMessage();
        m.enableDebug();
        m.put("a","b");
    }

    // gives the executorservice which may hold any hot threads,
    // using which would allow for better use of resources compared to making a new thread :)
    public static ExecutorService sharedExecutor() {
        return executor;
    }

    public void enableDebug() {
        logger.info("debugging enabled for WalkieTalkie");
        debug = true;
    }

    public void disableDebug() {
        logger.info("debugging disabled for WalkieTalkie");
        debug = false;
    }

    public static void initLogger() {
        //if(!Logger.getRootLogger().getAllAppenders().hasMoreElements())
         //   BasicConfigurator.configure();
    }
    public static void setLogger(Logger new_logger) {
        logger = new_logger;
    }

    public static Logger getLogger() {
        return logger;
    }


    public synchronized boolean isDebugging() {
        return debug;
    }

    // halts all listening ports
    public void stopListening() {
        if (debug)
            logger.info("stopping listening for all listeners in WalkieTalkie");
        for (PortListener listener : scanners.values()) {
            listener.stopListening();
        }
    }

    public void addResponse(int port, ReasonResponder responder) {

        if (debug)
            logger.info("adding ReasonResponder to WalkieTalkie with reason " + responder.getReason());

        if (scanners.get(port) == null) {
            PortListener listener = new PortListener(this, port, RSA_PUBLIC_B64, RSA_PRIVATE_B64);
            listener.start();
            scanners.put(port, listener);
        }
        scanners.get(port).addResponder(responder);
    }
}
