package mil.navy.nrl.sdt3d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Timer;
/**
 * @author thompson
 * @since Aug 16, 2019
 */
public class ScenarioController 
{
	public static final String SCENARIO_MODIFIED = "scenarioModified";
	public static final String SCENARIO_PLAYBACK = "scenarioPlayback";
	public static final String SCENARIO_PLAYBACK_STOPPED = "scenarioPlaybackStopped";

	public static final String SKIP_BACK = "skipBack";
	public static final String SKIP_FORWARD = "skipForward";
	public static final String POSITION_CHANGE = "positionChange";
	public static final String PLAY_STOPPED = "playStopped";
	public static final String PLAY_STARTED = "playStarted";
	
	private ScenarioModel scenarioModel;
	private ScenarioPlaybackPanel scenarioPlaybackPanel;
	private PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);
	
	private sdt3d.AppFrame listener;
	
	// TODO: Where to get start time
	static long scenarioStartTime;
	private Map<Integer, Integer> scenarioTimeMap = new LinkedHashMap<Integer,Integer>();
	private Timer mapTimer = null;

	
	public ScenarioController(sdt3d.AppFrame listener, ScenarioModel scenarioModel, ScenarioPlaybackPanel scenarioPlaybackPanel)
	{
		this.scenarioPlaybackPanel = scenarioPlaybackPanel;
		this.scenarioModel = scenarioModel;
		ScenarioController.scenarioStartTime = System.currentTimeMillis();
		this.listener = listener;
	}

	
	ScenarioModel getScenarioModel()
	{
		return this.scenarioModel;
	}


	private ScenarioPlaybackPanel view()
	{
		return this.scenarioPlaybackPanel;
	}
	
	
	void setUpListeners()
	{
		getScenarioModel().setUpListeners(this);
		view().setUpListeners(this);
	}
	
	/*
	 * Timer to control updating the scenario scrollbar once a second as
	 * new scenario commands are received
	 */
	private void startMapTimer()
	{
		final int POLL_INTERVAL = 1000;
		mapTimer = new Timer(POLL_INTERVAL, new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					updateScenarioTimeMap();					
				}
			});

	}


	public void initController()
	{
		setUpListeners();
		
		startMapTimer();
		
		// TODO: stop map timer
		mapTimer.start();
		
		// TODO: inittimer when scenario actually starts
	    propChangeSupport.addPropertyChangeListener(new PropertyChangeListener()
	        {
	            public void propertyChange(PropertyChangeEvent event)
	            {
	                if (event.getPropertyName().equals(SCENARIO_MODIFIED))
	                {	                		
	                		view().updateScenarioTime((int)event.getNewValue());
	                		view().updateReadout((int)event.getNewValue());
	                }
	                // TODO: clean up
	                if (event.getPropertyName().equals(SKIP_BACK))
	                {
	                		System.out.println("SKIP_BACK");
	                		getSdtCommandAtSliderTime(event);	                	
	                }
	                if (event.getPropertyName().equals(SKIP_FORWARD))
	                {	                		
	                		System.out.println("SKIP_FORWARD"); 
	                		getSdtCommandAtSliderTime(event);
	                }
	                if (event.getPropertyName().equals(POSITION_CHANGE))
	                {	                		
	                		System.out.println("POSITION_CHANGE"); 
	                		getSdtCommandAtSliderTime(event);
	                }
	                if (event.getPropertyName().equals(PLAY_STOPPED))
	                {	                		
	                		System.out.println("play stopped");
	                		listener.modelPropertyChange(ScenarioController.SCENARIO_PLAYBACK_STOPPED, null, null);	
	                }
	                if (event.getPropertyName().equals(PLAY_STARTED))
	                {	                		
	                		System.out.println("play started");
	                		getSdtCommandAtSliderTime(event);
	                		//listener.modelPropertyChange(ScenarioController.SCENARIO_PLAYBACK, null, null);	

	                }
	            }
	        });
	}
	
	
	void getSdtCommandAtSliderTime(PropertyChangeEvent event)
	{
		int sliderStartTime = (int) event.getNewValue();

		if (!scenarioTimeMap.containsKey(sliderStartTime))
		{
			System.out.println("ScenarioController::propertyChange() map does not contain key>" + event.getNewValue());
			return;
		}

		// Get the map key for the scenario slider time
		int scenarioPlaybackStartTime = scenarioTimeMap.get(sliderStartTime);
		listener.modelPropertyChange(ScenarioController.SCENARIO_PLAYBACK, null, scenarioPlaybackStartTime);		
	}
	

	
	void updateTime(long cmdTime)
	{
		// TODO: Delete me?
		// Update time independent of model - not currently used
		firePropertyChange(SCENARIO_MODIFIED, null, cmdTime);
	}
	
	
	/*
	 * Triggered at POLL_INTERVALs
	 */
	private void updateScenarioTimeMap()
	{
		if (view().getElapsedSecs() < 0)
		{
			// Scenario not started yet
			return;
		}
		
		// Take a snapshot of the latest sdt command time at slider scenario elapsed time
		scenarioTimeMap.put(view().getElapsedSecs(), getScenarioModel().getElapsedTime());
	}
	
	// used?
	void togglePlayOrStop()
	{
		if (view().isPlayButtonPressed())
		{
			//listener.modelPropertyChange(ScenarioController.SCENARIO_PLAYBACK, null, null);		
		}
		else
		{
			listener.modelPropertyChange(ScenarioController.SCENARIO_PLAYBACK_STOPPED, null, null);		
		}
	}
		
	
	public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
	{
		this.propChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}


}
