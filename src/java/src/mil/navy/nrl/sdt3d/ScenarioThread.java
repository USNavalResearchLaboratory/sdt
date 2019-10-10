package mil.navy.nrl.sdt3d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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
		// TODO: Get model from app?
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
	
	
	@Override
	public void run()
	{
		// started via thread start
		this.running = true;
		final CmdParser parser = theApp.new CmdParser();
		StringBuilder sb = new StringBuilder();
		
		//String value = " clear all \n";
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
		
		//clearAllRenderables();
		//this.elevationBuilder.clear();
		
		Long lastTime = new Long(0);	
		// implement a get first
		Iterator<Entry<Long, Map<Integer, String>>> titr = getScenarioModel().getModel().entrySet().iterator();
		if (titr.hasNext()) 
		{
			lastTime = titr.next().getKey();
		}
		
		boolean started = false;
		// Get playback speedfactor
		Float speedFactor = scenarioController.getView().getSpeedFactorValue();
		System.out.println("Speed factor>" + speedFactor);
		Iterator<Entry<Long, Map<Integer, String>>> itr = getScenarioModel().getModel().entrySet().iterator();		
		
		synchronized(scenarioModel) {
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
				// Don't start pacing commands until we get to playback time
				value = " " + pendingCmd + " \"" + value + " \"\n";
			}
			else
			{
				if (!started)
				{
					scenarioController.startPlayer(scenarioPlaybackStartTime);
					started = true;
					// No wait when playback starts
					waitTime = new Long(0);
				}
				
				value = pendingCmd + " \"" + value + " \"\n";
				try
				{
					waitTime = (long) (waitTime * speedFactor);
					sleep(waitTime);
					// TODO: set scenario time from here... scenarioController.getView()
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
			else
			{
				System.out.println("pendingcmd equals wait");
			}
		}
		}
		
		running = false;

	} // end ScenarioThread::run()


	public boolean isRunning() 
	{	
		return this.running;
	}
}
