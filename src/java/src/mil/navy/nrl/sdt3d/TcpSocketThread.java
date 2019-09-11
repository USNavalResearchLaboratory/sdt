package mil.navy.nrl.sdt3d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

//import com.google.protobuf.TextFormat;

import mil.navy.nrl.sdt3d.sdt3d.AppFrame;
import mil.navy.nrl.sdt3d.sdt3d.AppFrame.CmdParser;
//import mil.navy.nrl.sdtCommands.sdtCommandsProtos.Node;

public class TcpSocketThread extends SocketThread
{

	ServerSocket tcpSocket = null;


	public TcpSocketThread(AppFrame theSdtApp, int thePort)
	{
		super(theSdtApp, thePort);
		// TODO Auto-generated constructor stub
	}


	// override
	@Override
	public void stopThread()
	{
		stopFlag = true;
		try
		{
			if (tcpSocket != null)
				tcpSocket.close();

		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	public void run()
	{
		Socket clientSocket = null;
		try
		{
			tcpSocket = new ServerSocket(port);
		}
		catch (IOException e)
		{
			System.err.println("Could not listen on port:" + port);
			return;
		}

		while (true)
		{
			try
			{
				clientSocket = tcpSocket.accept();
			}
			catch (IOException e)
			{
				System.out.println(e);
				return;
			}

			TcpClient theClient = new TcpClient(clientSocket);
			theClient.start();
		} // end while
	}

	class TcpClient extends Thread implements Runnable
	{
		Socket clientSocket = null;


		TcpClient(Socket theClient)
		{
			clientSocket = theClient;
		}


		@Override
		public void run()
		{
			final CmdParser parser = sdt3dApp.new CmdParser();
			StringBuilder sb = new StringBuilder();

			try
			{
				final char[] inputChars = new char[8192];
				int charsRead = 0;
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

				// Node node = Node.parseFrom(clientSocket.getInputStream());
				// System.out.println("Protobuf>\n " + node.toString());

				while ((charsRead = in.read(inputChars, 0, 256)) != -1)
				{
					if (stopFlag)
					{
						in.close();
						clientSocket.close();
						return;
					}

					sb.append(inputChars, 0, charsRead);
					// parse string
					parseString(sb, parser);

				} // Client Socket closed

				in.close();
				clientSocket.close();

			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
			catch (NumberFormatException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		} // end Run

	} // end class TcpClient

}
