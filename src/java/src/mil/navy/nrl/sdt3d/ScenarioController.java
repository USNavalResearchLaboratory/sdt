package mil.navy.nrl.sdt3d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Timer;

import mil.navy.nrl.sdt3d.sdt3d.AppFrame.Time;
/**
 * @author thompson
 * @since Aug 16, 2019
 */
public class ScenarioController implements PropertyChangeListener
{
	public static final String SCENARIO_MODIFIED = "scenarioModified";
	public static final String SCENARIO_PLAYBACK = "scenarioPlayback";
	public static final String SCENARIO_PLAYBACK_STOPPED = "scenarioPlaybackStopped";
	public static final String SCENARIO_STARTED = "scenarioStarted";

	public static final String SKIP_BACK = "skipBack";
	public static final String SKIP_FORWARD = "skipForward";
	public static final String POSITION_CHANGE = "positionChange";
	public static final String PLAY_STOPPED = "playStopped";
	public static final String PLAY_STARTED = "playStarted";
	
	
	private ScenarioModel scenarioModel = new ScenarioModel();
	private ScenarioPlaybackPanel scenarioPlaybackPanel;
	
	private sdt3d.AppFrame listener;
	
	// Current time when taping begins
	static long scenarioStartTime;
	private Map<Integer, Long> scenarioSliderTimeMap = new LinkedHashMap<Integer,Long>();
	private Timer commandMapTimer = null;

	
	public ScenarioController(sdt3d.AppFrame listener, ScenarioPlaybackPanel scenarioPlaybackPanel)
	{
		scenarioStartTime = System.currentTimeMillis();
		
		this.scenarioPlaybackPanel = scenarioPlaybackPanel;
		this.listener = listener;
		
		initController();
	}

	
	ScenarioModel getScenarioModel()
	{
		return this.scenarioModel;
	}


	private ScenarioPlaybackPanel getView()
	{
		return this.scenarioPlaybackPanel;
	}
	

	synchronized void updateModel(int pendingCmd, String val)
	{
		scenarioModel.updateModel(pendingCmd, val);
	}
	
	
	public int getScenarioSecsFromRealTime(Long realScenarioTime)
	{
		// TODO: Create reverse map?
		for (Map.Entry<Integer, Long> entry: scenarioSliderTimeMap.entrySet())
		{
			if (entry.getValue() >= realScenarioTime) 
			{
				System.out.println("Seeting scenario secs> " + entry.getKey() + " value>" + entry.getValue());

				return entry.getKey();
			}
		}
		return 0;
	}
	
	
	/*
	 * Called by the scenario thread 
	 */
	public void startPlayer(Long scenarioTime)
	{
		getView().startPlayer(getScenarioSecsFromRealTime(scenarioTime));
	}

	
	public void initController()
	{
		getView().setListener(this);

		startCommandMapTimer();		
	}
	
	
	@Override
	public void propertyChange(PropertyChangeEvent event)
	{		
		//System.out.println("ScenarioController::propertyChange");
		if (event.getPropertyName().equals(SCENARIO_STARTED))
		{
			System.out.println("Controller propertyChange SCENARIO_STARTED");
			///view().setScenarioTime((int)event.getNewValue());
            			
		}
            	
		if (event.getPropertyName().equals(SCENARIO_MODIFIED))
		{	    
			System.out.println("Controller propertyChange SCENARIO_MODIFIED");

			getView().updateScenarioTime((int)event.getNewValue());
			getView().updateReadout((int)event.getNewValue());
		}
		// TODO: clean up
		if (event.getPropertyName().equals(SKIP_BACK))
		{
			System.out.println("Controller propertyChange SKIP_BACK");
			getCommandAtSliderTime(event);	                	
		}
		if (event.getPropertyName().equals(SKIP_FORWARD))
		{	                		
			System.out.println("Controller propertyChange SKIP_FORWARD"); 
			getCommandAtSliderTime(event);
		}
		if (event.getPropertyName().equals(POSITION_CHANGE))
		{	                		
			//System.out.println("Controller propertyChange POSITION_CHANGE this is called every time the slider changes"); 
			//getCommandAtSliderTime(event);
		}
		if (event.getPropertyName().equals(PLAY_STOPPED))
		{	                		
			System.out.println("Controller propertyChange PLAY_STOPPED");
			listener.modelPropertyChange(ScenarioController.SCENARIO_PLAYBACK_STOPPED, null, null);	
		}
		if (event.getPropertyName().equals(PLAY_STARTED))
		{	                		
			System.out.println("Controller propertyChange PLAY_STARTED");  // getcommandatslidertime sends scenario_playback to sdt3d // do that here?  10/7
			getCommandAtSliderTime(event);
                		//listener.modelPropertyChange(ScenarioController.SCENARIO_PLAYBACK, null, null);	

		}
	}
	
	void getCommandAtSliderTime(PropertyChangeEvent event)
	{
		int sliderStartTime = (int) event.getNewValue();

		if (!scenarioSliderTimeMap.containsKey(sliderStartTime))
		{
			System.out.println("ScenarioController::propertyChange() map does not contain key>" + event.getNewValue());
			return;
		}

		// Get the command map key for the scenario slider time
		Long scenarioPlaybackStartTime = scenarioSliderTimeMap.get(sliderStartTime);

		listener.modelPropertyChange(ScenarioController.SCENARIO_PLAYBACK, sliderStartTime, scenarioPlaybackStartTime);		
	}
	
	
	/*
	 * Timer to control updating the scenario scrollbar once a second as
	 * new scenario commands are received
	 */
	private void startCommandMapTimer()
	{
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
}
