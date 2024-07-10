

import CMPC3M06.AudioRecorder;
import javax.sound.sampled.LineUnavailableException;
import java.net.*;
import java.nio.ByteBuffer;
import java.io.*;
import java.security.Security;

import uk.ac.uea.cmp.voip.*;

public class SocketTestSend implements Runnable
{
    static DatagramSocket sending_socket;
    static DatagramSocket receiving_socket;
    static DatagramSocket4 sending_socket4;
    
    public void run()
    {
        int PORT = 55555;
        InetAddress clientIP = null;
	    try {
		    //clientIP = InetAddress.getByName("139.222.99.37");
            clientIP = InetAddress.getByName("localhost");
	    } catch (UnknownHostException e) {
            System.out.println("ERROR: Sender: Could not find client IP");
		    e.printStackTrace();
            System.exit(0);
	    }
        try{
		    sending_socket = new DatagramSocket();//Making a sending socket
	    } catch (SocketException e){
            System.out.println("ERROR: Sender: Could not open UDP socket to send from.");
		    e.printStackTrace();
            System.exit(0);
	    }
        /*try{
		    sending_socket4 = new DatagramSocket4();//Making a sending socket
	    } catch (SocketException e){
            System.out.println("ERROR: Sender: Could not open UDP socket to send from.");
		    e.printStackTrace();
            System.exit(0);
	    }*/
        
        System.out.println("Setting up connection...");
        
        int privateKey = 6;
        int publicPrime = 11;
        long publicLargeNum = 5559917313492231482L;

        ByteBuffer numStorage = ByteBuffer.allocate(8);//Stores the number that will be sent/received
        numStorage.putInt(publicPrime);
        numStorage.rewind();
        DatagramPacket numPacket = new DatagramPacket(numStorage.array(), numStorage.array().length, clientIP, PORT);
        SendPacket(numPacket);//Send shared prime first

        numStorage.putLong(publicLargeNum);
        numStorage.rewind();
        numPacket = new DatagramPacket(numStorage.array(), numStorage.array().length, clientIP, PORT);
        SendPacket(numPacket);//Send shared large number second
        
        Security manager = new Security(privateKey, publicPrime, publicLargeNum);
        long firstNum = manager.FirstStep();
        numStorage.putLong(firstNum);
        numStorage.rewind();
        numPacket = new DatagramPacket(numStorage.array(), numStorage.array().length, clientIP, PORT);
        SendPacket(numPacket);//Send calculated value third

        try{
            Thread.sleep(500);//Wait to ensure the receiver has closed their socket
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try{
            receiving_socket = new DatagramSocket(PORT);//Open socket for sender
        } catch (SocketException e){
            System.out.println("ERROR: Sender: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }

        byte[] currentNum = new byte[8];
        DatagramPacket receivingNum = new DatagramPacket(currentNum, 0, 8);
        try{
            receiving_socket.receive(receivingNum);//Receive their calculated num
        } catch (IOException e) {
            System.out.println("ERROR: Sender: IO error occured!");
            e.printStackTrace();
        }

        receiving_socket.close();
        numStorage.put(currentNum);
        numStorage.rewind();
        long theirNum = numStorage.getLong();

        manager.GenerateSharedKey(theirNum);
        sending_socket.close();
        try{
		    sending_socket4 = new DatagramSocket4();//Making a sending socket 4
	    } catch (SocketException e){
            System.out.println("ERROR: Sender: Could not open UDP socket to send from.");
		    e.printStackTrace();
            System.exit(0);
	    }

        while (true)
        {
            try
            {
                AudioRecorder recorder = new AudioRecorder();
                for (int i = 0; i < Math.ceil(10 / 0.032); i++)
                {
                    byte[] block = recorder.getBlock();
                    byte[] encrypted = manager.Encrypt(block);
                    DatagramPacket packet = new DatagramPacket(encrypted, encrypted.length, clientIP, PORT);
                    //DatagramPacket packet = new DatagramPacket(block, block.length, clientIP, PORT);
                    sending_socket4.send(packet);
                    //SendPacket(packet);
                }
                
                recorder.close();

            } catch (LineUnavailableException e){
                throw new RuntimeException(e);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        /*for (int i = 1; i <= 1000; i++)
        {
            ByteBuffer t = ByteBuffer.allocate(4);
            t.putInt(i);
            t.rewind();
            byte[] res = t.array();
            DatagramPacket packet = new DatagramPacket(res, res.length, clientIP, PORT);
            try {
                sending_socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    private void SendPacket(DatagramPacket packet)
    {
        try{
            sending_socket.send(packet);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
