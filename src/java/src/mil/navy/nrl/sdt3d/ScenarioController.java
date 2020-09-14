package mil.navy.nrl.sdt3d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import javax.swing.JFrame;
import javax.swing.Timer;

import mil.navy.nrl.sdt3d.sdt3d.AppFrame.Time;
/**
 * @author thompson
 * @since Aug 16, 2019
 */
public class ScenarioController implements PropertyChangeListener
{
	public static final String START_SCENARIO_PLAYBACK = "scenarioPlayback";
	public static final String STOP_SCENARIO_PLAYBACK = "scenarioPlaybackStopped";
	public static final String RECORDING_STARTED = "recordingStarted"; // TODO: cleanup
	public static final String RECORDING_STOPPED = "recordingStopped";
	public static final String SAVE_RECORDING = "saveState";
	public static final String LOAD_RECORDING = "loadState";
	public static final String CONFIGURE_SCENARIO = "configureScenario";
	
	public static final String SKIP_BACK = "skipBack";
	public static final String SKIP_FORWARD = "skipForward";
	public static final String POSITION_CHANGE = "positionChange";
	
	boolean recording = false;
	
	// TODO: Not yet implemented
	public static final String RESUME_LIVE_PLAY = "resumeLivePlay";
	
	private ScenarioModel scenarioModel;
	
	private ScenarioPlaybackPanel scenarioPlaybackPanel;
	
	private sdt3d.AppFrame listener;
	
	private Map<Integer, Long> scenarioSliderTimeMap = new LinkedHashMap<Integer,Long>();
		
	private Timer commandMapTimer = null;
	
	static String SCENARIO_FILENAME = System.getProperty("user.dir") 
			+ File.separator
			+ "sdtScenarioRecording";

	private final HashMap<String, Integer> cmd2Int = new HashMap<>();

	public ScenarioController(sdt3d.AppFrame listener, 
			ScenarioPlaybackPanel scenarioPlaybackPanel)
	{
		this.scenarioModel = new ScenarioModel();
		this.scenarioPlaybackPanel = scenarioPlaybackPanel;
		this.listener = listener;
		initialize_cmd_map();
		initController();
	}

	public void initController()
	{
		getView().setListener(this);		
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
			// Load our cmd maps and remove the leading +/- 
			cmd2Int.put(cmd.substring(1).toLowerCase(), x);
		}
	}
	
	ScenarioModel getScenarioModel()
	{
		return this.scenarioModel;
	}


	ScenarioPlaybackPanel getView()
	{
		return this.scenarioPlaybackPanel;
	}
	

	void updateModel(int pendingCmd, String val)
	{
		scenarioModel.updateModel(pendingCmd, val);
	}
	
	
	void updateBufferModel(int pendingCmd, String val)
	{
		scenarioModel.updateBufferModel(pendingCmd, val);
	}
	
	
	void appendBufferModel()
	{
		scenarioModel.appendBufferModel();
	}
	

	/*
	 * Called by the scenario thread to update the HH:MM:SS field
	 * time is the current time saved in the command map.
	 */
	void updatePlaybackTime(Long time)
	{
		scenarioPlaybackPanel.updatePlaybackTime(time);
	}
	

	int getScenarioSecsFromRealTime(Long realScenarioTime)
	{
		// TODO:  Fix/Optimize!
		commandMapTimer.stop();
		int sliderTime = 0;
		for (Map.Entry<Integer, Long> entry: scenarioSliderTimeMap.entrySet())
		{
			if (entry.getValue() >= realScenarioTime) 
			{
			
				//Date theTime = new Date(realScenarioTime);
			
				sliderTime = entry.getKey();
				break;
			}
			// If no key is greater than realScenarioTime return last time in our slider
			sliderTime = entry.getKey();
		}
		commandMapTimer.start();
		return sliderTime;
	}
	
	
	/*
	 * Called by the scenario thread to set slider to
	 * scenario playback time
	 */
	public void setScenarioTime(Long scenarioTime)
	{
		// TODO: Implement
		getView().setScenarioTime(getScenarioSecsFromRealTime(scenarioTime));
	}

	
	private void startRecording()
	{
		getView().initPlayback();
		
		startCommandMapTimer();
		
		recording = true;
		
		// Put an entry in our model so we track when recording
		// is actually started.  Change sdt3d title.
		String scenarioName = SCENARIO_FILENAME;
		int sepPos = SCENARIO_FILENAME.lastIndexOf("/");
		if (sepPos != -1)
		{
			scenarioName = SCENARIO_FILENAME.substring(++sepPos);
		}
		updateModel(cmd2Int.get("title"), "Scenario Playback (" + scenarioName + ")");
	}
	
	/*
	 * Reset stops all recording and clears the data model
	 * Called by a hard system reset.
	 */
	
	//not uesd?? delete
	public void reset()
	{
		getView().reset();
		
		recording = false;
		stopCommandMapTimer();
		scenarioSliderTimeMap = new LinkedHashMap<Integer,Long>();
		commandMapTimer = null;
		scenarioModel.clearModelState();
	
	}
	
	private void saveState()
	{
		// Save the model state (sdt commands)
		getScenarioModel().saveRecording(SCENARIO_FILENAME + ".cmdMap");
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(SCENARIO_FILENAME + ".timeMap");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(scenarioSliderTimeMap);
			oos.close();
			fos.close();

		}  catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	void configureScenarioState()
	{
		Optional<String> fileName = new ScenarioSettingsDialog(listener).show(SCENARIO_FILENAME);

		if (!fileName.isPresent())
		{
			// user cancelled - abort
			// LJT TODO: handle other config options
			return;
		}
		// We allow the user to select the command map as "the"
		// scenario file but we have two files so strip off the
		// extension.  TODO: create some hidden subfiles or something
		String scenarioName = fileName.get();
		SCENARIO_FILENAME = scenarioName.replace(".cmdMap","");
	}
	
		
	void loadRecording()
	{
		// Load scenario commands
		getScenarioModel().loadRecording(SCENARIO_FILENAME + ".cmdMap");
		
		scenarioSliderTimeMap = null;
		FileInputStream fis;
		try {
			// Load scenario command time map
			fis = new FileInputStream(SCENARIO_FILENAME + ".timeMap");
			ObjectInputStream ois = new ObjectInputStream(fis);			
			scenarioSliderTimeMap = (Map<Integer, Long>) ois.readObject();
			ois.close();
			fis.close();

		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Integer elapsedSecs = getElapsedSecs();
		
		getView().setElapsedSecs(elapsedSecs);
	}
	
	
	Integer  getElapsedSecs()
	{
		//Map.Entry<Integer, Long> entry = (Entry<Integer, Long>) scenarioSliderTimeMap.entrySet().toArray()[scenarioSliderTimeMap.size()];
		//Integer.valueOf(scenarioSliderTimeMap.entrySet().toArray()[scenarioSliderTimeMap.size()]);
		
		Integer key = 0;
		for (Map.Entry<Integer, Long> entry : scenarioSliderTimeMap.entrySet()) {
			key = entry.getKey();
		}		
		return key;  
	}
	
	/*
	 * Stop recording is called when the user stops recording.
	 * The data model is retained for user playback.
	 */	
	public void stopRecording()
	{
		getView().stopRecording();
		recording = false;
		stopCommandMapTimer();
		commandMapTimer = null;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event)
	{	
		// TODO: Switch statement
		if (event.getPropertyName().equals(RECORDING_STARTED))
		{
			if (!recording)
			{
				startRecording();
			}
			listener.modelPropertyChange(ScenarioController.RECORDING_STARTED, null, null);		

		}

		if (event.getPropertyName().equals(RECORDING_STOPPED))
		{
			listener.modelPropertyChange(ScenarioController.RECORDING_STOPPED, null, null);
		}
		
		if (event.getPropertyName().equals(SAVE_RECORDING))
		{
			// Stop recording and save state
			listener.modelPropertyChange(ScenarioController.RECORDING_STOPPED, null, null);
			saveState();
		}

		if (event.getPropertyName().equals(LOAD_RECORDING))
		{
			listener.modelPropertyChange(ScenarioController.RECORDING_STOPPED, null, null);
			loadRecording();
			listener.modelPropertyChange(ScenarioController.LOAD_RECORDING, null, null);
		}

		if (event.getPropertyName().equals(CONFIGURE_SCENARIO))
		{
			// Stop recording and save state?
			// LJT TODO listener.modelPropertyChange(ScenarioController.RECORDING_STOPPED, null, null);
			configureScenarioState();
		}
		
		if (event.getPropertyName().equals(STOP_SCENARIO_PLAYBACK))
		{	                		
			listener.modelPropertyChange(ScenarioController.STOP_SCENARIO_PLAYBACK, null, event.getNewValue());
			
		}
		
		if (event.getPropertyName().equals(START_SCENARIO_PLAYBACK))
		{	                		
			int sliderStartTime = (int) event.getNewValue();
			
			Long scenarioPlaybackStartTime = (long) sliderStartTime;
			if (sliderStartTime != 0)
			{
				scenarioPlaybackStartTime = scenarioSliderTimeMap.get(sliderStartTime);
			}
			listener.modelPropertyChange(ScenarioController.START_SCENARIO_PLAYBACK, sliderStartTime, scenarioPlaybackStartTime);		

		}
		
		if (event.getPropertyName().equals(RESUME_LIVE_PLAY))
		{
			listener.modelPropertyChange(ScenarioController.RESUME_LIVE_PLAY, null, null);
		}
		
		if (event.getPropertyName().equals(SKIP_FORWARD))
		{
			int sliderStartTime = (int) event.getNewValue();
						
			Long scenarioPlaybackStartTime = scenarioSliderTimeMap.get(sliderStartTime);

			listener.modelPropertyChange(ScenarioController.SKIP_FORWARD, sliderStartTime, scenarioPlaybackStartTime);		
		}
		
		if (event.getPropertyName().equals(SKIP_BACK))
		{
			Long scenarioPlaybackStartTime = scenarioSliderTimeMap.get(event.getNewValue());

			if (scenarioPlaybackStartTime == null)
			{
				scenarioPlaybackStartTime = (long) 0;
			}
			listener.modelPropertyChange(ScenarioController.SKIP_BACK, 0, scenarioPlaybackStartTime);		
		}
	}
		
	
	/*
	 * Timer to control updating the scenario scrollbar once a second as
	 * new scenario commands are received
	 */
	private void startCommandMapTimer()
	{
		// TODO: Should we do this in playback player?
		final int POLL_INTERVAL = 1000;
		commandMapTimer = new Timer(POLL_INTERVAL, new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					// Take a snapshot of time at slider time
					long currentTime = Time.increasingTimeMillis();
					scenarioSliderTimeMap.put(getView().getElapsedSecs(), currentTime); 
				}
			});
		
		commandMapTimer.start();

	}
	
	private void stopCommandMapTimer()
	{
		if (commandMapTimer != null)
		{
			commandMapTimer.stop();
		}
	}
}
