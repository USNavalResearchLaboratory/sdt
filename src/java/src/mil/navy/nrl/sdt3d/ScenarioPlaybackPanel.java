package mil.navy.nrl.sdt3d;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;


/**
 * @author thompson
 * @since Aug 16, 2019
 */
public class ScenarioPlaybackPanel extends JPanel 
{
	private static final long serialVersionUID = 1L;
	
    // panel components
    private JLabel scenarioTime;
    private JLabel scenarioTimeValue; 
    private JLabel elapsedScenarioTime;
    private JLabel elapsedScenarioTimeValue;
    private JSpinner scenarioSpinner;
    private JSlider scenarioSlider;
    private JButton fastReverseButton;
    private JButton reverseButton;
    private JButton playPauseButton;
    private JButton startStopRecordingButton;
    private JButton saveRecordingButton;
    private JButton loadRecordingButton;
    private JButton configureScenarioButton;
    private JButton forwardButton;
    private JButton fastForwardButton;
    private JLabel speedLabel;
    private JSpinner speedSpinner;
    private JSpinner speedFactorSpinner;
    
    /* Set when USER stops recording but we still
     * want to allow playback of anything recorded
     */
	private boolean playbackOnly = false;

    private int maxSliderValue = 1000;
    private Timer player;
    
    // Consolidate these and use enums 
    private static final int PLAY_PAUSED = 0;
    static final int PLAYING = 2;
    private static final int RECORDING = 3;
    private static final int STOP_RECORDING = 4;
    private static final int START_RECORDING = 5; 
    
    private int playMode = PLAYING; 

    // elapsedSecs is total taped scenario time
    private int elapsedSecs = 0; 
    // scenarioSecs is scenario play time
    private int scenarioSecs = 0;
    // time when scenario is paused to determine if we should skip back/forward
    private int currentSecs = 0;
    
    private sdt3d.AppFrame listener = null;
		 
    public ScenarioPlaybackPanel(sdt3d.AppFrame listener)
    {
        initComponents();
        
        this.playMode = START_RECORDING;
        this.listener = listener;
    }
    
 
    public void initPlayback()
    {
        playPauseButton.setEnabled(true);

    	updateEnabledState(true);
    		
    	setPlayMode(PLAY_PAUSED);
    		
    	playPauseButton.setText("Play");
    
    }
    
 
    public void initRecording()
    {
        this.playPauseButton.setEnabled(false);

    	updateEnabledState(false);
    		
    	setPlayMode(RECORDING);
    		
    	playPauseButton.setText("Play");
    
    }
  
    public void reset()
    {
		playPauseButton.setText("Play");
		playPauseButton.setEnabled(false);
		startStopRecordingButton.setText("Start Recording");
		setPlayMode(PLAYING);
		scenarioSlider.setValue(0);
		if (player != null)
		{
			player.stop();
    		player = null;
		}
		scenarioSecs = 0;
		elapsedSecs = 0;
		playbackOnly = false;
		loadRecordingButton.setEnabled(true);
		saveRecordingButton.setEnabled(false);
		startStopRecordingButton.setEnabled(true);
    }
    
    /*
     * Called when the user initiates stop recording
     */
    public void stopRecording()
    {
		playPauseButton.setText("Play");
		setPlayMode(STOP_RECORDING);
		playbackOnly = true;
		startStopButtonActionPerformed();
		
    }
    
    void initComponents()
    {
        //======== this ========
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        //======== "Position" Section ========
        Box positionPanel = Box.createVerticalBox();
        {
            //======== Position Readout ========
            JPanel readoutPanel = new JPanel(new GridLayout(1, 3, 0, 0)); // nrows, ncols, hgap, vgap
            readoutPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            {
                //======== Scenario Time ========
                Box time = Box.createHorizontalBox();
                {
                    time.add(Box.createHorizontalGlue());
                    elapsedScenarioTime = new JLabel();
                    elapsedScenarioTime.setText("Elapsed Time:");
                    time.add(elapsedScenarioTime);
                    time.add(Box.createHorizontalStrut(3));

                    elapsedScenarioTimeValue = new JLabel();
                    elapsedScenarioTimeValue.setText("0");
                    time.add(elapsedScenarioTimeValue);
                    time.add(Box.createHorizontalGlue());
                    
                    //.add(Box.createHorizontalGlue());
                    scenarioTime = new JLabel();
                    scenarioTime.setText("Time:");
                    time.add(scenarioTime);
                    time.add(Box.createHorizontalStrut(3));

                    scenarioTimeValue = new JLabel();
                    scenarioTimeValue.setText("0");
                    time.add(scenarioTimeValue);
                    time.add(Box.createHorizontalGlue());
  
                }
                readoutPanel.add(time);
            }
            positionPanel.add(readoutPanel);
            positionPanel.add(Box.createVerticalStrut(16));

            //======== Position Spinner, Slider ========
            Box positionControlPanel = Box.createHorizontalBox();
            positionControlPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            {
                //---- Position Spinner ----
                scenarioSpinner = new JSpinner();
                scenarioSpinner.setModel(new SpinnerNumberModel(0,0,maxSliderValue, 1));
                Dimension size = new Dimension(50, this.scenarioSpinner.getPreferredSize().height);
                scenarioSpinner.setMinimumSize(size);
                scenarioSpinner.setPreferredSize(size);
                scenarioSpinner.setMaximumSize(size);
                scenarioSpinner.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        positionSpinnerStateChanged();
                    }
                });
                positionControlPanel.add(this.scenarioSpinner, BorderLayout.WEST);
                positionControlPanel.add(Box.createHorizontalStrut(10));

                //---- Position Slider ----
                scenarioSlider = new JSlider();
                scenarioSlider.setMaximum(maxSliderValue);
                scenarioSlider.setValue(0);
                this.scenarioSlider.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        positionSliderStateChanged();
                    }
                });
                
                positionControlPanel.add(this.scenarioSlider, BorderLayout.CENTER);
            }
            positionPanel.add(positionControlPanel);
            positionPanel.add(Box.createVerticalStrut(16));

            //======== "VCR" Panel ========
            Box vcrPanel = Box.createHorizontalBox();
            vcrPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            {
                vcrPanel.add(Box.createHorizontalGlue());
                //---- "<<" Button ----
                fastReverseButton = new JButton();
                fastReverseButton.setText("<<");
                fastReverseButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        fastReverseButtonActionPerformed();
                    }
                });
                vcrPanel.add(this.fastReverseButton);
                vcrPanel.add(Box.createHorizontalStrut(3));

                //---- "<" Button----
                reverseButton = new JButton();
                reverseButton.setText("<");
                reverseButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        reverseButtonActionPerformed();
                    }
                });
                vcrPanel.add(this.reverseButton);
                vcrPanel.add(Box.createHorizontalStrut(3));

                //---- "Stop" Button ----
                playPauseButton = new JButton();
                playPauseButton.setText("Play");
                playPauseButton.setEnabled(false);
                playPauseButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        startStopButtonActionPerformed();
                    }
                });
                vcrPanel.add(this.playPauseButton);
                vcrPanel.add(Box.createHorizontalStrut(3));

                //---- ">" Button ----
                forwardButton = new JButton();
                forwardButton.setText(">");
                forwardButton.setBorder(UIManager.getBorder("Button.border"));
                forwardButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        forwardButtonActionPerformed();
                    }
                });
                vcrPanel.add(this.forwardButton);
                vcrPanel.add(Box.createHorizontalStrut(3));

                //---- ">>" Button ----
                fastForwardButton = new JButton();
                fastForwardButton.setText(">>");
                fastForwardButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        fastForwardButtonActionPerformed();
                    }
                });
                vcrPanel.add(this.fastForwardButton);

                //--------
                vcrPanel.add(Box.createHorizontalGlue());
            }
            positionPanel.add(vcrPanel);
            positionPanel.add(Box.createVerticalStrut(16));

            //======== "Speed" Panel ========
            Box speedPanel = Box.createHorizontalBox();
            speedPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            {
                speedPanel.add(Box.createHorizontalGlue());
                //---- "Speed:" Label ----
                speedLabel = new JLabel();
                speedLabel.setText("Speed:");
                speedPanel.add(this.speedLabel);
                speedPanel.add(Box.createHorizontalStrut(10));

                //---- Speed Spinner ----
                int numValues = 100;
                String[] speedValues = new String[numValues];
                for (int i = 1; i <= numValues; i++)
                {
                    speedValues[i - 1] = "" + (i * 10);
                }
                speedSpinner = new JSpinner();
                speedSpinner.setModel(new SpinnerListModel(speedValues));
                speedSpinner.setValue("200");
                Dimension size = new Dimension(60, speedSpinner.getPreferredSize().height);
                speedSpinner.setMinimumSize(size);
                speedSpinner.setPreferredSize(size);
                speedSpinner.setMaximumSize(size);

                //---- Speed Multiplier Spinner ----
                speedFactorSpinner = new JSpinner();
                speedFactorSpinner.setModel(new SpinnerListModel(
                    new String[] {"x.01", "x.12", "x.25", "x.50", "x.75", "x1", "x2", "x3", "x4", "x5", "x7", "x10"}));
                speedFactorSpinner.setValue("x1");
                size = new Dimension(60, this.speedFactorSpinner.getPreferredSize().height);
                speedFactorSpinner.setMinimumSize(size);
                speedFactorSpinner.setPreferredSize(size);
                speedFactorSpinner.setMaximumSize(size);
                
                speedFactorSpinner.addChangeListener(new ChangeListener() {
                	@Override
                	public void stateChanged(ChangeEvent e) {
                		Float speedFactor = getSpeedFactorValue();
                		firePropertyChange(ScenarioController.SET_REPLAY_SPEED, null, speedFactor);
                	}
                });
                
                speedPanel.add(this.speedFactorSpinner);
                speedPanel.add(Box.createHorizontalGlue());
                
                //============ "State" Panel ================
                Box statePanel = Box.createHorizontalBox();
                statePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                statePanel.add(Box.createHorizontalGlue());
                
                startStopRecordingButton = new JButton();
                startStopRecordingButton.setText("Start Recording");
                startStopRecordingButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        startStopRecordingButtonActionPerformed();
                    }
                });
 
                statePanel.add(this.startStopRecordingButton);
                statePanel.add(Box.createHorizontalStrut(3));
 
                saveRecordingButton = new JButton();
                saveRecordingButton.setText("Save Recording");
                saveRecordingButton.setEnabled(false);
                saveRecordingButton.addActionListener(new ActionListener()
                {
                	public void actionPerformed(ActionEvent e)
                	{
                		saveRecordingButtonActionPerformed();
                	}
                });
                statePanel.add(saveRecordingButton);
                statePanel.add(Box.createHorizontalStrut(3));
                 
                loadRecordingButton = new JButton();
                loadRecordingButton.setText("Load Recording");
                loadRecordingButton.setEnabled(true);
                loadRecordingButton.addActionListener(new ActionListener()
                	{
                		public void actionPerformed(ActionEvent e)
                		{
                			loadRecordingActionPerformed();
                		}
                	});
                statePanel.add(loadRecordingButton);
                statePanel.add(Box.createHorizontalGlue());
                
                speedPanel.add(statePanel);
                speedPanel.add(Box.createHorizontalStrut(4));
                speedPanel.add(Box.createHorizontalGlue());
 
                
                configureScenarioButton = new JButton();
                configureScenarioButton.setText("Settings");
                configureScenarioButton.setEnabled(false);
                configureScenarioButton.addActionListener(new ActionListener()
                {
                	public void actionPerformed(ActionEvent e)
                	{
                		//configureScenarioButtonActionPerformed();
                	}
                });
 
                speedPanel.add(configureScenarioButton);
                speedPanel.add(Box.createHorizontalStrut(5));
                speedPanel.add(Box.createHorizontalGlue());
 
                
            }
            positionPanel.add(speedPanel);
            positionPanel.add(Box.createVerticalGlue());
        }
        positionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(positionPanel);
        add(Box.createVerticalGlue());
        
        updateEnabledState(false);
    }

    
	void setListener(ScenarioController listener)
	{
		addPropertyChangeListener(listener);
	}
	
	
    private void setPositionSpinnerNumber(int n)
    {
    	scenarioSpinner.setValue(n);
    }

    /*
     * For now, vcr controls etc only enabled when
     * scenario is paused.
     */
    void updateEnabledState(boolean state)
    {
        scenarioSpinner.setEnabled(state);
        scenarioSlider.setEnabled(state);
        fastReverseButton.setEnabled(state);
        reverseButton.setEnabled(state);
        forwardButton.setEnabled(state);
        fastForwardButton.setEnabled(state);
        speedLabel.setEnabled(state);
        speedSpinner.setEnabled(state);
        speedFactorSpinner.setEnabled(state);
    }

    
    Float getSpeedFactorValue()
    {
    	String val = (String) speedFactorSpinner.getValue();
    	String [] value = val.split("x",2);
    	Float speedFactor = new Float(value[1]);
    	return speedFactor;
    }
    
    
    private void positionSpinnerStateChanged()
    {
    	scenarioSlider.setValue(getCurrentPositionNumber());
    }

    
    private void positionSliderStateChanged()
    {
    	// Don't allow slider to go beyond our scenario elapsed time
    	if (scenarioSlider.getValue() >= elapsedSecs)
    	{
    		scenarioSlider.setValue(elapsedSecs);
    	}
    		
    	elapsedScenarioTimeValue.setText(String.valueOf(scenarioSlider.getValue()));
    	
    	scenarioSpinner.setValue(scenarioSlider.getValue());
		firePropertyChange(ScenarioController.UPDATE_TIME, null, scenarioSlider.getValue());

     }

        
    private int getCurrentPositionNumber()
    {
        Object o = this.scenarioSpinner.getValue();
        if (o == null)
            return -1;

        return Integer.parseInt(o.toString().trim().replaceAll(",", ""));
    }

    
    // Not yet implemented
    private void setTimeDelta(int positionNumber, double positionDelta)
    {
    	setPositionSpinnerNumber(positionNumber);
    	int min = this.scenarioSlider.getMinimum();
    	int max = this.scenarioSlider.getMaximum();
    	int value = (int) (min + (double) (max - min) * positionDelta);
    	this.scenarioSlider.setValue(value);
    }

    
    void updatePlaybackTime(Long time)
    {
		Date date = new Date(time);
		// SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSSSSS");
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		String formatted = formatter.format(date);

        this.scenarioTimeValue.setText(formatted); 
    }
    
    
    void updateScenarioSecs(int currentScenarioValue)
    {    	
     	// If we've gone beyond our initial scenario time increase the slider
    	
    	// TODO: Only if playmode == recording
    	if (currentScenarioValue > maxSliderValue)
    	{
    		// TODO: Optimize this
    		maxSliderValue = currentScenarioValue + maxSliderValue/2;
    		scenarioSlider.setMaximum(maxSliderValue);
     	}
    	
     	scenarioSlider.setValue(currentScenarioValue);
        elapsedScenarioTimeValue.setText(String.valueOf(currentScenarioValue)); 
    }
    
    
    // TODO: Resume not yet fully implemented - eventually call this from scenario thread
    // obsolete?
    void resumeScenarioPlayback()
    {
    		playPauseButton.setText("Pause");
    		// Resuming live play will set our slider value to total scenario elapsed time
    		setPlayMode(RECORDING);
    		firePropertyChange(ScenarioController.RESUME_LIVE_PLAY, null, null);
    }
    
    
    // Called by scenario thread when we've played back all
    // commands in the scenario model.  "Continuing" will append
    // buffered commands and continue playback
    void stopScenarioPlayback()
    {
		setPlayMode(PLAY_PAUSED);   
		playPauseButton.setText("Play");
		updateEnabledState(true);
     }
    
    
    private void startStopRecordingButtonActionPerformed()
    {
    	if (playMode == RECORDING)
    	{
    		playMode = PLAY_PAUSED;
     		startStopRecordingButton.setText("Start Recording");
     		saveRecordingButton.setEnabled(true);
     		firePropertyChange(ScenarioController.RECORDING_STOPPED, null, null);
    	}
    	else
    	{
    		playMode = RECORDING;
    		startStopRecordingButton.setText("Stop Recording");
    		loadRecordingButton.setEnabled(false);
    		firePropertyChange(ScenarioController.RECORDING_STARTED, null, null);
    	}    	
    }
    
    
    private void saveRecordingButtonActionPerformed()
    {    	
		Optional<String> fileName = new ScenarioSettingsDialog(listener).show(ScenarioController.SCENARIO_FILENAME);

       	if (!fileName.isPresent())
       	{
       		reset();
       		return;
       	}
       	String scenarioName = fileName.get();
       	ScenarioController.SCENARIO_FILENAME = scenarioName.replace(".cmdMap", "");

       	saveRecordingButton.setEnabled(false);
       	loadRecordingButton.setEnabled(true);
       	firePropertyChange(ScenarioController.SAVE_RECORDING, null, null);
    }
 
    
    private void loadRecordingActionPerformed()
    {
    	loadRecordingButton.setEnabled(false);
    	startStopRecordingButton.setEnabled(false);
    	playPauseButton.setEnabled(false);
    	scenarioSlider.setValue(1);
     	playbackOnly = true;
       	
		Optional<String> fileName = new ScenarioSettingsDialog(listener).show(ScenarioController.SCENARIO_FILENAME);

       	if (!fileName.isPresent())
       	{
       		reset();
       		return;
       	}
       	
    	playPauseButton.setText("Loading...");

       	String scenarioName = fileName.get();
       	ScenarioController.SCENARIO_FILENAME = scenarioName.replace(".cmdMap", "");
       	
    	firePropertyChange(ScenarioController.LOAD_RECORDING, null, null);
    }
     
    
    int getSliderValue()
    {
    	return scenarioSlider.getValue();
    }

    
    void startStopButtonActionPerformed()
    {
 
    	if (playMode == PLAY_PAUSED)
    	{   			
    		playPauseButton.setText("Pause");
       		updateEnabledState(false);

    		scenarioSecs = scenarioSlider.getValue();
    		setScenarioTime(scenarioSecs);
    		setPlayMode(PLAYING);   			

    		if (scenarioSlider.getValue() < currentSecs)
    		{
    			firePropertyChange(ScenarioController.SKIP_BACK, null, scenarioSlider.getValue());
        	}
    		else
    	   	{
    			if (scenarioSlider.getValue() > currentSecs)
    			{
    				firePropertyChange(ScenarioController.SKIP_FORWARD, null, scenarioSlider.getValue());
     	   		}
    			else
    			{
    				// appending the buffer was hanging 
     	   			//firePropertyChange(ScenarioController.START_SCENARIO_PLAYBACK, null, scenarioSecs);
    				firePropertyChange(ScenarioController.RESUME_PLAYBACK, null, scenarioSecs);
    			}
    	   	}
    	}
    	else
    	{
    		playPauseButton.setText("Play");    			
    		updateEnabledState(true);
    			
    		currentSecs = scenarioSlider.getValue();
    		setPlayMode(PLAY_PAUSED);
    		scenarioSecs = scenarioSlider.getValue();
    		firePropertyChange(ScenarioController.STOP_SCENARIO_PLAYBACK, null, scenarioSlider.getValue());  
    	}
    }
    

    private void reverseButtonActionPerformed()
    {	
    	// error checking beginning of scenario
    	scenarioSecs = scenarioSlider.getValue() - 1;
		updateScenarioSecs(scenarioSecs);
		firePropertyChange(ScenarioController.REWIND, 0, scenarioSecs);
    }


    private void fastReverseButtonActionPerformed()
    {		
    	scenarioSecs = scenarioSlider.getValue() - 10;
		updateScenarioSecs(scenarioSecs);
		firePropertyChange(ScenarioController.REWIND, 0, scenarioSecs);

    }

    
    private void forwardButtonActionPerformed()
    {    
   		scenarioSecs = scenarioSlider.getValue() + 1;

		// TODO: Check for end of scenario
		updateScenarioSecs(scenarioSecs);
		// need new skip forward action for other use
		firePropertyChange(ScenarioController.FAST_FORWARD, scenarioSecs, 1);

    }

    
    private void fastForwardButtonActionPerformed()
    {
   		scenarioSecs = scenarioSlider.getValue() + 10;

		// TODO: Check for end of scenario
		updateScenarioSecs(scenarioSecs);
		firePropertyChange(ScenarioController.FAST_FORWARD, scenarioSecs, 10);

    }
    
    
    void setScenarioTime(int scenarioSecs)
    {   
    	this.scenarioSecs = scenarioSecs;
    	
    }
    
    
    /*
     * Update slider max for loaded scenario.
     */
    void setScenarioElapsedSecs(Integer elapsedSecs) 
    {
    	this.elapsedSecs = elapsedSecs;
    	maxSliderValue = elapsedSecs;
    	scenarioSpinner.setModel(new SpinnerNumberModel(0,0,maxSliderValue, 1));
    	scenarioSlider.setMaximum(maxSliderValue);
    }
    
    
    private void initPlayer()
    {
        if (player != null)
            return;
        
        // Player runs continuously keeping track of elapsed scenario time
        // We only update the slider display if taping or playing.
        
        // TODO: ljt update only when taping?
        player = new Timer(1000, new ActionListener()
        {
            // Animate the view motion by controlling the positionSpinner and positionDelta?
            public void actionPerformed(ActionEvent actionEvent)
            {
            	if (playMode != STOP_RECORDING && !playbackOnly)
            	{
            		elapsedSecs++;
            	}
            		
            	if (playMode == RECORDING && !playbackOnly)
            	{
            		updateScenarioSecs(elapsedSecs);
            		
            		// Update our model every second with a dummy command to keep
            		// track of model time.  Otherwise playback controls don't work
            		// quite right at this point.
            		firePropertyChange(ScenarioController.UPDATE_MODEL, null, elapsedSecs);
             	}
            		            		
            	if (playMode == PLAYING)
            	{
            		// LJT - have playback set scenario secs & update scenario time??
            		//scenarioSecs++;
            		//†updateScenarioTime(scenarioSecs);
            	}
            		
            	if (playMode == PLAY_PAUSED)
            	{
            		//updateScenarioTime(scenarioSlider.getValue());
            	}
            }
        });
        player.start();

    }
    
    
    void setPlayMode(int mode)
    {    	
    	// If we stopped recording and now want to replay restart our player
    	if (playMode == STOP_RECORDING)
    	{
    		if (player != null)
    		{
    			player.start();
    		}
    	}
    	
        playMode = mode;
        if ((playMode == PLAYING || playMode == RECORDING  || playMode == START_RECORDING) 
        		&& player == null)
        {
            initPlayer();
        }
    }


	public Integer getElapsedSecs() 
	{
		return this.elapsedSecs;
	}  
}
