package online.umbcraft.libraries;



/**
 * All possible errors which can be returned by a failed <a href="#{@link}">{@link RadioMessage}</a><p>
 * the format that the <a href="#{@link}">{@link RadioMessage}</a> will contain the error in is:<p>
 * "TRANSMIT_ERROR" = "<a href="#{@link}">{@link RadioError}</a>"
 */
public enum RadioError {


    /**
     * receiver did not have a valid <a href="#{@link}">{@link ReasonResponder}</a>
     * to be able to respond to this message
     */
    NO_VALID_REASON,


    /**
     * <a href="#{@link}">{@link RadioMessage}</a> failed to connect to the specified IPv4 address
     */
    FAILED_TO_CONNECT,


    /**
     * <a href="#{@link}">{@link RadioMessage}</a> encountered an issue with reading / writing to the socket
     */
    BAD_NETWORK_RESPONSE,


    /**
     * <a href="#{@link}">{@link RadioMessage}</a> was not able to decrypt the reply with its RSA key
     */
    BAD_RSA_KEY,


    /**
     * <a href="#{@link}">{@link RadioMessage}</a> encountered a bad signature from the reply
     */
    INVALID_SIGNATURE;

}
