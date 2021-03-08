package online.umbcraft.libraries;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Handles all responding to <a href="#{@link}">{@link RadioMessage}</a> messages <p>
 * <p>
 * holds multiple <a href="#{@link}">{@link ReasonResponder}</a> instances<p>
 * Also holds a static instance of <a href="#{@link}">{@link ExecutorService}</a>,
 * which other RadioScanner classes use to run events asynchronously.
 *
 * @see ReasonResponder
 * @see RadioMessage
 */
public class WalkieTalkie {

    private static Logger logger = Logger.getLogger(WalkieTalkie.class.getSimpleName());
    private static ExecutorService executor = Executors.newCachedThreadPool();
    private final String RSA_PUBLIC_B64;
    private final String RSA_PRIVATE_B64;
    private Map<Integer, PortListener> scanners;
    private boolean debug;


    /**
     * Creates a blank WalkieTalkie containing RSA keypair for use in responding to messages
     *
     * @param pub_key_b64  base 64 public RSA key
     * @param priv_key_b64 base 64 private RSA key
     * @see WalkieTalkie
     */
    public WalkieTalkie(String pub_key_b64, String priv_key_b64) {

        if (debug)
            logger.info("creating WalkieTalkie");

        scanners = new HashMap<>(2);
        RSA_PUBLIC_B64 = pub_key_b64;
        RSA_PRIVATE_B64 = priv_key_b64;
    }


    public static void main(String[] args) {
        WalkieTalkie a = new WalkieTalkie("", "");
        sharedExecutor();
        a.enableDebug();
        getLogger();
        setLogger(null);
        a.isDebugging();
        a.stopListening();
        a.addResponse(1, null);
        RadioMessage aa = null;
        aa.clear();
        aa.merge(aa);
        aa.get("");
        aa.sendE("aa",1);
    }


    /**
     * Gives the shared ExecutorService used for async events
     *
     * @return the ExecutorService shared by all RadioScanner classes
     * @see ExecutorService
     */
    public static ExecutorService sharedExecutor() {
        return executor;
    }


    /**
     * gets the currently used logger
     *
     * @return the instance of <a href="#{@link}">{@link Logger}</a> this class uses
     */
    public static Logger getLogger() {
        return logger;
    }


    /**
     * sets the class logger
     *
     * @param new_logger the instance of <a href="#{@link}">{@link Logger}</a> to be used as the class logger
     */
    public static void setLogger(Logger new_logger) {
        logger = new_logger;
    }


    /**
     * Enables logger output for most actions performed by this object
     */
    public void enableDebug() {
        logger.info("debugging enabled for WalkieTalkie");
        debug = true;
    }


    /**
     * Disables logger output for this object
     */
    public void disableDebug() {
        logger.info("debugging disabled for WalkieTalkie");
        debug = false;
    }


    /**
     * Gives whether or not this object is currently outputting debug statements to its <a href="#{@link}">{@link Logger}</a>
     *
     * @return if this class is in debug mode
     */
    public synchronized boolean isDebugging() {
        return debug;
    }


    /**
     * Closes all listening <a href="#{@link}">{@link ReasonResponder}</a>s within this object
     */
    public void stopListening() {
        if (debug)
            logger.info("stopping listening for all listeners in WalkieTalkie");
        for (PortListener listener : scanners.values()) {
            listener.stopListening();
        }
    }


    /**
     * Adds a <a href="#{@link}">{@link ReasonResponder}</a> to this object on a certain port,
     * allowing that responder to react to <a href="#{@link}">{@link RadioMessage}</a>s
     * sent to that port with a matching reason
     *
     * @param port      network port to listen on
     * @param responder <a href="#{@link}">{@link ReasonResponder}</a> which responds to certain RadioMessages on the specified port
     */
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
