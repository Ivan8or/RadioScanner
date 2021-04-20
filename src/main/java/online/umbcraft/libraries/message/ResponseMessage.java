package online.umbcraft.libraries.message;

import online.umbcraft.libraries.encrypt.HelpfulRSAKeyPair;

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

    @Override
    public ResponseMessage put(String key, String val) {
        if(key.equals("reason")) throw new IllegalArgumentException("reserved key");
        super.put(key, val);
        return this;
    }

    @Override
    public ResponseMessage clear() {
        super.clear();
        return this;
    }

    @Override
    public synchronized ResponseMessage setRSAKeys(String public_key, String private_key) {
        super.setRSAKeys(public_key, private_key);
        return this;
    }

    @Override
    public synchronized ResponseMessage setRSAKeys(HelpfulRSAKeyPair keys) {
        super.setRSAKeys(keys);
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
