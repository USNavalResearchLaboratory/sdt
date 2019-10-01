package mil.navy.nrl.sdt3d;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author thompson
 * @since Aug 16, 2019
 */
public class ScenarioModel
{

	private LinkedHashMap<Integer,Map<Integer,String>> sdtCommandMap = new LinkedHashMap<Integer, Map<Integer,String>>();
		
	private Map<Integer, Map<Integer, String>> synMap = Collections.synchronizedMap(sdtCommandMap);
		
	private static int elapsedTime = 0;
	
	ScenarioModel()
	{

	}
	
	
	synchronized Map<Integer, Map<Integer, String>> getModel()
	{
		return synMap;
	}
	
	
	int getElapsedTime()
	{
		return elapsedTime;
	}
	
	/*
	 * Called from the app when taping the scenario.  Elapsed time is the time
	 * sdt has been running (not necessarily "scenario" elapsed time)
	 */
	synchronized void updateModel(long currentTime, int pendingCmd, String val)
	{
		elapsedTime = (int) ((currentTime - ScenarioController.scenarioStartTime));
		
		Map<Integer,String> cmd = new HashMap<Integer,String>();
		cmd.put(pendingCmd, val);
		getModel().put(elapsedTime, cmd);		
	}	
}
