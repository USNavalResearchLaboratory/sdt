package mil.navy.nrl.sdt3d;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import mil.navy.nrl.sdt3d.sdt3d.AppFrame;
import mil.navy.nrl.sdt3d.sdt3d.AppFrame.CmdParser;

public class UdpSocketThread extends SocketThread
{

	MulticastSocket socket = null;

	String multicastAddr = "";


	public UdpSocketThread(AppFrame theApp, int thePort,
			String theMulticastAddr)
	{
		super(theApp, thePort);
		// TODO Auto-generated constructor stub
		multicastAddr = theMulticastAddr;
	} // UdpSocketThread::UdpSocketThread()


	@Override
	public void run()
	{
		byte buffer[] = new byte[65535];
		final CmdParser parser = theApp.new CmdParser();
		String inputLine = null;
		DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
		StringBuilder sb = new StringBuilder();

		try
		{
			socket = new MulticastSocket(port);
			if (!multicastAddr.isEmpty())
			{
				socket.joinGroup(InetAddress.getByName(multicastAddr));
			}

		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return; // We can't change Thread::Run() return value;
		}

		while (true)
		{
			if (stopFlag)
			{
				if (!multicastAddr.isEmpty())
					try
					{
						socket.leaveGroup(InetAddress.getByName(multicastAddr));
					}
					catch (UnknownHostException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				if (socket != null)
					socket.close();
				return;
			}
			try
			{
				socket.receive(dp);
			}
			catch (IOException e2)
			{
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			inputLine = new String(dp.getData(), 0, dp.getLength());
			sb.append(inputLine, 0, dp.getLength());
			// parse string
			parseString(sb, parser);

		}
	} // end run()
}