package mil.navy.nrl.sdt3d;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

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
	private Map<Long, Map<Integer, String>> synSdtCommandMap = Collections.synchronizedMap(sdtCommandMap);
		
	/*
	* LinkedHashMap maintains insertion order
	*/
	private LinkedHashMap<Long,Map<Integer,String>> sdtBufferCommandMap = new LinkedHashMap<Long, Map<Integer,String>>();
		
	/*
	 * Collections.synchronizedMap returns a synchronized (thread-safe) map backed by the
	 * specified map.  In order to guarantee serial access, all access to the backing map
	 * must be accomplished through the returned mpa.
	 */
	private Map<Long, Map<Integer, String>> synSdtBufferCommandMap = Collections.synchronizedMap(sdtBufferCommandMap);

	
	ScenarioModel()
	{
	}
	
	
	void loadRecording(String scenarioFileName)
	{
		try {				
			FileInputStream fis = new FileInputStream(scenarioFileName);

		
			Instant startTime = Instant.now();

			// The command map keeps a list of commands received and time
			// of reception
			LinkedHashMap<Long,Map<Integer,String>> sdtCommandMap = null; 
			ObjectInputStream ois = new ObjectInputStream(fis);
			sdtCommandMap = (LinkedHashMap<Long, Map<Integer, String>>) ois.readObject();
			System.out.println(sdtCommandMap);
			ois.close();
			fis.close();

			synSdtCommandMap = Collections.synchronizedMap(sdtCommandMap);

			System.out.println("cmdMap> " + sdtCommandMap);
			
			Instant endTime = Instant.now();
			Duration interval = Duration.between(startTime, endTime);
			System.out.println("loadModelState() Execution time in seconds: " + interval.getSeconds());
			
			// The buffered command map keeps track of commands received 
			// while we are playing back a part of a scenario
			sdtBufferCommandMap = new LinkedHashMap<Long, Map<Integer,String>>();
			synSdtBufferCommandMap = Collections.synchronizedMap(sdtBufferCommandMap);
		
			endTime = Instant.now();
			interval = Duration.between(startTime, endTime);
			System.out.println("loadRecording() Total execution time in seconds: " + interval.getSeconds());

		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	void saveRecording(String scenarioFileName)
	{
		FileOutputStream fos;
		try {
			Instant startTime = Instant.now();
			
			fos = new FileOutputStream(scenarioFileName);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(sdtCommandMap);
			oos.close();
			fos.close();
			
			Instant endTime = Instant.now();
			Duration interval = Duration.between(startTime, endTime);
			System.out.println("saveRecording() Execution time in seconds: " + interval.getSeconds());
	
		}  catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	void  clearModelState()
	{	
		sdtCommandMap.clear(); 
		sdtCommandMap = new LinkedHashMap<Long, Map<Integer,String>>();
		synSdtCommandMap = Collections.synchronizedMap(sdtCommandMap);

		sdtBufferCommandMap.clear();
		sdtBufferCommandMap = new LinkedHashMap<Long, Map<Integer,String>>();
		synSdtBufferCommandMap = Collections.synchronizedMap(sdtBufferCommandMap);
		
	}
	
	
	Map<Long, Map<Integer, String>> getSynSdtCommandMap()
	{
		return synSdtCommandMap;
	}
	
	
	Map<Long, Map<Integer, String>> getSynSdtBufferCommandMap()
	{
		return synSdtBufferCommandMap;
	}
	
		
	/*
	 * Called from the app when taping the scenario.  
	 */
	void updateModel(int pendingCmd, String val)
	{
		long currentTime = Time.increasingTimeMillis();
		
		Date wdate = new Date(currentTime);
		// use correct format ('S' for milliseconds)
		SimpleDateFormat wformatter = new SimpleDateFormat("HH:mm:ss.SSSSSS");
		wformatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		// format date
		String wformatted = wformatter.format(wdate);

		
		System.out.println("time> " + wformatted + "currentTime> " + currentTime + " " + pendingCmd + " " + val);
		
		Map<Integer,String> cmd = new HashMap<Integer,String>();
		cmd.put(pendingCmd, val);
		synchronized(synSdtCommandMap) {
			synSdtCommandMap.put(currentTime, cmd);
		}
	}	
	

	/*
	 * Appends any commands stored in the buffered command map
	 * to our primary command map.
	 */
	void appendBufferModel()
	{
		synchronized(synSdtBufferCommandMap)
		{
			synchronized(synSdtCommandMap) 
			{
				// Append any cmds added to our buffer while we were stopped
				
				if ((synSdtBufferCommandMap.size() == 0)
						||
					(synSdtBufferCommandMap.size() == synSdtCommandMap.size()))
				{
					return;
				}
				//System.out.println("Appending synbuffer>" + synBufferMap.size());
				synSdtCommandMap.putAll(synSdtBufferCommandMap);
				sdtBufferCommandMap = new LinkedHashMap<Long, Map<Integer,String>>();
				synSdtBufferCommandMap = Collections.synchronizedMap(sdtBufferCommandMap);
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
		synchronized(synSdtBufferCommandMap)
		{ 
			synSdtBufferCommandMap.put(currentTime, cmd);
		}
	}	
}
