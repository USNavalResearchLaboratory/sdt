package mil.navy.nrl.sdt3d;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.StringReader;
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

	protected sdt3d.AppFrame sdt3dApp;
	
	private ScenarioModel scenarioModel;
	
	private ScenarioController scenarioController;
	
	private int scenarioPlaybackStartTime;

	int lastWaitTime = 0;
	
	HashMap<Integer, String> int2Cmd;

// TODO: Change superclass !
	public ScenarioThread(sdt3d.AppFrame theSdtApp, ScenarioController scenarioController, HashMap<Integer, String> int2Cmd, int scenarioPlaybackStartTime)
	{
		super(theSdtApp, 0);
		sdt3dApp = theSdtApp;
		this.scenarioController = scenarioController;
		// TODO: Get model from app?
		this.scenarioModel = scenarioController.getScenarioModel();
		this.scenarioPlaybackStartTime = scenarioPlaybackStartTime;
		this.int2Cmd = int2Cmd;
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
	
	void setScenarioPlaybackTime(int scenarioPlaybackStartTime)
	{
		this.scenarioPlaybackStartTime = scenarioPlaybackStartTime;
		System.out.println("scenarioPlaybackStartTime> " + scenarioPlaybackStartTime);
	}
	
	@Override
	public void run()
	{
		// started via thread start
		this.running = true;
		final CmdParser parser = sdt3dApp.new CmdParser();
		StringBuilder sb = new StringBuilder();
		
		// test clear state
		String value = " clear all \n";
		sb.append(value, 0, value.length());
		parseString(sb, parser);
		
		int lastTime = 0;	
		// implement a get first
		Iterator<Entry<Integer, Map<Integer, String>>> titr = getScenarioModel().getModel().entrySet().iterator();
		if (titr.hasNext()) 
		{
			lastTime = titr.next().getKey();
		}
		// Intialize scenario start time ljt fire?
		scenarioController.setScenarioTime(scenarioPlaybackStartTime/1000);

		Iterator<Entry<Integer, Map<Integer, String>>> itr = getScenarioModel().getModel().entrySet().iterator();		
		synchronized(scenarioModel) {
		while (!stopFlag && itr.hasNext())
		{
			System.out.println("Scenario Playback iterating");
			Entry<Integer, Map<Integer,String>> entry = itr.next();
				
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

			int waitTime = entry.getKey() - lastTime;
			lastTime = entry.getKey();
			
			System.out.println("lastTime> " + lastTime/1000 + " scenarioPlaybackStartTime> " + scenarioPlaybackStartTime/1000); 
			if (lastTime < scenarioPlaybackStartTime)
			{
				// Don't start pacing commands until we get to playback time
				value = " " + pendingCmd + " \"" + value + " \"\n";
			}
			else
			{
				value = pendingCmd + " \"" + value + " \"\n";
				try
				{
					System.out.println("Sleeping> " + waitTime/1000);
					sleep(waitTime);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				scenarioController.startPlayer();
			}
			System.out.println("cmd> " + value);

			/*

			if (this.lastWaitTime > 0)
			{
				currentTime = System.currentTimeMillis();
				elapsedTime = currentTime - this.lastWaitTime;
				sleepTime = sleepTime - elapsedTime;
				if (sleepTime < 0)
					sleepTime = 0;
			}
	
			*/
			if (!pendingCmd.equalsIgnoreCase("wait"))
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
