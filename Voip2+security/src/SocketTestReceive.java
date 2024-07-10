package src.networksassignment;

import javax.sound.sampled.LineUnavailableException;
import CMPC3M06.AudioPlayer;
import java.net.*;
import java.nio.ByteBuffer;
import java.io.*;
import uk.ac.uea.cmp.voip.*;

public class SocketTestReceive implements Runnable
{
    static DatagramSocket sending_socket;
    static DatagramSocket receiving_socket;

    public void run()
    {
        int PORT = 55555;
        InetAddress clientIP = null;
	    try {
		    //clientIP = InetAddress.getByName("139.222.99.37");
            clientIP = InetAddress.getByName("localhost");
	    } catch (UnknownHostException e) {
            System.out.println("ERROR: TextSender: Could not find client IP");
		    e.printStackTrace();
            System.exit(0);
	    }
        try{
		    sending_socket = new DatagramSocket();//Making a sending socket
	    } catch (SocketException e){
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
		    e.printStackTrace();
            System.exit(0);
	    }
        try{
            receiving_socket = new DatagramSocket(PORT);//Making a receiving socket
        } catch (SocketException e){
            System.out.println("ERROR: TextReceiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }
        AudioPlayer player;
        try{
            player = new AudioPlayer();
        } catch (LineUnavailableException e){
            throw new RuntimeException(e);
        }
        
        int privateKey = 3;
        byte[] currentNum = new byte[8];//Stores whatever number is received from sender
        ByteBuffer numStorage = ByteBuffer.allocate(8);
        DatagramPacket receivingNum = new DatagramPacket(currentNum, 0, 8);

        ReceivePacket(receivingNum);
        int publicPrime = ExtractNum(currentNum);//Get the shared prime num

        ReceivePacket(receivingNum);
        numStorage.put(currentNum);
        numStorage.rewind();
        long publicLargeNum = numStorage.getLong();//Get the shared large num
        numStorage.rewind();
        //int publicLargeNum = ExtractNum(currentNum);

        Security manager = new Security(privateKey, publicPrime, publicLargeNum);
        long firstNum = manager.FirstStep();

        ReceivePacket(receivingNum);
        numStorage.put(currentNum);
        numStorage.rewind();
        long theirNum = numStorage.getLong();//Get their calculated num
        numStorage.rewind();

        receiving_socket.close();
        try{
            Thread.sleep(1000);//Wait to ensure the sender has opened their socket
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        numStorage.putLong(firstNum);
        numStorage.rewind();
        DatagramPacket numPacket = new DatagramPacket(numStorage.array(), numStorage.array().length, clientIP, PORT);
        try{
            sending_socket.send(numPacket);//Send our calculated number
        } catch (IOException e){
            e.printStackTrace();
        }

        try{
            Thread.sleep(500);//Wait to ensure the sender has closed their socket
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try{
            receiving_socket = new DatagramSocket(PORT);//Open our socket
        } catch (SocketException e){
            System.out.println("ERROR: Receiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }
        
        manager.GenerateSharedKey(theirNum);
        byte[] previous = new byte[512];

        while(true)
        {
            try{
                byte[] buffer = new byte[512+8];
                DatagramPacket packet = new DatagramPacket(buffer, 0, 512+8);
                ReceivePacket(packet);
                if (manager.Authenticate(buffer)){
                    byte[] decrypted = manager.Decrypt(buffer);
                    player.playBlock(decrypted);
                    previous = decrypted;
                }
                else
                {
                    player.playBlock(previous);
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        /*boolean running = true;
        int totalreceived = 0;
        int lastreceived = 0;
        ArrayList<Integer> bursts = new ArrayList<>();
        while (running){

            try{
                //Receive a DatagramPacket (note that the string cant be more than 80 chars)
                byte[] res = new byte[4];
                DatagramPacket packet = new DatagramPacket(res, 0, 4);

                receiving_socket.setSoTimeout(5000);
                receiving_socket.receive(packet);
                totalreceived++;

                int value = 0;
                for (byte b : res)
                {
                    value = (value << 8) + (b & 0xFF);
                }
                System.out.println(value);
                //lastreceived = value;
                if (value - lastreceived != 1)
                {
                    int burstamount = value - lastreceived;
                    bursts.add(burstamount);
                    bursts.add(lastreceived);
                }
                if (value > lastreceived)
                {
                    lastreceived = value;
                }
            } catch (SocketTimeoutException e)
            {
                System.out.println("Packets that were received were " + totalreceived + "/1000");
                System.out.println(1000 - totalreceived + " packets that were lost");
                System.out.println(bursts.size() + " total drops in packet streaming");
                for (int i = 0; i < bursts.size(); i++)
                {
                    System.out.println("There were "+ bursts.get(i) +" packets lost at "+ bursts.get(i+1));
                    i++;
                }
                System.exit(0);
            } catch (IOException e) {
                System.out.println("ERROR: TextReceiver: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        //Close the socket
        receiving_socket.close();
        //***************************************************
        */
    }

    private int ExtractNum(byte[] array)
    {
        int value = 0;
        for (int i = 0; i < 4; i++)
        {
            value = (value << 8) + (array[i] & 0xFF);
        }
        return value;
    }

    private void ReceivePacket(DatagramPacket packet)
    {
        try{
            receiving_socket.receive(packet);//First receive shared prime num
        } catch (IOException e) {
            System.out.println("ERROR: Receiver: IO error occured!");
            e.printStackTrace();
        }
    }
}
