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

import java.awt.EventQueue;
import java.io.IOException;
import java.io.StringReader;

/**
 * This class provides a UDP socket that listens for SDT commands
 * and passes them to the "sdt3dApp"
 *
 * It is intended as a replacement for PipeThread.java
 * 
 * @author Dustin Ingram
 */

public class SocketThread extends Thread
{
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
	 * Called by input threads to parse input and extract the wait commands
	 */
	protected void parseString(StringBuilder sb, final SdtCmdParser parser)
	{
		long currentTime = 0;
		long elapsedTime = 0;
		int index = 0;
		int ch;
		boolean comment = false;
		boolean waiting = false;
		boolean quoted = false;
		StringReader reader = new StringReader(sb.toString());

		try
		{
			// reposition stream where we left off
			reader.skip(index);

			while ((ch = reader.read()) != -1)
			{
				index++;

				if (ch == 92 || Character.toString((char) ch).equals("\\"))
				{
					// We're not allowing any escaped characters at this point
					// System.out.println("We're skipping an escaped char");
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
						sb = sb.delete(0, index);
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

				if (Character.isWhitespace((char) ch) || (ch == '\r' || ch == '\n'))
				{

					if (index == 1)
					{
						// We just have whitespace, continue
						sb = sb.delete(0, index);
						index = 0;
						continue;
					}
					final String message = sb.substring(0, index);
					if (message.contains("wait") && message.length() == 5)
					{
						waiting = true;
						sb = sb.delete(0, index);
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
						// System.out.println("ElapsedTime>" + elapsedTime + " OriginalSleepTime>" +
						// Float.valueOf(message.trim()).intValue() + " newSleepTime>" + sleepTime);
						try
						{
							sleep(sleepTime);
						}
						catch (InterruptedException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						this.lastWaitTime = System.currentTimeMillis();
						waiting = false;
						sb = sb.delete(0, index);
						index = 0;
						continue;
					}

					EventQueue.invokeLater(new Runnable()
						{
							@Override
							public void run()
							{
								sdt3dApp.onInput(message, parser);
							}
						});
					sb = sb.delete(0, index);
					index = 0;
				}
			}
		}
		catch (NumberFormatException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
