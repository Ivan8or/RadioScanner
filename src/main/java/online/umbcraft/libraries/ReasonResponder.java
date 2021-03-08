package online.umbcraft.libraries;

/**
 * Responsible for responding to a single type/reason of <a href="#{@link}">{@link RadioMessage}</a><p>
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
     * @param reason String which if any <a href="#{@link}">{@link RadioMessage}</a><p> share, this will reply to them
     */
    public ReasonResponder(String reason) {
        this.reason = reason;
    }


    /**
     * Returns the <a href="#{@link}">{@link RadioMessage}</a> reason this object responds to
     */
    public final String getReason() {
        return reason;
    }


    /**
     * generates a <a href="#{@link}">{@link RadioMessage}</a> to be sent in response to an incoming <a href="#{@link}">{@link RadioMessage}</a>
     *
     * @param message the incoming message
     * @return the generated reply
     */
    public abstract RadioMessage response(RadioMessage message);
}
