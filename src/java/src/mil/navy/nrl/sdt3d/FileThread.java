package mil.navy.nrl.sdt3d;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import mil.navy.nrl.sdt3d.sdt3d.AppFrame.CmdParser;

// We're extending socketThread so we can share the parseString method
public class FileThread extends SocketThread
{
	private FileReader fIn = null;

	private BufferedReader brIn = null;

	private boolean stopFlag = false;

	BufferedReader inputFile = null;

	CmdParser parser = null;

	LinkedList<FileReaders> fileStack = new LinkedList<FileReaders>();
	

	public boolean isRunning()
	{
		return !fileStack.isEmpty();
	}

	
	class FileReaders
	{
		private FileReader fIn;

		private BufferedReader brIn;

		private boolean pipeInputFile = false;


		public FileReaders()
		{
		};


		public FileReaders(FileReader theFile, BufferedReader theReader, boolean pipeInput)
		{
			this.fIn = theFile;
			this.brIn = theReader;
			this.pipeInputFile = pipeInput;
		}


		public boolean readingPipeInputFile()
		{
			return this.pipeInputFile;
		}


		public FileReader getFileReader()
		{
			return fIn;
		}


		public BufferedReader getBufferedReader()
		{
			return brIn;
		}
	}


	public FileThread(sdt3d.AppFrame theApp, String fileName, boolean pipeCmd)
	{
		super(theApp, 0);

		try
		{
			fIn = new FileReader(fileName);
			brIn = new BufferedReader(fIn);

			FileReaders tmp = new FileReaders(fIn, brIn, pipeCmd);
			fileStack.add(tmp);
		}
		catch (IOException e)
		{
			System.out.println("IOException error!");
			e.printStackTrace();
		}
	}


	public void clear()
	{
		stopRead();
		fileStack.clear();
	}


	public void stopRead()
	{
		// trigger break out of run while loop
		try
		{
			if (fileStack != null && !fileStack.isEmpty())
			{
				fileStack.peek().getBufferedReader().close();
			}
		}
		catch (IOException e)
		{
			System.out.println("FileThread::stopRead() IOException error");
			e.printStackTrace();
		}
		fileStack.clear();

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


	public boolean isStopped()
	{
		return stopFlag;
	}


	public void pushFile(String fileName)
	{
		try
		{
			fIn = new FileReader(fileName);
			brIn = new BufferedReader(fIn);

			// If we're processing the file immediately, we recv'd
			// the command from the file menu
			FileReaders tmp = new FileReaders(fIn, brIn, false);
			fileStack.push(tmp);
			inputFile = fileStack.peek().getBufferedReader();
		}
		catch (IOException e)
		{
			System.out.println("IOException error!");
			e.printStackTrace();
		}
	}


	public void addLast(String fileName, boolean forceAppend)
	{
		try
		{
			fIn = new FileReader(fileName);
			brIn = new BufferedReader(fIn);

			// the forceAppend flag indicates the file is being appended
			// via the menu option - in which case we don't want to disable
			// input command processing. Otherwise, if we've recv'd an
			// input command over the command pipe, we DO want to disable
			// any further input commands until the file is fully processed.
			FileReaders tmp;
			if (forceAppend)
			{
				tmp = new FileReaders(fIn, brIn, false);
			}
			else
			{
				tmp = new FileReaders(fIn, brIn, true);
			}
			fileStack.addLast(tmp);
		}
		catch (IOException e)
		{
			System.out.println("IOException error!");
			e.printStackTrace();
		}

	}


	public void popFile()
	{
		try
		{
			if (!fileStack.isEmpty())
			{
				fileStack.peek().getFileReader().close();
				fileStack.peek().getBufferedReader().close();
				fileStack.pop();
			}
		}

		catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	// We want to ignore command line input if we are still reading
	// a command line file
	public boolean readingCmdInputFile()
	{
		if (fileStack.peek() != null)
			return fileStack.peek().readingPipeInputFile();
		else
			return false;
	}

	
	@Override
	public void run()
	{
		final CmdParser parser = theApp.new CmdParser();

		StringBuilder sb = new StringBuilder();

		// start thread that opens FileThread and listens for open file
		String record = null;
		try
		{
			while (!fileStack.isEmpty() && (inputFile = fileStack.peek().getBufferedReader()) != null)
			{
				while ((record = inputFile.readLine()) != null && !stopFlag)
				{
					// Reattach eol
					record = record + '\n';
					sb.append(record, 0, record.length());
					// parse string
					parseString(sb, parser);
				} // end processing file stack								
				popFile();				
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	} // end FileThread::run()
} // end class FileThread
