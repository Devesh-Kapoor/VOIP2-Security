/*
 * TextSender.java
*/

/**
 *
 * @author  Devesh
 */
import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.DatagramSocket2;

import javax.sound.sampled.LineUnavailableException;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Vector;

public class TextSender implements Runnable{
    
    static DatagramSocket2 sending_socket;

    static void rotateMatrix(
            int n, DatagramPacket matrix[][]){
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                DatagramPacket temp= matrix[i][j];
                matrix[i][j]= matrix[j][i];
                matrix[j][i]= temp;
            }
        }
        for(int i=0;i<n;i++){
            int top=0;
            int bottom = n-1;
            while(top<bottom){
                DatagramPacket temp = matrix[top][i];
                matrix[top][i]=matrix[bottom][i];
                matrix[bottom][i] = temp;
                top++;
                bottom--;
            }
        }
    }
    

    public void run(){
     
        //***************************************************
        //Port to send to
        int PORT = 55555;
        //IP ADDRESS to send to
        InetAddress clientIP = null;
	try {
		clientIP = InetAddress.getByName("localhost");

	} catch (UnknownHostException e) {
                System.out.println("ERROR: TextSender: Could not find client IP");
		e.printStackTrace();
                System.exit(0);
	}
        //***************************************************
        
        //***************************************************
        //Open a socket to send from
        //We don't need to know its port number as we never send anything to it.
        //We need the try and catch block to make sure no errors occur.
        
        //DatagramSocket sending_socket;
        try{
		sending_socket = new DatagramSocket2();
	} catch (SocketException e){
                System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
		e.printStackTrace();
                System.exit(0);
	}
        //***************************************************

      
        //***************************************************
        //Get a handle to the Standard Input (console) so we can read user input


        
        boolean running = true;
        
        while (running){
            try{
                //Read in a string from the standard input
                //String str = in.readLine();
                //Convert it to an array of bytes
                //byte[] buffer = str.getBytes();
                //Make a DatagramPacket from it, with client address and port number


                double recordTime = 100;


                AudioRecorder recorder = new AudioRecorder();

                //Capture audio data and add to voiceVector


                DatagramPacket[] oneDArray = new DatagramPacket[16];
                DatagramPacket[][] matrix = new DatagramPacket[4][4];
                DatagramPacket[] rotatedMatrix = new DatagramPacket[16];

                int looper = 0;

                for (int i = 0; i < Math.ceil(recordTime / 0.032); i++) {
                    byte[] block = recorder.getBlock();


                    ByteBuffer VoIPpacket = ByteBuffer.allocate(block.length + 2);
                    VoIPpacket.putShort((short)looper);
                    VoIPpacket.put(block);

                    byte[] numbered = VoIPpacket.array();


                    DatagramPacket packet = new DatagramPacket(numbered, numbered.length, clientIP, PORT);


                    oneDArray[looper] = packet;
                    looper++;

                    if ((i+1)%16 == 0){
                        int count = 0;

                        for(int j=0;j<=3;j++)

                        {

                            for(int k=0;k<=3;k++)

                            {

                                if(count==oneDArray.length) break;

                                matrix[j][k]=oneDArray[count];


                                count++;

                            }
                            looper = 0;
                        }


                        rotateMatrix(4,matrix);
                        int counter = 0;
                        for (int j = 0; j < 4 ; j++) {
                            for (int k = 0; k < 4; k++) {
                                rotatedMatrix[counter] = matrix[j][k];
                                counter++;
                            }
                        }







                        for (int l = 0; l < 15; l++) {
                            sending_socket.send(rotatedMatrix[l]);
                        }
                    }


                }


                recorder.close();

                //Send it


            } catch (IOException | LineUnavailableException e){
                System.out.println("ERROR: TextSender: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        //Close the socket
        sending_socket.close();
        //***************************************************
    }
} 
