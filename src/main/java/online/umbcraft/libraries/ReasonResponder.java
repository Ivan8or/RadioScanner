package online.umbcraft.libraries;/*
ReasonResponder CLass

An abstract class that can be extended to allow certain RadioMessage(s) to be responded to
the response() method takes in the received message, and returns the custom response
only one ReasonResponder can function (on the same port) per unique message reason
 */

public abstract class ReasonResponder {

    // the reason for which this responder listens
    protected String reason;

    public ReasonResponder(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    // generates a message to be sent in response to an incoming message request
    public abstract RadioMessage response(RadioMessage message);
}
