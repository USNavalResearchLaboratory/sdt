package mil.navy.nrl.sdt3d;

import java.awt.Cursor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;


/**
 * This class listens for SDT commands from a ScenarioController
 * and passes them to the "sdt3d App"
 * 
 * @author Laurie Thompson
 * @since Aug 23, 2019
 */

public class ScenarioThread extends SocketThread 
{	
	private final Object GUI_MONITOR = new Object();
	
	private volatile boolean pauseThreadFlag = false;
	
	Iterator<Entry<Long, Map<Integer, String>>> itr = null;
	
	protected volatile boolean restartPlaybackFlag = false;
		
	private ScenarioModel scenarioModel;
	
	private ScenarioController scenarioController;
	
	private Long scenarioPlaybackStartTime;
	
	private Float speedFactor;
	
	SdtCmdParser parser = null;
	
	Long lastTime = new Long(0);	
	
	private final HashMap<Integer, String> int2Cmd = new HashMap<>();

	Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	
	Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);

	public ScenarioThread(sdt3d.AppFrame theApp, ScenarioController scenarioController, 
			Long scenarioPlaybackStartTime)
	{
		
		super(theApp, 0);

		initialize_cmd_map();

		this.scenarioController = scenarioController;
		this.scenarioModel = scenarioController.getScenarioModel();
		this.scenarioPlaybackStartTime = scenarioPlaybackStartTime;
		isScenarioThread = true;
	}

	
	private void initialize_cmd_map()
	{
		int x = 0;
		for (String cmd : SdtCmdParser.CMD_LIST)
		{
			if (cmd == null)
			{
				continue;
			}
			x++;
			// Load our cmd map and remove the leading +/- 
			int2Cmd.put(x, cmd.substring(1).toLowerCase());
		}
	}

			
	private ScenarioModel getScenarioModel()
	{
		return this.scenarioModel;
	}
	
		
	public void setScenarioStartTime(Long newTime)
	{
		lastTime = scenarioPlaybackStartTime = newTime;
		
		/*
		Date date = new Date(scenarioPlaybackStartTime);
		// use correct format ('S' for milliseconds)
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		// format date
		String formatted = formatter.format(date);
		 */
	}
	
	
	public void sleepAtLeast(long millis) throws InterruptedException 
	{
		  long t0 = System.currentTimeMillis();
		  long millisLeft = millis;
		  while (millisLeft > 0) 
		  {
			  Thread.sleep(millisLeft);
			  long t1 = System.currentTimeMillis();
			  millisLeft = millis - (t1 - t0);
		  }
	}
	
	private void iterate()
	{
		String value = null;
		parser = new SdtCmdParser(theApp);
		StringBuilder sb = new StringBuilder();
		speedFactor = scenarioController.getView().getSpeedFactorValue();
		
		synchronized(scenarioModel.getSynSdtCommandMap()) {
			
		itr = getScenarioModel().getSynSdtCommandMap().entrySet().iterator();		
		
		while (!stopFlag) {
			
		while (itr.hasNext())
		{
			if (stopFlag) // ?
			{
				return;
			}
			
			checkForPaused();
			
			if (restartPlaybackFlag)
			{
				// reset iterator to start at the beginning
				// hack until we implement sdt state saving
				itr = getScenarioModel().getSynSdtCommandMap().entrySet().iterator();	
				restartPlaybackFlag = false;
				// implement get first
				Iterator<Entry<Long, Map<Integer, String>>> titr = getScenarioModel().getSynSdtCommandMap().entrySet().iterator();
				if (titr.hasNext()) 
				{
					lastTime = titr.next().getKey();
				}
				// Clear state as we are replaying all commands for now
				clearState();
			}
			
			// Get the command value pair
			Entry<Long, Map<Integer,String>> entry = itr.next();
				
			Map<Integer, String> cmdMap = entry.getValue();
			int key = 0; 
			value = null;
			String pendingCmd = null;
			for (Map.Entry<Integer, String> cmdEntry: cmdMap.entrySet())
			{
				key = (int) cmdEntry.getKey();
				pendingCmd = int2Cmd.get(key);
				value = (String) cmdEntry.getValue();
    		}			

			Long waitTime = entry.getKey() - lastTime;
			lastTime = entry.getKey();
										
			
			if (lastTime <= scenarioPlaybackStartTime)
			{
				if (theApp.getCursor().getType() != Cursor.WAIT_CURSOR)
				{
					theApp.setCursor(waitCursor);
				}
				value = " " + pendingCmd + " \"" + value + " \"\n";	
				sb.append(value, 0, value.length());
				parseString(sb, parser);	

			}
			else
			{	
				if (theApp.getCursor().getType() != Cursor.DEFAULT_CURSOR)
				{
					theApp.setCursor(defaultCursor);
				}
			
				value = pendingCmd + " \"" + value + " \"\n";
				try
				{	
					// If we have stopped the playback before all played, play
					// back the remaining commands as fast as possible so state
					// will be correct.
					if (!stopFlag)
					{
						if (waitTime < 0)
						{
							System.out.println("Timeout value is negative!");
							waitTime = (long) 0;
						}
						
						// We want our slider to keep incrementing so if our 
						// wait time is > 1 second, fake it
						int sliderVal = scenarioController.getView().getSliderValue();					
						long secs = waitTime / 1000;

						while (secs > 1)
						{
							sleep((long) (1000 * speedFactor));  
							scenarioController.getView().updateScenarioSecs(sliderVal++);
							secs = secs - 1;
							waitTime = waitTime - 1000;
						} 
						sleep((long) (waitTime * speedFactor));
					}
					if ((!pendingCmd.equalsIgnoreCase("wait"))
							&&
						(!pendingCmd.equalsIgnoreCase("listen")))
					{
						scenarioController.updateScenarioSecs(lastTime);
						sb.append(value, 0, value.length());
						parseString(sb, parser);	
					}

				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();					
				}
			}

		} // end while
		
		// reset our iterator since we can't currently go to a specifc place
		// in our command map.  Tell our controller that we are paused and
		// waiting for user control.
		// implement get first
		Iterator<Entry<Long, Map<Integer, String>>> titr = getScenarioModel().getSynSdtCommandMap().entrySet().iterator();
		if (titr.hasNext()) 
		{
			lastTime = titr.next().getKey();
		}
		itr = getScenarioModel().getSynSdtCommandMap().entrySet().iterator();		

		scenarioController.getView().stopScenarioPlayback();
		try {
			pauseThread();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		} // end while ! stopFlag
		} // end synchronized
		
	
	}
	
	@Override
	public void run()
	{
		
		clearState();
		
		lastTime = new Long(0);	

		// implement a get first
		synchronized(getScenarioModel().getSynSdtCommandMap()) 
		{
			Iterator<Entry<Long, Map<Integer, String>>> titr = getScenarioModel().getSynSdtCommandMap().entrySet().iterator();
			if (titr.hasNext()) 
			{
				lastTime = titr.next().getKey();
			}
		}
	
		iterate();
		
		// If the user interrupted playback allow them to control
		// when we restart
		if (!stopFlag)
		{
			// Play back any cmds added to our buffer while we were stopped
			// threading issue?  playbackBufferedCommands();
			scenarioController.getView().resumeScenarioPlayback();
			
		}		
	}
	
	
	/*
	 * Clear all sdt renderable state
	 */
	private  void clearState()
	{
		final SdtCmdParser parser = new SdtCmdParser(theApp);
		StringBuilder sb = new StringBuilder();
		
		String value = " clear nodes \n";
		sb.append(value, 0, value.length());
		parseString(sb, parser);
		value = " clear regions \n";
		sb.append(value, 0, value.length());
		parseString(sb, parser);
		value = " clear tiles \n";
		sb.append(value, 0, value.length());
		parseString(sb, parser);
		value = " clear linkLabels \n";
		sb.append(value, 0, value.length());
		parseString(sb, parser);
		value = " clear kml \n";
		sb.append(value, 0, value.length());
		parseString(sb, parser);
		value = " status \"\" \n";
		sb.append(value, 0, value.length());
		parseString(sb, parser);
	}

		
	
	private void checkForPaused() 
	{
		synchronized (GUI_MONITOR) {
			while(pauseThreadFlag) 
			{
				try 
				{
					GUI_MONITOR.wait();
				}
				catch (InterruptedException ix)
				{
					System.out.println("Interupted Exception ScenarioThread.checkForPaused()\n");
				}
				catch (Exception e)
				{
					System.out.println("Exception!  ScenarioThread.checkForPaused()\n");
				}

			}
		}
	}
	
	
	public void pauseThread() throws InterruptedException 
	{
		synchronized (GUI_MONITOR) {
			pauseThreadFlag = true;
			GUI_MONITOR.notify();
		}
		
	}
	
	
	public void resumeThread() 
	{
		synchronized (GUI_MONITOR) {
			pauseThreadFlag = false;
			GUI_MONITOR.notify();
		}
	}
	
	
	public void restartPlayback(Long time)
	{
		synchronized (GUI_MONITOR) {
			scenarioPlaybackStartTime = time;
			restartPlaybackFlag = true;
			pauseThreadFlag = false;
			GUI_MONITOR.notify();
		}
	}
	
	
	public void setSpeedFactor(Float speedFactor)
	{
		this.speedFactor = speedFactor;
	}
	
	
	/*
	 * Not currently used
	 */
	void playbackBufferedCommands()
	{
		String value = null;
		//final SdtCmdParser 
		parser = new SdtCmdParser(theApp);
		StringBuilder sb = new StringBuilder();

		synchronized(getScenarioModel().getSynSdtBufferCommandMap()) {
		Iterator<Entry<Long, Map<Integer, String>>> itr = getScenarioModel().getSynSdtBufferCommandMap().entrySet().iterator();		
		while (!stopFlag && itr.hasNext())
		{
			Entry<Long, Map<Integer,String>> entry = itr.next();
			
			Map<Integer, String> cmdMap = entry.getValue();
			int key = 0; 
			value = null;
			String pendingCmd = null;
			for (Map.Entry<Integer, String> cmdEntry: cmdMap.entrySet())
			{
				key = (int) cmdEntry.getKey();
				pendingCmd = int2Cmd.get(key);
				value = (String) cmdEntry.getValue();
    		}			

			value = " " + pendingCmd + " \"" + value + " \"\n";
				
			if ((!pendingCmd.equalsIgnoreCase("wait"))
					&&
				(!pendingCmd.equalsIgnoreCase("listen")))
			{
				sb.append(value, 0, value.length());
				parseString(sb, parser);	
			}
		}
		}
	
	}

}
