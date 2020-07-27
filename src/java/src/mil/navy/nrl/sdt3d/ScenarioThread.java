package mil.navy.nrl.sdt3d;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;


/**
 * This class listens for SDT commands from a ScenarioController
 * and passes them to the "sdt3dApp"
 * 
 * @author Laurie Thompson
 * @since Aug 23, 2019
 */

public class ScenarioThread extends SocketThread
{
	/*
	 * stopFlag is used for scenario play/pause control
	 */
	protected boolean stopFlag = false;
	
	/* 
	 * stopRecordingFlag is set when we want to completely stop
	 * recording - e.g. a hard reset occurs or when the
	 * user stops recording (when that is implemented)
	 */
	protected boolean stopRecordingFlag = false;
		
	private boolean running = false;

	private ScenarioModel scenarioModel;
	
	private ScenarioController scenarioController;
	
	private Long scenarioPlaybackStartTime;
	
	Long lastTime = new Long(0);	
	
	private final HashMap<Integer, String> int2Cmd = new HashMap<>();


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
	public boolean stopped()
	{
		return stopFlag;

	}


	public void stopThread()
	{
		stopFlag = true;
	}

	/*
	 * Called when we stop recording completely.
	 */
	public void stopRecording()
	{
		stopRecordingFlag = true;
	}
	
		
	private ScenarioModel getScenarioModel()
	{
		return this.scenarioModel;
	}
	
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
	
	
	public void setScenarioStartTime(Long newTime)
	{
		scenarioPlaybackStartTime = newTime;
		lastTime = scenarioPlaybackStartTime;
		
		Date date = new Date(scenarioPlaybackStartTime);
		// use correct format ('S' for milliseconds)
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		// format date
		String formatted = formatter.format(date);

		
		System.out.println("New scenariostarttime value> " + formatted);
	}
	
	
	@Override
	public void run()
	{
		System.out.println("ScenarioThread::Run()");
		// started via thread start
		this.running = true;
		clearState();
		lastTime = new Long(0);	
		long firstTime = new Long(0);	

		// implement a get first
		synchronized(getScenarioModel().getSynSdtCommandMap()) 
		{
			Iterator<Entry<Long, Map<Integer, String>>> titr = getScenarioModel().getSynSdtCommandMap().entrySet().iterator();
			if (titr.hasNext()) 
			{
				lastTime = titr.next().getKey();
				firstTime = lastTime;
			}
		}
		boolean started = false;
		// Get playback speedfactor
		String value = null;
		final SdtCmdParser parser = new SdtCmdParser(theApp);
		StringBuilder sb = new StringBuilder();
		Float speedFactor = scenarioController.getView().getSpeedFactorValue();

		
		synchronized(scenarioModel.getSynSdtCommandMap()) {
		Iterator<Entry<Long, Map<Integer, String>>> itr = getScenarioModel().getSynSdtCommandMap().entrySet().iterator();		

		//long gatedWaitTime = 0;
		
		while (itr.hasNext())
		{
			/*
			 * Recording has been completely stopped, exit thread.
			 */
			if (stopRecordingFlag)
			{
				System.out.println("ScenarioThread() Recording stopped");
				return;
			}
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

			//
			//System.out.println("\n\nlastTime> " + lastTime + " key> " + entry.getKey());
			Long waitTime = entry.getKey() - lastTime;
			//gatedWaitTime = gatedWaitTime + waitTime;
			lastTime = entry.getKey();
			
			
			Date date = new Date(lastTime);
			// use correct format ('S' for milliseconds)
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSSSSS");
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			// format date
			String formatted = formatter.format(date);
			//System.out.println("lastTime> " + formatted + " scenarioPlaybackStartTime> " + scenarioPlaybackStartTime);

			Date sdate = new Date(scenarioPlaybackStartTime);
			// use correct format ('S' for milliseconds)
			SimpleDateFormat sformatter = new SimpleDateFormat("HH:mm:ss.SSSSSS");
			sformatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			// format date
			String sformatted = sformatter.format(sdate);

			Date fdate = new Date(firstTime);
			// use correct format ('S' for milliseconds)
			SimpleDateFormat fformatter = new SimpleDateFormat("HH:mm:ss.SSSSSS");
			fformatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			// format date
			String fformatted = fformatter.format(fdate);

			//System.out.println("FirstTime> " + fformatted + " waitTime> " + waitTime + " LastTime> " + formatted + " >= scenarioPlaybackStartTime \n" + sformatted 
			//		+ " " + pendingCmd + " " + value);

			Date wdate = new Date(waitTime);
			// use correct format ('S' for milliseconds)
			SimpleDateFormat wformatter = new SimpleDateFormat("HH:mm:ss.SSSSSS");
			wformatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			// format date
			String wformatted = wformatter.format(wdate);

			//System.out.println("FirstTime> " + fformatted + " wait> " + wformatted + " waitTime> " + waitTime + 
			//		"\n LastTime> " + formatted + " >= scenarioPlaybackStartTime " + sformatted + 
			//		//"\n gatedWaitTime> " + gatedWaitTime + "\n" +
			//		" " + pendingCmd + " " + value);

			
			if (lastTime <= scenarioPlaybackStartTime)
			{
				value = " " + pendingCmd + " \"" + value + " \"\n";	

			}
			else
			{
				if (!started)
				{
					started = true;
				}
				
				//System.out.println("	LastTime> " + formatted + " >= scenarioPlaybackStartTime " + sformatted);

				value = pendingCmd + " \"" + value + " \"\n";
				try
				{	
					// If we have stopped the playback before all played, play
					// back the remaining commands as fast as possible so state
					// will be correct.
					if (!stopFlag)
					{
						//if (gatedWaitTime > 100)
						//{
						//	sleep((long) (gatedWaitTime * speedFactor));
						//	gatedWaitTime = 0;
						//}
						waitTime = (long) (waitTime * speedFactor);
						
						sleep(waitTime);

					}
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if ((!pendingCmd.equalsIgnoreCase("wait"))
					&&
				(!pendingCmd.equalsIgnoreCase("listen")))
			{
				//scenarioController.getView().resumeScenarioPlayback();
				// ljt review all this
				scenarioController.updatePlaybackTime(lastTime);
				sb.append(value, 0, value.length());
				parseString(sb, parser);	
			}
		}
		
		
		
		running = false;		
		// If the user interrupted playback allow them to control
		// when we restart
		if (!stopFlag)
		{
			// Play back any cmds added to our buffer while we were stopped
			// threading issue?  playbackBufferedCommands();
			scenarioController.getView().resumeScenarioPlayback();
			
		}
		}
	} // end ScenarioThread::run()


	/*
	 * Not currently used
	 */
	void playbackBufferedCommands()
	{
		String value = null;
		final SdtCmdParser parser = new SdtCmdParser(theApp);
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
	
	
	public boolean isRunning() 
	{	
		return this.running;
	}
}
