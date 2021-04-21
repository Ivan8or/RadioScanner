package online.umbcraft.libraries.network.response;

import online.umbcraft.libraries.network.message.RadioMessage;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


/**
 * Handles all responding to {@link RadioMessage} messages <p>
 * <p>
 * holds multiple {@link ReasonResponder}instances<p>
 * Also holds a static instance of {@link ExecutorService},
 * which other RadioScanner classes use to run events asynchronously.
 *
 * @see ReasonResponder
 * @see RadioMessage
 */
public class WalkieTalkie {

    private static Logger logger = Logger.getLogger(WalkieTalkie.class.getSimpleName());
    private static ExecutorService executor = Executors.newCachedThreadPool();
    private Map<Integer, PortListener> scanners;
    private boolean debug;


    /**
     * Creates a blank WalkieTalkie
     *
     * @see WalkieTalkie
     */
    public WalkieTalkie() {

        scanners = new TreeMap<>();
    }


    /**
     * Sets the shared ExecutorService to be used for async events
     *
     * @param executor the ExecutorService to be used by all RadioScanner classes
     * @see ExecutorService
     */
    public static void setExecutor(ExecutorService executor) {
        WalkieTalkie.executor = executor;
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
     * @return the instance of {@link Logger} this class uses
     */
    public static Logger getLogger() {
        return logger;
    }


    /**
     * sets the class logger
     *
     * @param new_logger the instance of {@link Logger} to be used as the class logger
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
     * Gives whether or not this object is currently outputting debug statements to its {@link Logger}
     *
     * @return if this class is in debug mode
     */
    public synchronized boolean isDebugging() {
        return debug;
    }


    /**
     * Closes all listening {@link ReasonResponder}s within this object
     */
    public void stopListening() {
        if (debug)
            logger.info("stopping listening for all listeners in WalkieTalkie");
        for (PortListener listener : scanners.values()) {
            listener.stopListening();
        }
    }


    /**
     * Adds a {@link ReasonResponder} to this object on a certain port,
     * allowing that responder to react to {@link RadioMessage}s
     * sent to that port with a matching reason
     *
     * @param port      network port to listen on
     * @param responder {@link ReasonResponder} which responds to certain RadioMessages on the specified port
     */
    public void addResponse(int port, ReasonResponder responder) {

        if (debug)
            logger.info("adding ReasonResponder to WalkieTalkie with reason " + responder.getReason());

        if (!scanners.containsKey(port)) {
            PortListener listener = new PortListener(this, port);
            listener.start();
            scanners.put(port, listener);
        }
        scanners.get(port).addResponder(responder);
    }
}
