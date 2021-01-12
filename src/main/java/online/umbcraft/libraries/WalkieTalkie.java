package online.umbcraft.libraries;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
WalkieTalkie CLass

Responsible for holding all responses to messages sent to the plugin across the network
Also owns the ExecutorService which the other network-related classes use to run asynchronously
 */

public class WalkieTalkie {

    private static ExecutorService executor = Executors.newCachedThreadPool();
    private Map<Integer, PortListener> scanners;
    private static String RSA_PUBLIC_B64;
    private static String RSA_PRIVATE_B64;

    public WalkieTalkie(String pub_key_b64, String priv_key_b64) {
        scanners = new HashMap<>(2);
        RSA_PUBLIC_B64 = pub_key_b64;
        RSA_PRIVATE_B64 = priv_key_b64;
    }

    // halts any listening serversockets
    public void stopListening() {
        for(PortListener listener: scanners.values()) {
            listener.stopListening();
        }
    }

    // gives the executorservice which may hold any hot threads,
    // using which would allow for better use of resources compared to making a new thread :)
    public static ExecutorService sharedExecutor() {
        return executor;
    }

    public void addResponse(int port, ReasonResponder responder) {
        if(scanners.get(port) == null) {
            PortListener listener = new PortListener(port, RSA_PUBLIC_B64, RSA_PRIVATE_B64);
            listener.start();
            scanners.put(port, listener);
        }
        scanners.get(port).addResponder(responder);
    }
}
