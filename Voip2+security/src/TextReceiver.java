/*
 * TextReceiver.java
*/

/**
 *
 * @author  Devesh Kapoor
 */
import CMPC3M06.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import java.net.*;
import java.io.*;
import java.util.Arrays;


public class TextReceiver implements Runnable{

    static DatagramSocket receiving_socket;


    // rotates the array back to its origional
    static void rotateMatrix (int n, byte[][] incomingPackets[][]){

        // first I transpose the matrix
        for(int i=0;i<n;i++)
        {
            for(int j=i;j<n;j++)
            {
                byte[][] temp = incomingPackets[i][j];
                incomingPackets[i][j] = incomingPackets[j][i];
                incomingPackets[j][i] = temp;
            }
        }
        for(int i=0;i<n;i++)
        {
            //then I swap the columns
            int low = 0;
            int high = n-1;
            while(low < high)
            {
                byte[][] temp = incomingPackets[low][i];
                incomingPackets[low][i] = incomingPackets[high][i];
                incomingPackets[high][i] = temp;
                low++;
                high--;
            }
        }
    }
    
    public void run () {




     
        //***************************************************
        //Port to open socket on
        int PORT = 55555;
        //***************************************************
        AudioPlayer player = null;
        try {
            player = new AudioPlayer();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        //***************************************************
        //Open a socket to receive from on port PORT
        
        //DatagramSocket receiving_socket;
        try{
		receiving_socket = new DatagramSocket(PORT);
	} catch (SocketException e){
                System.out.println("ERROR: TextReceiver: Could not open UDP socket to receive from.");
		e.printStackTrace();
                System.exit(0);
	}
        
        boolean running = true;
        
        while (running){
         
            try{

                //Receive a DatagramPacket (note that the string cant be more than 80 chars)

                byte[][] ByteMatrix = new byte[16][514];
                byte[] compare = new byte[514];

                boolean check = true;
                while (check) {

                    //set the place for the incoming packet

                byte[] buffer = new byte[514];
                DatagramPacket packet = new DatagramPacket(buffer, 0, 514);
                receiving_socket.receive(packet);


                byte[] temp = new byte[2];
                temp[0] = buffer[0];
                temp[1] = buffer[1];
                //rotateMatrix(4,matrix);
                int value = 0;
                for (byte t:temp) {
                    value = (value << 8) + (t & 0xFF);

                }
                byte[] lastPlayed = new byte[512];
                if (!Arrays.equals(ByteMatrix[value],compare)){
                    for (int j = 0; j < 16; j++) {
                        if (Arrays.equals(ByteMatrix[j],compare)){
                            player.playBlock(lastPlayed);
                        }
                        else{
                            player.playBlock(ByteMatrix[j]);
                            lastPlayed = ByteMatrix[j];
                        }
                        check = false;
                    }
                }else {
                    byte[] decreased = new byte[512];

                    System.arraycopy(buffer, 2, decreased, 0, 512);
                    ByteMatrix[value] = decreased;
                }

            }


            } catch (SocketTimeoutException e){
                System.out.println(".");
            } catch (IOException e){
                System.out.println("ERROR: TextReceiver: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        //Close the socket
        receiving_socket.close();
        //***************************************************
    }
}
