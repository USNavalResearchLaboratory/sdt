package mil.navy.nrl.sdt3d;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.text.html.HTMLDocument.Iterator;

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
	

	void loadModel()
	{
		LinkedHashMap<Long,Map<Integer,String>> sdtCommandMap = null; //new LinkedHashMap<Long, Map<Integer,String>>();
		FileInputStream fis;
		try {
			fis = new FileInputStream("hashmap.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			sdtCommandMap = (LinkedHashMap<Long, Map<Integer, String>>) ois.readObject();
			System.out.println(sdtCommandMap);
			ois.close();
			fis.close();

		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Deserialized hashmap..");
		
	}
	
	
	void  clearModelState()
	{
		/*FileOutputStream fos;
		try {
			fos = new FileOutputStream("hashmap.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(sdtCommandMap);
			oos.close();
			fos.close();
	
		}  catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		sdtCommandMap.clear(); // probably don't need clear
		sdtCommandMap = new LinkedHashMap<Long, Map<Integer,String>>();
		synMap = Collections.synchronizedMap(sdtCommandMap);

		sdtBufferCommandMap.clear();
		sdtBufferCommandMap = new LinkedHashMap<Long, Map<Integer,String>>();
		synBufferMap = Collections.synchronizedMap(sdtBufferCommandMap);
		
		//loadModel();
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
