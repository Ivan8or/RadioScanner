package online.umbcraft.libraries.network.errors;


import online.umbcraft.libraries.network.message.RadioMessage;
import online.umbcraft.libraries.network.response.ReasonResponder;

/**
 * <p> All possible errors which can be returned by a failed{@link RadioMessage} </p>
 * <p> the format that the{@link RadioMessage} will contain the error in is: </p>
 * "TRANSMIT_ERROR" = "{@link RadioError}"
 */
public enum RadioError {


    /**
     * receiver did not have a valid{@link ReasonResponder}
     * to be able to respond to this message
     */
    NO_VALID_REASON,


    /**
     * the {@link ReasonResponder} errored while responding to this message
     */
    ERROR_ON_RESPONSE,


    /**
     * {@link RadioMessage} failed to connect to the specified IPv4 address
     */
    FAILED_TO_CONNECT,


    /**
     * {@link RadioMessage} encountered an issue with reading from the socket
     */
    BAD_NETWORK_READ,


    /**
     * {@link RadioMessage} encountered an issue with writing to the socket
     */
    BAD_NETWORK_WRITE,


    /**
     * {@link RadioMessage} was not able to decrypt the reply with its RSA / AES key
     */
    BAD_CRYPT_KEY,


    /**
     * the public key of the remote message is not familiar
     */
    UNKNOWN_HOST,


    /**
     * {@link RadioMessage} the json format of the response was invalid
     */
    INVALID_JSON,


    /**
     * {@link RadioMessage} encountered a bad signature from the reply
     */
    INVALID_SIGNATURE;

}
