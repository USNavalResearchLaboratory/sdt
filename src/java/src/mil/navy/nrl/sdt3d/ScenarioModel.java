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
		
	//private Map<Integer, Map<Integer, String>> synMap = Collections.synchronizedMap(sdtCommandMap);
	
	private ScenarioController listener;
	
	private int elapsedTime = 0;
	
	ScenarioModel()
	{

	}
	
	
	synchronized LinkedHashMap<Integer,Map<Integer,String>> getModel()
	{
		return sdtCommandMap;
		//return this.synMap;
	}
	
	
	int getElapsedTime()
	{
		return elapsedTime;
	}
	

	void updateModel(long currentTime, int pendingCmd, String val)
	{
		System.out.println("Updating model");
		elapsedTime = (int) ((currentTime - ScenarioController.scenarioStartTime));
		
		Map<Integer,String> cmd = new HashMap<Integer,String>();
		cmd.put(pendingCmd, val);
		sdtCommandMap.put(elapsedTime, cmd);
		//synMap.put(elapsedTime, cmd);
						
		// Tell the controller to update the view
		//this.listener.firePropertyChange(ScenarioController.SCENARIO_MODIFIED, null, timeInSecs);
		
	}
	
	
	
	void setUpListeners(ScenarioController listener)
	{
		this.listener = listener;
	}


}
