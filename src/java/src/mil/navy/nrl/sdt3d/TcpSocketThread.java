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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

//import com.google.protobuf.TextFormat;

import mil.navy.nrl.sdt3d.sdt3d.AppFrame;
//import mil.navy.nrl.sdtCommands.sdtCommandsProtos.Node;

public class TcpSocketThread extends SocketThread
{

	ServerSocket tcpSocket = null;


	public TcpSocketThread(AppFrame sdt3dApp, int thePort)
	{
		super(sdt3dApp, thePort);
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
			final SdtCmdParser parser = new SdtCmdParser(sdt3dApp);
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
