package mil.navy.nrl.sdt3d;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
	/*
	* LinkedHashMap maintains insertion order
	* 
	* Key: current time
	* Value: Map<pendingCmd(asInt), value>
	*/
	private LinkedHashMap<Long,Map<Integer,String>> sdtCommandMap = new LinkedHashMap<Long, Map<Integer,String>>();
		
	/*
	 * Collections.synchronizedMap returns a synchronized (thread-safe) map backed by the
	 * specified map.  In order to guarantee serial access, all access to the backing map
	 * must be accomplished through the returned mpa.
	 */
	private Map<Long, Map<Integer, String>> synMap = Collections.synchronizedMap(sdtCommandMap);
		
	/*
	* LinkedHashMap maintains insertion order
	*/
	private LinkedHashMap<Long,Map<Integer,String>> sdtBufferCommandMap = new LinkedHashMap<Long, Map<Integer,String>>();
		
	/*
	 * Collections.synchronizedMap returns a synchronized (thread-safe) map backed by the
	 * specified map.  In order to guarantee serial access, all access to the backing map
	 * must be accomplished through the returned mpa.
	 */
	private Map<Long, Map<Integer, String>> synBufferMap = Collections.synchronizedMap(sdtBufferCommandMap);

	
	ScenarioModel()
	{

	}
	

	void  resetModel()
	{
		sdtCommandMap.clear(); // probably don't need clear
		sdtCommandMap = new LinkedHashMap<Long, Map<Integer,String>>();
		synMap = Collections.synchronizedMap(sdtCommandMap);

		sdtBufferCommandMap.clear();
		sdtBufferCommandMap = new LinkedHashMap<Long, Map<Integer,String>>();
		synBufferMap = Collections.synchronizedMap(sdtBufferCommandMap);
	}
	
	
	Map<Long, Map<Integer, String>> getSynMap()
	{
		return synMap;
	}
	
	
	Map<Long, Map<Integer, String>> getSynBufferMap()
	{
		return synBufferMap;
	}
	
		
	/*
	 * Called from the app when taping the scenario.  
	 */
	void updateModel(int pendingCmd, String val)
	{
		long currentTime = Time.increasingTimeMillis();
		
		Map<Integer,String> cmd = new HashMap<Integer,String>();
		cmd.put(pendingCmd, val);
		synchronized(synMap) {
			synMap.put(currentTime, cmd);
		}
	}	
	

	void appendBufferModel()
	{
		synchronized(synBufferMap)
		{
			synchronized(synMap) 
			{
				// Append any cmds added to our buffer while we were stopped
				
				if ((synBufferMap.size() == 0)
						||
					(synBufferMap.size() == synMap.size()))
				{
					return;
				}
				//System.out.println("Appending synbuffer>" + synBufferMap.size());
				synMap.putAll(synBufferMap);
				sdtBufferCommandMap = new LinkedHashMap<Long, Map<Integer,String>>();
				synBufferMap = Collections.synchronizedMap(sdtBufferCommandMap);
			}
		}
	}
	
	/*
	 * Called from the app when replaying the scenario/ 
	 */
	void updateBufferModel(int pendingCmd, String val)
	{
		long currentTime = Time.increasingTimeMillis();
		
		Map<Integer,String> cmd = new HashMap<Integer,String>();
		cmd.put(pendingCmd, val);
		synchronized(synBufferMap)
		{ 
			synBufferMap.put(currentTime, cmd);
		}
	}	
}
