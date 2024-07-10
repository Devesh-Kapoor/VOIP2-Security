/*
 * TextDuplex.java
 */

/**
 *
 * @author  abj
 */
public class TextDuplex {
    
    public static void main (String[] args){
        
        TextReceiver receiver = new TextReceiver();
        TextSender sender = new TextSender();
        Thread Thread1 = new Thread(receiver);
        Thread Thread2 = new Thread(sender);
        Thread1.start();
        Thread2.start();
        
    }
    
}
