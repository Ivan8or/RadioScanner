A simplified way to have message->response java networking!


maven repo / dependencies:
=
```
<repositories>
        <repository>
            <id>umbcraft-repo</id>
            <url>https://nexus.umbcraft.online/repository/umbcraft-pub/</url>
        </repository>
</repositories>
```


```
<dependencies>
        <dependency>
            <groupId>online.umbcraft.libraries</groupId>
            <artifactId>RadioScanner</artifactId>
            <version>3.0.6</version>
        </dependency>
</dependencies>
```



BASIC DEMONSTRATION:
=

```Java

public class myClass {
public static void main(String[] args) {
    
    
    // creating new keypairs
    // this is for test purposes, you would normally want to create keypairs once and store them
    // instead of making new ones each time

    HelpfulRSAKeyPair server_keypair = new HelpfulRSAKeyPair();
    HelpfulRSAKeyPair client_keypair = new HelpfulRSAKeyPair();


    // ------- creating the server side -------

    WalkieTalkie talkie = new WalkieTalkie();


    // creating a response for any messages with the reason "do_i_have_enough_pets?"
    ReasonResponder my_reason = new ReasonResponder("do_i_have_enough_pets?", server_keypair) {

        // body of the response - this is where your own code should go!!
        @Override
        public ResponseMessage response(ReasonMessage message) {

            ResponseMessage toReturn = new ResponseMessage()
                    .put("enough_pets?", "no!")
                    .setSuccess(true);
            return toReturn;

        }
    };

    // telling server about the client public key in advance so it can recognize it
    my_reason.addKnown(client_keypair.pub64());

    // also setting port on which this responder listens
    talkie.addResponse(25540, my_reason);






    // ------- creating the client side -------

    // creating a message
    ReasonMessage toSend = new ReasonMessage();


    // setting reason
    toSend.setReason("do_i_have_enough_pets?");


    // you can fill the message with other data here if you need
    // toSend.put("cats_owned", "2");
    // toSend.put("dogs_owned", "0");


    // giving the message it's client keypair so it can sign and decrypt
    toSend.setRSAKeys(client_keypair);

    // giving the message the remote public key so it can encrypt
    toSend.setRemoteKey(server_keypair.pub());


    // sending the message to the server and getting a future back
    Future<ResponseMessage> response = toSend.send("127.0.0.1", 25540);


    // getting the value from the future!
    // (will block thread, so do only if you're okay with
    // waiting for stuff to go over the network)

    ResponseMessage response_msg = null;
    try {
        response_msg = response.get();
    } catch (Exception e) {
        e.printStackTrace();
    }


    // displaying results to user
    System.out.println("what is the answer?: " + response_msg.get("enough_pets?"));


    // stop server since it will otherwise listen forever
    talkie.stopListening();
    }
}
```


ERROR CHECKING:
=


if using the #send(...) function encounters some issue with sending the message, it will  

return a Future that contains a radio message with the contents "TRANSMIT_ERROR" = (your error)

All of the errors are listed in the RadioError enum class.


use the enableDebug() functions on the RadioMessage / WalkieTalkie class for more verbose logs
















