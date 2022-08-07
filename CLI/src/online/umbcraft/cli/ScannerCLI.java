package online.umbcraft.cli;

import com.sun.source.tree.Tree;
import online.umbcraft.libraries.encrypt.HelpfulRSAKeyPair;
import online.umbcraft.libraries.network.message.ReasonMessage;
import online.umbcraft.libraries.network.message.ResponseMessage;

import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

public class ScannerCLI {

    private String address = "192.168.1.22:25590";
    private String rsa_public = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApsDfnFXJrD3PVnqUUCcKu/qzMXCZCRCTRU6pp5dy8ICmpbn6BB4PZGXbE3bEf2Ytmw8LUyUQFCVcl0xzP28J5d8LqIGAhRx2hEhUc4NyDEgNsdfjIYkdwrMYq71YhjmBwKyIaRUySR1zvnpYwn5siChaSMaB5N2XmzzBqyT01IlO+N6xSSOKg3/sZeCPsube31Olkih6EJVU/XaWfpjyZtpyKQqFvVn17CHLCjLNq+JB42kYLPi0IqH8DgCaUUhovoWZN58D7XIj+LdAjorHtj8csAkoDAfAnXdnYeATncherPn8Ivnk6A3vbazo53NL0BAn7/OPmmJ/ER5u5HzVTwIDAQAB";
    private String rsa_private = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCmwN+cVcmsPc9WepRQJwq7+rMxcJkJEJNFTqmnl3LwgKalufoEHg9kZdsTdsR/Zi2bDwtTJRAUJVyXTHM/bwnl3wuogYCFHHaESFRzg3IMSA2x1+MhiR3CsxirvViGOYHArIhpFTJJHXO+eljCfmyIKFpIxoHk3ZebPMGrJPTUiU743rFJI4qDf+xl4I+y5t7fU6WSKHoQlVT9dpZ+mPJm2nIpCoW9WfXsIcsKMs2r4kHjaRgs+LQiofwOAJpRSGi+hZk3nwPtciP4t0COise2PxywCSgMB8Cdd2dh4BOdyF6s+fwi+eToDe9trOjnc0vQECfv84+aYn8RHm7kfNVPAgMBAAECggEAR9oNwCBbAOO1Rm8VHyBjiLIyqlf5KSXCP3fwtG0WsrjMldZ3+3Q0b69P4agobTjK5+homc/7iKK8rdjcQ/YFjs44CMicZz7ndoJwGUIHT3FwrTP4A4hAkZd4RvAUh+mZljsBoD3o6AZPzECNpGizPUwd8wPODp7I57QbQzJuaHF43j+cx8cAx0mVXQdVzREzuoMBaTNVdebJ+myTDODMlikPTGQT+6DdiAxfMNzBkMC3agVMXVnxhGdngVRVOlvkm60bJJz7e4YoAukD5sg8R75m1JTMYkSy9AJnshae87p/6OXrStwsaiA1iSr8R7lCjnIL/vqPCZLKOhrx17js2QKBgQD3tqLJAcdn+E8/34medeyQCSLD05MENUUTrIc3aS9nuw1ZpclOWDbTNLdwhiT3JdTDHohTcdjarUs5/TCQK7jbLxWeNStYt1yuE4wkErHZJsstgsh6SWjBHAAoVdet1RPD2yBxlyZaiu4y/S77ge3HKJ2HwB0y7fxVn47XTAFZ9QKBgQCsVOnh9OJYItyBDy+Lmfr/sEgSbusyFxIju8s7bM4Jj5oGmGIKAcNLP74ysGchCYksidr0Omj0Y4RSbT7nxfIx2UmSXDnic6Hz0rhMeBx1xarUYR6evPamcKe42KWVYjdXXnT1P7VXRJ1E0SlTAUw9bedZS2snTcQgmw+kew7TswKBgQCyUD4OA8HPDee+SuN9tdV8OZdU79A8jzwSVXdgY89EFFof1j96rfWJQ8FotqeVYR4u9qObrbA0edpU+/UhUtfWr6YYJJ33VsbYaCnRLcPoVQyczjASFxXZLJbQcjq3X1LDAdHw8qUhODEPc0GGupi3bH05Ywmhx76ExD7GrGesoQKBgDT6Kag38vkrRmQ6eeBNptbBp3ZbONx+MSphCdor2a8gwTXz1HfFEPdqEhtdFWcXml7hxji8WBWBHmWEhgkUVh8y8CYZSgYujAP7rIJ9jio6aQi/Dn24E48ZJMZze3vP5LlsJUJbonDAfCRytGR7Cqf7yFowEDHV+0wWvxlOZ0F3AoGAKZba9UYv1caLeJDU2KzjZR0y4byaCMrrj2CcsHt4mYOMRhW1hzlMD8Q5FdMp0Dv+UwfbaB5fe/M+AVgki+oW2i2JV/ETWwPN6vcrkgVQzpuP9R0/ExBfdfpFKT9LQusJU3koBhh3iCv+ZmgqkRx1cEFc0NbBTczXqyRYldTXdHY=";
    private String rsa_remote = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApsDfnFXJrD3PVnqUUCcKu/qzMXCZCRCTRU6pp5dy8ICmpbn6BB4PZGXbE3bEf2Ytmw8LUyUQFCVcl0xzP28J5d8LqIGAhRx2hEhUc4NyDEgNsdfjIYkdwrMYq71YhjmBwKyIaRUySR1zvnpYwn5siChaSMaB5N2XmzzBqyT01IlO+N6xSSOKg3/sZeCPsube31Olkih6EJVU/XaWfpjyZtpyKQqFvVn17CHLCjLNq+JB42kYLPi0IqH8DgCaUUhovoWZN58D7XIj+LdAjorHtj8csAkoDAfAnXdnYeATncherPn8Ivnk6A3vbazo53NL0BAn7/OPmmJ/ER5u5HzVTwIDAQAB";
    private String reason = "stopserver";

    private ReasonMessage message;

    public static void main(String[] args) {


        ScannerCLI cli = new ScannerCLI();
        System.out.println("--Scanner CLI--");

        Scanner sc = new Scanner(System.in);

        while(true) {
            cli.fill(sc);
            cli.send();
        }
    }

    public void fill(Scanner sc) {
        message = new ReasonMessage();

        while(true) {
            System.out.print("key> ");

            String nextkey = sc.nextLine();
            nextkey.trim();
            if(nextkey.contentEquals("")) {
                return;
            }
            System.out.print("val> ");
            String nextval = sc.nextLine().trim();

            message.put(nextkey, nextval);
        }
    }

    public void send() {
        ResponseMessage response = null;
        System.out.println("sending message to "+address);
        try {
            message.setRSAKeys(rsa_public, rsa_private);
            message.setRemoteKey(HelpfulRSAKeyPair.publicFrom64(rsa_remote));
            message.setReason(reason);

            response = message.send(address).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("received response: ");
        System.out.println("Success: "+response.getSuccess());
        System.out.println("Body: "+response);
    }

    public void configure(String[] args, int on) throws IndexOutOfBoundsException {
        if(on >= args.length)
            return;

        switch(args[on]) {
            case "--public":
                rsa_public = args[on+1];
                configure(args, on+2);
                return;
            case "--private":
                rsa_private = args[on+1];
                configure(args, on+2);
                return;
            case "--remote":
                rsa_remote = args[on+1];
                configure(args, on+2);
                return;
            case "--address":
                address = args[on+1];
                configure(args, on+2);
                return;
            case "--reason":
                reason = args[on+1];
                configure(args, on+2);
                return;
            default:
                configure(args, on+1);
                return;
        }
    }
}






















