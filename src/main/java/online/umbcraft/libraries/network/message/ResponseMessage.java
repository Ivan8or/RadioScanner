package online.umbcraft.libraries.network.message;

public class ResponseMessage extends RadioMessage {


    /**
     * Creates a blank RadioMessage
     */
    public ResponseMessage() {
        super();
    }

    /**
     * Creates a RadioMessage from a JSON String
     *
     * @param json JSON string from which the message is generated
     */
    public ResponseMessage(String json) {
        super(json);
    }


    /**
     * sets the success status for this response
     *
     * @param success the new success status
     * @return itself
     */
    public ResponseMessage setSuccess(boolean success) {
        if (debug) logger.info("setting success " + success);
        message.put("success", success);
        return this;
    }


    /**
     * gets the success status for this response
     *
     * @return whether this response was successful
     */
    public Boolean getSuccess() {
        if(message.has("success"))
            return message.getBoolean("success");
        return false;
    }


    @Override
    public ResponseMessage put(String key, String val) {
        if(key.equals("success")) throw new IllegalArgumentException("reserved key");
        super.put(key, val);
        return this;
    }


    @Override
    public ResponseMessage clear() {
        super.clear();
        return this;
    }


    @Override
    public ResponseMessage merge(RadioMessage other) {
        super.merge(other);
        return this;
    }


    @Override
    public ResponseMessage enableDebug() {
        super.enableDebug();
        return this;
    }


    @Override
    public ResponseMessage disableDebug() {
        super.disableDebug();
        return this;
    }
}