package online.umbcraft.libraries;

import online.umbcraft.libraries.encrypt.HelpfulRSAKeyPair;
import online.umbcraft.libraries.message.RadioMessage;
import online.umbcraft.libraries.message.ReasonMessage;
import online.umbcraft.libraries.message.ResponseMessage;

/**
 * Responsible for responding to a single type/reason of {@link RadioMessage}<p>
 * Extend this class to add custom logic for whenever a RadioMessage is received
 *
 * @see ReasonResponder
 * @see RadioMessage
 */

public abstract class ReasonResponder {

    protected final String reason;
    protected final HelpfulRSAKeyPair keypair;


    /**
     * Creates a blank ReasonResponder and sets the reason
     *
     * @param reason String which if any {@link RadioMessage}<p> share, this will reply to them
     * @param self   RSA keyset used to reply to the messages
     */
    public ReasonResponder(String reason, HelpfulRSAKeyPair self) {
        this.reason = reason;
        this.keypair = self;
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
     * Returns the {@link HelpfulRSAKeyPair} meant to be used with this reason
     *
     * @return the type of message this responds to
     */
    public final HelpfulRSAKeyPair getKeyPair() {
        return keypair;
    }


    /**
     * generates a {@link RadioMessage} to be sent in response to an incoming {@link RadioMessage}
     *
     * @param message the incoming message
     * @return the generated reply
     */
    public abstract ResponseMessage response(ReasonMessage message);
}
