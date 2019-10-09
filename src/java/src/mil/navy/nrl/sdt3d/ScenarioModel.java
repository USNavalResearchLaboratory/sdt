package mil.navy.nrl.sdt3d;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import mil.navy.nrl.sdt3d.sdt3d.AppFrame.Time;

/**
 * @author thompson
 * @since Aug 16, 2019
 */
public class ScenarioModel
{

	private LinkedHashMap<Long,Map<Integer,String>> sdtCommandMap = new LinkedHashMap<Long, Map<Integer,String>>();
		
	private Map<Long, Map<Integer, String>> synMap = Collections.synchronizedMap(sdtCommandMap);
		
	private static int elapsedTime = 0;
	
	ScenarioModel()
	{

	}
	
	
	synchronized Map<Long, Map<Integer, String>> getModel()
	{
		return synMap;
	}
	
	
	int getElapsedTime()
	{
		return elapsedTime;
	}
	
	/*
	 * Called from the app when taping the scenario.  
	 */
	synchronized void updateModel(int pendingCmd, String val)
	{
		
		long currentTime = Time.increasingTimeMillis();
		
		Map<Integer,String> cmd = new HashMap<Integer,String>();
		cmd.put(pendingCmd, val);
		getModel().put(currentTime, cmd);
	}	
}
