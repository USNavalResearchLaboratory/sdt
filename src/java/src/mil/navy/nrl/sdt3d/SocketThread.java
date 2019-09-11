package mil.navy.nrl.sdt3d;

import java.awt.EventQueue;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.io.IOException;
import mil.navy.nrl.sdt3d.sdt3d.AppFrame.CmdParser;

/**
 * This class provides a UDP socket that listens for SDT commands
 * and passes them to the "sdt3dApp"
 *
 * It is intended as a replacement for PipeThread.java
 * @author Dustin Ingram
 */

public class SocketThread extends Thread {
   private boolean stopFlag = false;
   private sdt3d.AppFrame sdt3dApp;
   CmdParser parser = null;
   int port = 0;
   String multicastAddr = "";

   public SocketThread(sdt3d.AppFrame theSdtApp, int thePort,String theMulticastAddr) 
   	{ 
	   sdt3dApp = theSdtApp; 
	   port = thePort;
	   multicastAddr = theMulticastAddr;
	}
   public void stopThread() {stopFlag = true;}
   public void startThread() {stopFlag = false;}
   public void run()
   {
	   try {
		   parser = sdt3dApp.new CmdParser();
		   final MulticastSocket socket = new MulticastSocket(port);
		   byte buffer[] = new byte[67707];

		   if (!multicastAddr.isEmpty()) {socket.joinGroup(InetAddress.getByName(multicastAddr));} 

	       while(true)
	       {
	            if (stopFlag)
	            {
	     	       if (!multicastAddr.isEmpty()) {socket.leaveGroup(InetAddress.getByName(multicastAddr));}
	     	       socket.close();
	               return;
	            }
	            String str = null;
	            DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
	            socket.receive(dp);
	            str = new String(dp.getData(), 0, dp.getLength());
	            final String cmdString = str;
	
	            EventQueue.invokeLater(new Runnable()
	            {
	               public void run()
	               {
	                   if (stopFlag) return;
	            	  sdt3dApp.OnInput(cmdString,parser);
	                  sdt3dApp.getWwd().redraw();
	               }
	            });
	       }


	   } catch (IOException e)
	   {
		   e.printStackTrace();
	   }
   
   }
}
