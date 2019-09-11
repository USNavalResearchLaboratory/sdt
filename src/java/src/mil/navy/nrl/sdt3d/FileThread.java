package mil.navy.nrl.sdt3d;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import mil.navy.nrl.sdt3d.sdt3d.AppFrame.CmdParser;

public class FileThread extends Thread
{
	private FileReader fIn = null;
	private BufferedReader brIn = null;
	private sdt3d.AppFrame sdt3dApp;
	private boolean stopFlag = false;
	BufferedReader inputFile = null;
	CmdParser parser = null;
	
	LinkedList<FileReaders> fileStack = new LinkedList<FileReaders>();
	public boolean isRunning() {return !fileStack.isEmpty();}
	
	class FileReaders
	{
		private FileReader fIn;
		private BufferedReader brIn;
		private boolean pipeInputFile = false;
		
		public FileReaders() {};
		public FileReaders(FileReader theFile, BufferedReader theReader,boolean pipeInput)
		{
			this.fIn = theFile;
			this.brIn = theReader;
			this.pipeInputFile = pipeInput;
		}
		public boolean readingPipeInputFile() {return this.pipeInputFile;}
	
		public FileReader getFileReader()
		{
			return fIn;
		}
		public BufferedReader getBufferedReader()
		{
			return brIn;
		}
	}
	
	public FileThread(sdt3d.AppFrame theSdtApp, String fileName,boolean pipeCmd)
	{
		sdt3dApp = theSdtApp;
		try { 
			fIn = new FileReader(fileName);
			brIn = new BufferedReader(fIn);
			
			FileReaders tmp = new FileReaders(fIn,brIn,pipeCmd);			
			fileStack.add(tmp);
		} catch (IOException e) { 
			System.out.println("IOException error!");
			e.printStackTrace();
		}
	}
	public void stopRead()
	{
		// trigger break out of run while loop
		try {
			if (fileStack != null && !fileStack.isEmpty())
				fileStack.peek().getBufferedReader().close();
		} catch (IOException e) {
			System.out.println("FileThread::stopRead() IOException error");
			e.printStackTrace();
		}
		
	}
	public void stopThread()
	{
		stopFlag = true;
	}
	public void startThread()
	{
		stopFlag = false;
	}
		
	public void pushFile(String fileName)
	{
		try {
			fIn = new FileReader(fileName);
			brIn = new BufferedReader(fIn);
			
			// If we're processing the file immediately, we recv'd
			// the command from the file menu
			FileReaders tmp = new FileReaders(fIn,brIn,false);
			fileStack.push(tmp);
			inputFile = fileStack.peek().getBufferedReader();
		} catch (IOException e) {
			System.out.println("IOException error!");
			e.printStackTrace();
		}		
	}
	public void addLast(String fileName)
	{
		try {
			fIn = new FileReader(fileName);
			brIn = new BufferedReader(fIn);
			
			// If we're appending the file, we must have recv'd
			// the input file command from a pipe.  Set indicator
			// to ignore further input commands until the file has
			// been read
			FileReaders tmp = new FileReaders(fIn,brIn,true);
			fileStack.addLast(tmp);
		} catch (IOException e) {
			System.out.println("IOException error!");
			e.printStackTrace();
		}
		
	}
	
	public void popFile()
	{
		try {
			if (!fileStack.isEmpty()) 
			{
				fileStack.peek().getFileReader().close();	
				fileStack.peek().getBufferedReader().close();
				fileStack.pop();
			}
		}

		catch (IOException e) {
			// TODO Auto-generated catch block
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
	public void run()
	{
		parser = sdt3dApp.new CmdParser();
		
		//start thread that opens FileThread and listens for open file
		String record = null;
		try {
			while (!fileStack.isEmpty() && (inputFile = fileStack.peek().getBufferedReader()) != null)
			{	
				while ((record = inputFile.readLine()) != null && !stopFlag) {
					if(record.startsWith("wait"))
					{					
						try {
							sleep(Float.valueOf(record.substring(4).trim()).intValue());
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else if (record.startsWith("input"))
					{
						final String cmd = record;
						try { EventQueue.invokeAndWait(new Runnable() {
							public void run()
							{
								if (!stopFlag)
								{
									sdt3dApp.OnInput(cmd,parser);	
								}
							}
						});
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					else
					{
						final String cmd = record;

						try { EventQueue.invokeLater(new Runnable() {

							public void run()
							{		
								// So we don't clobber file/pipe state when interleaving 
								// the two command sets.  Certainly there's a better way, but for now...
								if (!stopFlag)
								{
									sdt3dApp.OnInput(cmd,parser);								
								}
							}
						});
						} catch (Exception ex) {
							ex.printStackTrace();
						}		
					}	
				}	
				fileStack.peek().getFileReader().close();			
				fileStack.pop();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}  // end FileThread::run()
}  // end class FileThread

