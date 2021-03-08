package online.umbcraft.libraries;

/**
 * Responsible for responding to a single type/reason of {@link RadioMessage}<p>
 * Extend this class to add custom logic for whenever a RadioMessage is received
 *
 * @see ReasonResponder
 * @see RadioMessage
 */

public abstract class ReasonResponder {

    protected final String reason;


    /**
     * Creates a blank ReasonResponder and sets the reason
     *
     * @param reason String which if any {@link RadioMessage}<p> share, this will reply to them
     */
    public ReasonResponder(String reason) {
        this.reason = reason;
    }


    /**
     * Returns the {@link RadioMessage} reason this object responds to
     *
     * @return the type of message this responds to
     */
    public final String getReason() {
        return reason;
    }


    /**
     * generates a {@link RadioMessage} to be sent in response to an incoming {@link RadioMessage}
     *
     * @param message the incoming message
     * @return the generated reply
     */
    public abstract RadioMessage response(RadioMessage message);
}
