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

import mil.navy.nrl.sdt3d.sdt3d.AppFrame.CmdParser;

/**
 * This class listens for SDT commands from a ScenarioController
 * and passes them to the "sdt3dApp"
 * 
 * @author Laurie Thompson
 * @since Aug 23, 2019
 */

public class ScenarioThread extends SocketThread
{
	protected boolean stopFlag = false;
	
	private boolean running = false;

	private ScenarioModel scenarioModel;
	
	private ScenarioController scenarioController;
	
	private Long scenarioPlaybackStartTime;
	
	int lastWaitTime = 0;
	
	HashMap<Integer, String> int2Cmd;

	public ScenarioThread(sdt3d.AppFrame theApp, ScenarioController scenarioController, HashMap<Integer, String> int2Cmd, Long scenarioPlaybackStartTime)
	{
		super(theApp, 0);
		this.scenarioController = scenarioController;
		this.scenarioModel = scenarioController.getScenarioModel();
		this.scenarioPlaybackStartTime = scenarioPlaybackStartTime;
		this.int2Cmd = int2Cmd;
		isScenarioThread = true;
	}


	public boolean stopped()
	{
		return stopFlag;

	}


	public void stopThread()
	{
		stopFlag = true;
	}

	
	private ScenarioModel getScenarioModel()
	{
		return this.scenarioModel;
	}
	
	private  void clearState()
	{
		final CmdParser parser = theApp.new CmdParser();
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
	
	
	@Override
	public void run()
	{
		// started via thread start
		this.running = true;
		
		clearState();
		
		
		Long lastTime = new Long(0);	
		// implement a get first
		Iterator<Entry<Long, Map<Integer, String>>> titr = getScenarioModel().getModel().entrySet().iterator();
		if (titr.hasNext()) 
		{
			lastTime = titr.next().getKey();
		}
		
		boolean started = false;
		// Get playback speedfactor
		String value = null;
		final CmdParser parser = theApp.new CmdParser();
		StringBuilder sb = new StringBuilder();
		Float speedFactor = scenarioController.getView().getSpeedFactorValue();

		Iterator<Entry<Long, Map<Integer, String>>> itr = getScenarioModel().getModel().entrySet().iterator();		
		
		synchronized(scenarioModel.getModel()) {
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

			Long waitTime = entry.getKey() - lastTime;
			lastTime = entry.getKey();
			
			
			if (lastTime < scenarioPlaybackStartTime)
			{
				value = " " + pendingCmd + " \"" + value + " \"\n";
			}
			else
			{
				if (!started)
				{
					started = true;
					// No wait when playback starts
					waitTime = new Long(0);
				}
				
				value = pendingCmd + " \"" + value + " \"\n";
				try
				{
					waitTime = (long) (waitTime * speedFactor);
					sleep(waitTime);
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
				sb.append(value, 0, value.length());
				parseString(sb, parser);	
			}
		}
		}
		
		// Fix threading issues with buffer
		//LIVE theApp.setPlaybackScenario(false);
		//LIVE playbackBufferedCommands();
		
		running = false;		
		scenarioController.getView().stopScenarioPlayback();
		//LIVE scenarioController.getView().resumeScenarioPlayback();

	} // end ScenarioThread::run()


	void playbackBufferedCommands()
	{
		String value = null;
		final CmdParser parser = theApp.new CmdParser();
		StringBuilder sb = new StringBuilder();

		Iterator<Entry<Long, Map<Integer, String>>> itr = getScenarioModel().getBufferModel().entrySet().iterator();		
		
		synchronized(scenarioModel.getBufferModel()) {
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
