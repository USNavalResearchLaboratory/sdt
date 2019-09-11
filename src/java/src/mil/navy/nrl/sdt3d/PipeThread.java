package mil.navy.nrl.sdt3d;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;

import mil.navy.nrl.protolib.*;
import mil.navy.nrl.sdt3d.sdt3d.AppFrame.CmdParser;

/**
 * This class simply provides a thread that listens on a ProtoPipe(MESSAGE)
 * named "sdt3d" for SDT commands and passes them to the "sdt3dApp" 
 * TODO Add an option to set the "pipeName" to something else
 * @author Brian Adamson
 */

public class PipeThread extends Thread {
	private boolean stopFlag = false;
	private ProtoPipe cmdPipe;
	private sdt3d.AppFrame sdt3dApp;
	CmdParser parser = null;

	public PipeThread(sdt3d.AppFrame theSdtApp)
	{
		sdt3dApp = theSdtApp;
	}
	public void stopThread()
	{
		stopFlag = true;
	}
	public void startThread()
	{
		stopFlag = false;
	}	
	public void run()
	{
		parser = sdt3dApp.new CmdParser();
		parser.SetPipeCmd(true);
		try {
			cmdPipe = new ProtoPipe();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			cmdPipe.listen(sdt3dApp.GetPipeName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte cmdBuffer[] = new byte[8192];
		boolean reading = true;
		while(reading)
		{
			if (stopFlag)
				return;

			int numBytes = -1;
			try {
				numBytes = cmdPipe.read(cmdBuffer, 0, 8192);
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IndexOutOfBoundsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (numBytes < 0) break;
			
			
			// This trims off any trailing '\0' character
			for (int i = (numBytes - 1); i >= 0; i--)
			{
			    if (0 == cmdBuffer[i])
			        numBytes--;
			    else
			        break;
			}
			
			// TODO Auto-generated method stub
            String str = null;
            try 
            {
                str = new String(cmdBuffer, 0, numBytes, "UTF8");
            } 
            catch (UnsupportedEncodingException e) 
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }           
            final String cmdString = str;
            
            // The use of "invokeLater" may be unnecessary if the WWJ code is
            // sufficiently thread-safe for the SDT manipulations
            EventQueue.invokeLater(new Runnable()
            {

                public void run()
                {
                    // If we're reading a file from the command line
                	// suspend further processing until the file
                	// read is complete.
                 	if (sdt3dApp.readingCmdInputFile())
                	{
                		System.out.println("Reading from input file, ignoring cmd: " + cmdString);
                		return;
                	}
                	sdt3dApp.OnInput(cmdString,parser);
                    sdt3dApp.getWwd().redraw();
                }
            
            });
            
		}
	}  // end PipeThread::run()
}  // end class PipeThread
