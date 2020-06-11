/*********************************************************************
 *
 * AUTHORIZATION TO USE AND DISTRIBUTE
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that: 
 *
 * (1) source code distributions retain this paragraph in its entirety, 
 *  
 * (2) distributions including binary code include this paragraph in
 *     its entirety in the documentation or other materials provided 
 *     with the distribution.
 * 
 *      "This product includes software written and developed 
 *       by Code 5520 of the Naval Research Laboratory (NRL)." 
 *         
 *  The name of NRL, the name(s) of NRL  employee(s), or any entity
 *  of the United States Government may not be used to endorse or
 *  promote  products derived from this software, nor does the 
 *  inclusion of the NRL written and developed software  directly or
 *  indirectly suggest NRL or United States  Government endorsement
 *  of this product.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * For more information send email to sdt_info@nrl.navy.mil
 *
 *
 * WWJ code:
 * 
 * Copyright (C) 2001 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 ********************************************************************/

package mil.navy.nrl.sdt3d;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import mil.navy.nrl.sdt3d.sdt3d.AppFrame;

public class UdpSocketThread extends SocketThread
{

	MulticastSocket socket = null;

	String multicastAddr = "";


	public UdpSocketThread(AppFrame sdt3dApp, int thePort,
			String theMulticastAddr)
	{
		super(sdt3dApp, thePort);
		// TODO Auto-generated constructor stub
		multicastAddr = theMulticastAddr;
	} // UdpSocketThread::UdpSocketThread()


	@Override
	public void run()
	{
		byte buffer[] = new byte[65535];
		final SdtCmdParser parser = new SdtCmdParser(sdt3dApp);
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