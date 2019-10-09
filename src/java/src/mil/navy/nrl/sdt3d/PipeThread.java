package mil.navy.nrl.sdt3d;

import java.io.IOException;
import java.net.SocketException;

import mil.navy.nrl.protolib.ProtoPipe;
import mil.navy.nrl.sdt3d.sdt3d.AppFrame.CmdParser;

/**
 * This class simply provides a thread that listens on a ProtoPipe(MESSAGE)
 * named "sdt3d" for SDT commands and passes them to the "sdt3dApp"
 * TODO Add an option to set the "pipeName" to something else
 * 
 * @author Brian Adamson
 */
// We haven't implemented wait time accumulation for pipe threads... LJT TODO: ?
public class PipeThread extends SocketThread
{
	private boolean stopFlag = false;

	private ProtoPipe cmdPipe;

	CmdParser parser = null;


	public PipeThread(sdt3d.AppFrame theApp)
	{
		super(theApp, 0);
	}


	@Override
	public void stopThread()
	{
		stopFlag = true;
	}


	public void startThread()
	{
		stopFlag = false;
	}


	@Override
	public void run()
	{
		final CmdParser parser = theApp.new CmdParser();
		StringBuilder sb = new StringBuilder();
		parser.SetPipeCmd(true);
		try
		{
			cmdPipe = new ProtoPipe();
		}
		catch (SocketException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try
		{
			cmdPipe.listen(theApp.getPipeName());
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Now continue forever reading pipe
		byte cmdBuffer[] = new byte[8192];
		int numBytes = 0;
		try
		{

			while (((numBytes = cmdPipe.read(cmdBuffer, 0, 8192)) != -1) && !stopFlag)
			{

				// This trims off any trailing '\0' character
				for (int i = (numBytes - 1); i >= 0; i--)
				{
					if (0 == cmdBuffer[i])
						numBytes--;
					else
						break;
				}

				String cmdString = new String(cmdBuffer, 0, numBytes, "UTF8");

				// If we're reading a file from the command line
				// suspend further processing until the file
				// read is complete.
				if (theApp.readingCmdInputFile())
				{
					// TODO: What should happen here?  This should be debugged
					System.out.println("PipeThread::Run() Warning!  Reading from input file possible interleaved commands!! " + cmdString);
					// continue;
					// return;
				}
				sb.append(cmdString, 0, cmdString.length());
				parseString(sb, parser);

			}
		}
		catch (NullPointerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IndexOutOfBoundsException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	} // end PipeThread::run()

} // end class PipeThread
