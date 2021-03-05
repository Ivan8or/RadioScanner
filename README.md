A simplified way to have event-based java networking!


maven repo / dependencies:
=

repo url http://192.168.1.22:27018/repository/umbcraft-pub/

dependency group id: online.umbcraft.libraries

dependency artifact Id: RadioScanner

dependency version: 2.3.5



BASIC DEMONSTRATION:
=

```
public static void main(String[] args) {

        // creating a new keypair... DO NOT DO THIS IN PRODUCTION!
        // this is for test purposes, you would normally want to create a single keypair and store it
        // instead of making a new one each time
        String[] keypair = MessageEncryptor.genRSAKeyPair();

        // creating the server side
        WalkieTalkie talkie = new WalkieTalkie(keypair[0], keypair[1]);

        // adding a response to the main object
        // also setting port on which this responder listens
        talkie.addResponse(25540,
                new ReasonResponder("do_i_have_enough_animals") {
                    @Override
                    public RadioMessage response(RadioMessage message) {
                        RadioMessage toReturn = new RadioMessage();

                        String how_many_cats = message.get("cats_owned");
                        String how_many_dogs = message.get("dogs_owned");

                        int cats_int = 0;
                        int dogs_int = 0;
                        try {
                            cats_int = Integer.parseInt(how_many_cats);
                            dogs_int = Integer.parseInt(how_many_dogs);

                        } catch (Exception e) {
                            toReturn.put("success", "false");
                            toReturn.put("fail-reason", "number convert error");
                            return toReturn;
                        }
                        if (cats_int > 1)
                            toReturn.put("enough_cats", "yes");
                        else
                            toReturn.put("enough_cats", "no");

                        if (dogs_int > 1)
                            toReturn.put("enough_dogs", "yes");
                        else
                            toReturn.put("enough_dogs", "no");
                        return toReturn;
                    }
                }
        );

        // sending a 'do_i_have_enough_animals' message from a client
        RadioMessage toSend = new RadioMessage();

        // setting reason (key val must be 'reason'), it's special!!!
        toSend.put("reason","do_i_have_enough_animals");

        // setting other relevant data
        toSend.put("cats_owned", "2");
        toSend.put("dogs_owned", "0");

        // giving rsa keypair to message so it knows how to encrypt it
        toSend.setRSAKeys(keypair[0], keypair[1]);

        // sending the message to the server and getting a future back
        Future<RadioMessage> response = toSend.sendE("127.0.0.1", 25540);

        // getting the value from the future! (
        // will block thread, so do only if you're okay with
        // lag since you're waiting for stuff to go over the network)
        RadioMessage response_msg = null;
        try {
            response_msg = response.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // displaying results to user
        String enough_cats = response_msg.get("enough_cats");
        String enough_dogs = response_msg.get("enough_dogs");

        System.out.println("response body!: "+response_msg);
        System.out.println("enough cats? "+enough_cats);
        System.out.println("enough dogs? "+enough_dogs);

        // stop server since it will otherwise run forever
        talkie.stopListening();
    }
}

```



WALKTHROUGH (SENDING MESSAGES):
=


// 1.1 create a new RadioMessage object passing in an RSA keypair to the constructor 

```
RadioMessage message = new RadioMessage(String rsa_key_pub, String rsa_key_priv);
```



// 1.2 set a reason for the message to be sent

```
message.put("reason", "do_i_have_enough_animals");
```



// 1.3 fill the message with any other string key-val pairs you want

```
message.put("cats_owned", "2");
message.put("dogs_owned", "0");
```



// 1.4 give the radio message your RSA keys so that it knows how to encrypt the message

// P.S. the keypair i am using in this example is NOT a real key pair (intentionally) and should NOT be used

// they are only here to get the point accross that you have to use the same keypair for 

// both sending messages AND for receiving messages... common sense!

// to generate a *REAL* keypair you can use 

// 'String[] keypair = MessageEncryptor.genRSAKeyPair();' (index 0 is public, index 1 is private)

// instead of the gibberish i'm using in this walkthrough


```
String rsa_public_key = "AAAAADADKAWDADWD;NOTAREALKEY";
String rsa_private_key = "WNHEFUIWBUCLIWUCBWUK$CUBRCTVWTEFDHG;ALSONOTAREALKEY;THEREALONEISMUCHLONGER"; 

message.setRSAKeys(rsa_public_key, rsa_private_key);
```



// 1.5 send the message to the correct IP / port

```
Future<RadioMessage> response_future = message.sendE("192.168.1.75", 25540);
```



// 1.6 wait for the response the server sent you back in the form of a second radio message

```
RadioMessage my_response = response_future.get();
```



// 1.7   get values out of the response and use them for whatever 
// P.S. the keys that you pass into the #get(key) function are set on the server side

```
String was_successful = my_response.get("success");
String did_i_have_enough_cats = my_response.get("enough_cats");
String did_i_have_enough_dogs = my_response.get("enough_dogs");
```




WALKTHROUGH (RECEIVING / RESPONDING TO MESSAGES):
=


// 2.1 create a WalkieTalkie object, with a constructor taking in your RSA public and private keys

```
String rsa_public_key = "AAAAADADKAWDADWD;NOTAREALKEY";
String rsa_private_key = "WNHEFUIWBUCLIWUCBWUK$CUBRCTVWTEFDHG;ALSONOTAREALKEY;THEREALONEISMUCHLONGER"; 

WalkieTalkie talkie = new WalkieTalkie(rsa_public_key, rsa_private_key);
```



// 2.2 create some classes that extend ReasonResponder for every 'reason' a message would be sent:

```
public class MyVeryOwnReasonRR extends ReasonResponder {


 // 2.3 override the 'response' function inside of the new MyVeryOwnReasonRR class
 // P.S. this is what gets called when a RadioMessage is received for the reason passed into the constructor
 
 @Override
 public RadioMessage response(RadioMessage message) {
 
      // 2.4 creating a new RadioMessage to eventually be sent back to the original sender as a reply
   
      RadioMessage toReturn = new RadioMessage();
      
      
     
      
      // 2.5 we are using the same message key values for #get(key) as under comment 1.3
      
      String how_many_cats = message.get("cats_owned"); 
      String how_many_dogs = message.get("dogs_owned");
      
      int cats_int = 0;
      int dogs_int = 0;
      try {
         cats_int = Integer.parseInt(how_many_cats);
         dogs_int = Integer.parseInt(how_many_dogs);
         
      } catch(Exception e) {
         toReturn.put("success", "false");
         toReturn.put("fail-reason","number convert error");
         return toReturn;
      }
      
      
      
      // 2.6 filling up the toReturn with the data we want the original sender to receive back
      if(cats_int > 1) 
         toReturn.put("enough_cats","yes");
      else 
         toReturn.put("enough_cats","no");
         
      if(dogs_int > 1) 
         toReturn.put("enough_dogs","yes");
      else 
         toReturn.put("enough_dogs","no");
      
      return toReturn;
 }
}
```


// 2.7 after making your class that extends ReasonResponder, add an instance of it into your WalkieTalkie object:

// P.S. you also set the port on which the ReasonResponder is listening in this step

// P.S. see comment 1.2 uses "do_i_have_enough_animals" as the reason a request is being sent

```
int WALKIE_PORT = 25540;
walkie.addResponse(WALKIE_PORT,new MyVeryOwnReasonRR("do_i_have_enough_animals", this));
```

// that's it!














