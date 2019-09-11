package mil.navy.nrl.sdt3d;

import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import mil.navy.nrl.sdt3d.sdt3d.AppFrame.CmdParser;

/**
 * This class provides a UDP socket that listens for SDT commands
 * and passes them to the "sdt3dApp"
 *
 * It is intended as a replacement for PipeThread.java
 * @author Dustin Ingram
 */

public class SocketThread extends Thread {
   protected boolean stopFlag = false;
   protected sdt3d.AppFrame sdt3dApp;
   int port = 0;
   long lastWaitTime = 0;

   public SocketThread(sdt3d.AppFrame theSdtApp, int thePort) 
  	{ 
	   sdt3dApp = theSdtApp; 
	   port = thePort;
	}
   public boolean stopped() 
   {
	   return stopFlag; 

   }
   public void stopThread() 
   {
	   stopFlag = true;
   }
   /*
    * Used by udpSocketThread & tcpSocketThread to extract the wait commands
    */
   protected void parseStringOrig(StringBuilder sb,final CmdParser parser)
   {
	   long currentTime = 0;
	   long elapsedTime = 0;
	   int index = 0;
	   int ch;
	   boolean comment = false;
	   boolean waiting = false;
	   boolean quoted = false;
	   StringReader reader = new StringReader(sb.toString());
       try {
			// reposition stream where we left off
			reader.skip(index);

			while ((ch = reader.read()) != -1)
			{
				index++;
				if (ch == 92)
				{
					// We're not allowing any escaped characters at this point
					ch = reader.read();
					index = index++;
					continue;
				}	
						
				// It's a comment, keep reading till end of line
				if (ch == 35)
				{
					comment = true;
					continue;
				}
				// Wait until comment is read
				if (comment)
				{
					if (ch == '\r' || ch == '\n')
					{
						comment = false;
						sb = sb.delete(0,index);
						index = 0;
						continue;
					}
					continue;
				}

				// It's a quoted string, keep reading till end of quote
				if (ch == 34 && !quoted)
				{
					quoted = true;
					continue;
				}
				if (quoted)
				{
					if (ch == 34)
					{
						quoted = false;						
					}					
					continue;
				}
				if (Character.isWhitespace((char)ch) || (ch == '\r' || ch == '\n'))
				{
					
					if (index == 1)
					{
						// We just have whitespace, continue
						sb = sb.delete(0,index);
						index = 0;
						continue;
					}
					final String message = sb.substring(0,index);
					if (message.contains("wait") && message.length() == 5)
					{
						waiting = true;
						sb = sb.delete(0,index);
						index = 0;
						continue;
					}
					if (waiting)
					{
						// Now we have the duration, let's wait
						long sleepTime = Float.valueOf(message.trim()).intValue();
						
						if (this.lastWaitTime > 0)
						{
							currentTime = System.currentTimeMillis();
							elapsedTime = currentTime - this.lastWaitTime;
							sleepTime = sleepTime - elapsedTime;
							if (sleepTime < 0)
								sleepTime = 0;
						}
						//System.out.println("ElapsedTime>" + elapsedTime + " OriginalSleepTime>" + Float.valueOf(message.trim()).intValue() + " newSleepTime>" + sleepTime);
						try {
							sleep(sleepTime);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						this.lastWaitTime = System.currentTimeMillis();
						waiting = false;
						sb = sb.delete(0,index);	
						index = 0;
						continue;
					}

					EventQueue.invokeAndWait(new Runnable()
					{
						public void run()
						{
								
							System.out.println("Msg>" + message);
							sdt3dApp.onInput(message, parser);
						}
					});
					sb = sb.delete(0,index);
					index = 0;
				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       	   
   }
   /*
    * Used by udpSocketThread & tcpSocketThread to extract the wait commands
    */
   protected void parseString(StringBuilder sb,final CmdParser parser)
   {
	   long currentTime = 0;
	   long elapsedTime = 0;
	   int index = 0;
	   int ch;
	   boolean comment = false;
	   boolean waiting = false;
	   boolean quoted = false;
	   StringReader reader = new StringReader(sb.toString());
	  // System.out.println("sb> " + sb.toString());
       try {
			// reposition stream where we left off
			reader.skip(index);

			while ((ch = reader.read()) != -1)
			{
				index++;	

				if (ch == 92 || Character.toString((char)ch).equals("\\"))
				{
					// We're not allowing any escaped characters at this point
					//System.out.println("We're skipping an escaped char");
					ch = reader.read();
					index++;
					continue;
				}
				// It's a comment, keep reading till end of line
				if (ch == 35)
				{
					comment = true;
					continue;
				}
				// Wait until comment is read
				if (comment)
				{
					if (ch == '\r' || ch == '\n')
					{
						comment = false;
						sb = sb.delete(0,index);
						index = 0;
						continue;
					}
					continue;
				}

				// It's a quoted string, keep reading till end of quote
				if (ch == 34 && !quoted)
				{
					quoted = true;
					continue;
				}
				if (quoted)
				{
					if (ch == 34)
					{
						quoted = false;						
					}					
					continue;
				}

				if (Character.isWhitespace((char)ch) || (ch == '\r' || ch == '\n'))
				{
					
					if (index == 1)
					{
						// We just have whitespace, continue
						sb = sb.delete(0,index);
						index = 0;
						continue;
					}
					final String message = sb.substring(0,index);
					if (message.contains("wait") && message.length() == 5)
					{
						waiting = true;
						sb = sb.delete(0,index);
						index = 0;
						continue;
					}
					if (waiting)
					{
						// Now we have the duration, let's wait
						long sleepTime = Float.valueOf(message.trim()).intValue();
						
						if (this.lastWaitTime > 0)
						{
							currentTime = System.currentTimeMillis();
							elapsedTime = currentTime - this.lastWaitTime;
							sleepTime = sleepTime - elapsedTime;
							if (sleepTime < 0)
								sleepTime = 0;
						}
						//System.out.println("ElapsedTime>" + elapsedTime + " OriginalSleepTime>" + Float.valueOf(message.trim()).intValue() + " newSleepTime>" + sleepTime);
						try {
							sleep(sleepTime);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						this.lastWaitTime = System.currentTimeMillis();
						waiting = false;
						sb = sb.delete(0,index);	
						index = 0;
						continue;
					}
					//System.out.println("Msg> " + message + " thread>" + this.getId());
					//EventQueue.invokeAndWait(new Runnable()
					EventQueue.invokeLater(new Runnable()
					{
						public void run()
						{
								
						//	System.out.println("Msg>" + message);
							sdt3dApp.onInput(message, parser);
							sdt3dApp.getWwd().redraw();
						} 
					}); 
					sb = sb.delete(0,index);
					index = 0;
				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       	   
   }
}

