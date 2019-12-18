package mil.navy.nrl.sdt3d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

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
	public static final String SAVE_STATE = "saveState";
	public static final String LOAD_STATE = "loadState";
	
	public static final String SKIP_BACK = "skipBack";
	public static final String SKIP_FORWARD = "skipForward";
	public static final String POSITION_CHANGE = "positionChange";
	
	boolean recording = false;
	
	// TODO: Not yet implemented
	public static final String RESUME_LIVE_PLAY = "resumeLivePlay";
	
	
	private ScenarioModel scenarioModel = new ScenarioModel();
	private ScenarioPlaybackPanel scenarioPlaybackPanel;
	
	private sdt3d.AppFrame listener;
	
	private Map<Integer, Long> scenarioSliderTimeMap = new LinkedHashMap<Integer,Long>();
		
	private Timer commandMapTimer = null;

	
	public ScenarioController(sdt3d.AppFrame listener, ScenarioPlaybackPanel scenarioPlaybackPanel)
	{
		this.scenarioPlaybackPanel = scenarioPlaybackPanel;
		this.listener = listener;
		
		initController();
	}

	public void initController()
	{
		getView().setListener(this);		
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
	
	
	int getScenarioSecsFromRealTime(Long realScenarioTime)
	{
		// TODO:  Fix/Optimize!
		commandMapTimer.stop();
		int sliderTime = 0;
		for (Map.Entry<Integer, Long> entry: scenarioSliderTimeMap.entrySet())
		{
			if (entry.getValue() >= realScenarioTime) 
			{
			
				Date theTime = new Date(realScenarioTime);
				System.out.println("realScenarioTime> " + realScenarioTime + " key>" + entry.getKey() + " fmtReal> " + theTime);
				
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
		getView().setScenarioTime(getScenarioSecsFromRealTime(scenarioTime));
	}

	
	private void startRecording()
	{
		getView().initPlayback();
		
		startCommandMapTimer();
		
		recording = true;
	}
	
	/*
	 * Reset stops all recording and clears the data model
	 * Called by a hard system reset.
	 */
	public void reset()
	{
		getView().clearRecording();
		
		recording = false;
		stopCommandMapTimer();
		scenarioSliderTimeMap = new LinkedHashMap<Integer,Long>();
		commandMapTimer = null;
		scenarioModel.clearModelState();
	
	}
	
	private void saveState()
	{
		getScenarioModel().saveState();
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream("controllerState.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(scenarioSliderTimeMap);
			oos.close();
			fos.close();

		}  catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
		
	void loadState()
	{
		getScenarioModel().loadState();
		
		scenarioSliderTimeMap = null;
		FileInputStream fis;
		try {
			fis = new FileInputStream("controllerState.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);			
			scenarioSliderTimeMap = (Map<Integer, Long>) ois.readObject();
			//System.out.println(scenarioSliderTimeMap);
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
		System.out.println("here");
		// hack for now
		//Map.Entry<Integer, Long> entry = (Entry<Integer, Long>) scenarioSliderTimeMap.entrySet().toArray()[scenarioSliderTimeMap.size()];
		Integer key = 0;
		for (Map.Entry<Integer, Long> entry : scenarioSliderTimeMap.entrySet()) {
			key = entry.getKey();
			//ArrayList<String> value = entry.getValue();
			}
		
		return key;  //Integer.valueOf(scenarioSliderTimeMap.entrySet().toArray()[scenarioSliderTimeMap.size()]);
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
		if (event.getPropertyName().equals(RECORDING_STARTED))
		{
			//System.out.println("Controller propertyChange PLAY_STARTING");

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
		
		if (event.getPropertyName().equals(SAVE_STATE))
		{
			// Stop recording and save state
			listener.modelPropertyChange(ScenarioController.RECORDING_STOPPED, null, null);
			saveState();
			//listener.modelPropertyChange(ScenarioController.SAVE_STATE, null, null);
		}

		if (event.getPropertyName().equals(LOAD_STATE))
		{
			// Stop recording and save state
			listener.modelPropertyChange(ScenarioController.RECORDING_STOPPED, null, null);
			loadState();
			listener.modelPropertyChange(ScenarioController.LOAD_STATE, null, null);
		}

		
		//System.out.println("ScenarioController::propertyChange");
		if (event.getPropertyName().equals(STOP_SCENARIO_PLAYBACK))
		{	                		
			//System.out.println("Controller propertyChange PLAY_STOPPED");
			listener.modelPropertyChange(ScenarioController.STOP_SCENARIO_PLAYBACK, null, null);
			
		}
		
		if (event.getPropertyName().equals(START_SCENARIO_PLAYBACK))
		{	                		
			//System.out.println("Controller propertyChange PLAY_STARTED");

			getCommandAtSliderTime(event);
		}
		
		if (event.getPropertyName().equals(RESUME_LIVE_PLAY))
		{
			//System.out.println("Controller propertyChange RESUME_LIVE_PLAY");
			listener.modelPropertyChange(ScenarioController.RESUME_LIVE_PLAY, null, null);
		}
	}
	
	
	void getCommandAtSliderTime(PropertyChangeEvent event)
	{
		int sliderStartTime = (int) event.getNewValue();
		
		// If no value provided start at beginning
		if (sliderStartTime == 0)
		{
			if (!scenarioSliderTimeMap.isEmpty())
			{
				Map.Entry<Integer, Long> entry = scenarioSliderTimeMap.entrySet().iterator().next();
				sliderStartTime = entry.getKey();
			}
		} 
		else 
		{
			if (!scenarioSliderTimeMap.containsKey(sliderStartTime))
			{	
				System.out.println("ScenarioController::propertyChange() map does not contain key>" + event.getNewValue());
				// Stop playback
				getView().startStopButtonActionPerformed();
				return;
			}
		}

		
		// Get the command map key for the scenario slider time
		Long scenarioPlaybackStartTime = scenarioSliderTimeMap.get(sliderStartTime);
		listener.modelPropertyChange(ScenarioController.START_SCENARIO_PLAYBACK, sliderStartTime, scenarioPlaybackStartTime);		
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
