package mil.navy.nrl.sdt3d;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    
    //private boolean suspendPositionEvents = false;
    private int maxSliderValue = 1000;
    private Timer player;
    
    // Consolidate these and use enums 
    private static final int PLAY_FORWARD = 1;
    private static final int PLAY_BACKWARD = -1;
    private static final int PLAY_PAUSED = 0;
    static final int PLAYING = 2;
    private static final int RECORDING = 3;
    private static final int STOP_RECORDING = 4;
    private static final int START_RECORDING = 5; 
    // when sdt3d complete stops playback - cleanup
    private static final int CLEAR_RECORDING = 6;
    
    private int playMode = PLAYING; 

    // elapsedSecs is total taped scenario time
    private int elapsedSecs = 0; 
    // scenarioSecs is scenario play time
    private int scenarioSecs = 0;
    // time when scenario is paused to determine if we should skip back/forward
    private int currentSecs = 0;
		 
    public ScenarioPlaybackPanel()
    {
        initComponents();
        
        this.playPauseButton.setEnabled(true);
        this.playMode = START_RECORDING;
  
    }
    
    /*
     * initPlayback will be called when we add start/stop recording support 
     */
    public void initPlayback()
    {
        this.playPauseButton.setEnabled(true);

    	updateEnabledState(true);
    		
    	setPlayMode(RECORDING);
    		
    	playPauseButton.setText("Pause");
    
    }
    
  
    public void reset()
    {
		// clean all this starting/stopping up!!
		playPauseButton.setText("Start Recording");
		startStopRecordingButton.setText("Stop Recording");
		setPlayMode(CLEAR_RECORDING);
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
    		
    }
    
    /*
     * Called when the user initiates stop recording
     */
    public void stopRecording()
    {
		// clean all this starting/stopping up!!
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
                //======== Latitude ========
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
                this.scenarioSpinner = new JSpinner();
                this.scenarioSpinner.setModel(new SpinnerNumberModel(0,0,maxSliderValue, 1));
                		//new SpinnerListModel(new String[] {"   0"}));
                this.scenarioSpinner.setEnabled(false);
                Dimension size = new Dimension(50, this.scenarioSpinner.getPreferredSize().height);
                this.scenarioSpinner.setMinimumSize(size);
                this.scenarioSpinner.setPreferredSize(size);
                this.scenarioSpinner.setMaximumSize(size);
                this.scenarioSpinner.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        positionSpinnerStateChanged();
                    }
                });
                positionControlPanel.add(this.scenarioSpinner, BorderLayout.WEST);
                positionControlPanel.add(Box.createHorizontalStrut(10));

                //---- Position Slider ----
                this.scenarioSlider = new JSlider();
                this.scenarioSlider.setMaximum(maxSliderValue);
                this.scenarioSlider.setValue(0);
                this.scenarioSlider.setEnabled(false);
                
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
                this.fastReverseButton = new JButton();
                this.fastReverseButton.setText("<<");
                this.fastReverseButton.setEnabled(false);
                this.fastReverseButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        fastReverseButtonActionPerformed();
                    }
                });
                vcrPanel.add(this.fastReverseButton);
                vcrPanel.add(Box.createHorizontalStrut(3));

                //---- "<" Button----
                this.reverseButton = new JButton();
                this.reverseButton.setText("<");
                this.reverseButton.setEnabled(false);
                this.reverseButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        reverseButtonActionPerformed();
                    }
                });
                vcrPanel.add(this.reverseButton);
                vcrPanel.add(Box.createHorizontalStrut(3));

                //---- "Stop" Button ----
                this.playPauseButton = new JButton();
                this.playPauseButton.setText("Start Recording");
                this.playPauseButton.setEnabled(false);
                this.playPauseButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        startStopButtonActionPerformed();
                    }
                });
                vcrPanel.add(this.playPauseButton);
                vcrPanel.add(Box.createHorizontalStrut(3));

                //---- ">" Button ----
                this.forwardButton = new JButton();
                this.forwardButton.setText(">");
                this.forwardButton.setBorder(UIManager.getBorder("Button.border"));
                this.forwardButton.setEnabled(true);
                this.forwardButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        forwardButtonActionPerformed();
                    }
                });
                vcrPanel.add(this.forwardButton);
                vcrPanel.add(Box.createHorizontalStrut(3));

                //---- ">>" Button ----
                this.fastForwardButton = new JButton();
                this.fastForwardButton.setText(">>");
                this.fastForwardButton.setEnabled(false);
                this.fastForwardButton.addActionListener(new ActionListener()
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
                this.speedLabel = new JLabel();
                this.speedLabel.setText("Speed:");
                speedPanel.add(this.speedLabel);
                speedPanel.add(Box.createHorizontalStrut(10));

                //---- Speed Spinner ----
                int numValues = 100;
                String[] speedValues = new String[numValues];
                for (int i = 1; i <= numValues; i++)
                {
                    speedValues[i - 1] = "" + (i * 10);
                }
                this.speedSpinner = new JSpinner();
                this.speedSpinner.setModel(new SpinnerListModel(speedValues));
                this.speedSpinner.setValue("200");
                this.speedSpinner.setEnabled(false);
                Dimension size = new Dimension(60, this.speedSpinner.getPreferredSize().height);
                this.speedSpinner.setMinimumSize(size);
                this.speedSpinner.setPreferredSize(size);
                this.speedSpinner.setMaximumSize(size);
                speedPanel.add(this.speedSpinner);
                speedPanel.add(Box.createHorizontalStrut(10));

                //---- Speed Multiplier Spinner ----
                this.speedFactorSpinner = new JSpinner();
                this.speedFactorSpinner.setModel(new SpinnerListModel(
                    new String[] {"x.01", "x.12", "x.25", "x.50", "x.75", "x1", "x2", "x3", "x4", "x5", "x7", "x10"}));
                this.speedFactorSpinner.setValue("x1");
                this.speedFactorSpinner.setEnabled(false);
                size = new Dimension(60, this.speedFactorSpinner.getPreferredSize().height);
                this.speedFactorSpinner.setMinimumSize(size);
                this.speedFactorSpinner.setPreferredSize(size);
                this.speedFactorSpinner.setMaximumSize(size);
                speedPanel.add(this.speedFactorSpinner);
                speedPanel.add(Box.createHorizontalGlue());
                
                //============ "State" Panel ================
                Box statePanel = Box.createHorizontalBox();
                statePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                statePanel.add(Box.createHorizontalGlue());
                
                startStopRecordingButton = new JButton();
                startStopRecordingButton.setText("Stop Recording");
                startStopRecordingButton.setEnabled(false);
                startStopRecordingButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        stopRecordingButtonActionPerformed();
                    }
                });
 
                statePanel.add(this.startStopRecordingButton);
                statePanel.add(Box.createHorizontalStrut(3));
                //statePanel.add(Box.createHorizontalGlue());
 
                saveRecordingButton = new JButton();
                saveRecordingButton.setText("Save Recording");
                saveRecordingButton.setEnabled(true);;
                saveRecordingButton.addActionListener(new ActionListener()
                	{
                		public void actionPerformed(ActionEvent e)
                		{
                			saveStateButtonActionPerformed();
                		}
                	});
                statePanel.add(saveRecordingButton);
                statePanel.add(Box.createHorizontalStrut(3));
                //statePanel.add(Box.createHorizontalGlue());
            
                
                loadRecordingButton = new JButton();
                loadRecordingButton.setText("Load Recording");
                loadRecordingButton.setEnabled(true);
                loadRecordingButton.addActionListener(new ActionListener()
                	{
                		public void actionPerformed(ActionEvent e)
                		{
                			loadStateButtonActionPerformed();
                		}
                	});
                statePanel.add(loadRecordingButton);
                statePanel.add(Box.createHorizontalGlue());
                
                speedPanel.add(statePanel);
                speedPanel.add(Box.createHorizontalStrut(4));
                speedPanel.add(Box.createHorizontalGlue());
 
                
                configureScenarioButton = new JButton();
                configureScenarioButton.setText("Settings");
                configureScenarioButton.setEnabled(true);
                configureScenarioButton.addActionListener(new ActionListener()
                {
                	public void actionPerformed(ActionEvent e)
                	{
                		configureScenarioButtonActionPerformed();
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
        this.add(positionPanel);
        this.add(Box.createVerticalGlue());
    }

    
	void setListener(ScenarioController listener)
	{
		addPropertyChangeListener(listener);

	}
	

    JSlider getScenarioSlider()
    {
    		return scenarioSlider;
    }
        
    // Spinner not yet implemented
    private void setPositionSpinnerNumber(int n)
    {
        //this.scenarioSpinner.setValue(String.format("%,4d", n));
    	scenarioSpinner.setValue(n);
    }

    
    private void updateEnabledState(boolean state)
    {
        scenarioSpinner.setEnabled(state);
        scenarioSlider.setEnabled(state);
        scenarioTime.setEnabled(state);
        elapsedScenarioTime.setEnabled(state);
        fastReverseButton.setEnabled(false);
        reverseButton.setEnabled(false);
        forwardButton.setEnabled(state);
        fastForwardButton.setEnabled(false);
        speedLabel.setEnabled(state);
        speedSpinner.setEnabled(false);
        speedFactorSpinner.setEnabled(false);
    }

    
    Float getSpeedFactorValue()
    {
    	String val = (String) speedFactorSpinner.getValue();
    	String [] value = val.split("x",2);
    	Float speedFactor = new Float(value[1]);
    	return speedFactor;
    }
    
    
    // Spinner not yet implemented
    private void positionSpinnerStateChanged()
    {
    	//setTimeDelta(getCurrentPositionNumber(), 0);
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
     }

    
    void updateReadout(int time)
    {
        elapsedScenarioTimeValue.setText(String.valueOf(time)); 
    }

    
    // Spinner not yet implemented
    private int getCurrentPositionNumber()
    {
        Object o = this.scenarioSpinner.getValue();
        if (o == null)
            return -1;

        return Integer.parseInt(o.toString().trim().replaceAll(",", ""));
    }

    
    // Spinner not yet implemented
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
		// use correct format ('S' for milliseconds)
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSSSSS");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		// format date
		String formatted = formatter.format(date);
		//System.out.println("lastTime> " + formatted + " scenarioPlaybackStartTime> " + scenarioPlaybackStartTime);

        this.scenarioTimeValue.setText(formatted); 
    }
    
    
    void updateScenarioTime(int currentScenarioValue)
    {
     	// If we've gone beyond our initial scenario time increase the slider
    	if (currentScenarioValue > maxSliderValue)
    	{
    		// TODO: Optimize this
    		maxSliderValue = currentScenarioValue + maxSliderValue/2;
    		scenarioSlider.setMaximum(maxSliderValue);
     	}
    	
     	scenarioSlider.setValue(currentScenarioValue);
        elapsedScenarioTimeValue.setText(String.valueOf(currentScenarioValue)); 

     	//updateReadout(currentScenarioValue);
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
		speedFactorSpinner.setEnabled(false);
     }
    
    
    private void stopRecordingButtonActionPerformed()
    {
    		startStopRecordingButton.setEnabled(false);
    		//startStopRecordingButton.setText("Playback Only Mode");
    		// if playback mode only already set do nothing
    		firePropertyChange(ScenarioController.RECORDING_STOPPED, null, null);
    }
    
    
    private void saveStateButtonActionPerformed()
    {
       	configureScenarioButtonActionPerformed();
       	saveRecordingButton.setEnabled(false);
       	firePropertyChange(ScenarioController.SAVE_RECORDING, null, null);
    }
 
    
    private void loadStateButtonActionPerformed()
    {
    	
    	configureScenarioButtonActionPerformed();
    	loadRecordingButton.setEnabled(false);
		playPauseButton.setText("Start Recording");
    	playPauseButton.setEnabled(false);
    	initPlayback();
    	//setPlayMode(PLAYING);
    	playbackOnly = true;
    	//updateEnabledState(true);
    	firePropertyChange(ScenarioController.LOAD_RECORDING, null, null);
    }
 
    
    private void configureScenarioButtonActionPerformed()
    {
    	//loadStateButton.setEnabled(false);
    	//initPlayback();
    	//setPlayMode(PLAYING);
    	//playbackOnly = true;
    	//updateEnabledState(true);
    	playPauseButton.setEnabled(false);
    	playPauseButton.setText("Loading Recording");
    	firePropertyChange(ScenarioController.CONFIGURE_SCENARIO, null, null);
    }
    
    
    void startStopButtonActionPerformed()
    {
    	// TODO: ljt switch statement?
    	if (playMode == START_RECORDING || playMode == CLEAR_RECORDING)
    	{
    		playPauseButton.setText("Pause");
    		playPauseButton.setEnabled(false);
    		speedFactorSpinner.setEnabled(false);
    		elapsedSecs = 0;
    		scenarioSecs = 0;
    		setScenarioTime(scenarioSecs);
    		if (playMode == START_RECORDING)
    		{
    			playbackOnly = false;
    		}
    		System.out.println("START_RECORDING scenarioSecs> " + scenarioSecs);
       		 
    		setPlayMode(START_RECORDING);   
    		startStopRecordingButton.setEnabled(playMode == START_RECORDING);
    		firePropertyChange(ScenarioController.RECORDING_STARTED, null, scenarioSecs);
    		return;
    	}
    	
    	if (playMode == PLAY_PAUSED)
    	{   			
    		playPauseButton.setText("Pause");
    		speedFactorSpinner.setEnabled(false);
    		scenarioSecs = scenarioSlider.getValue();
    		setScenarioTime(scenarioSecs);
    		System.out.println("PLAYING scenarioSecs> " + scenarioSecs);
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
     	   			firePropertyChange(ScenarioController.START_SCENARIO_PLAYBACK, null, scenarioSecs);
    			}
    	   	}
    	}
    	else
    	{
    		playPauseButton.setText("Play");    			
    		speedFactorSpinner.setEnabled(true);
    			
    		currentSecs = scenarioSlider.getValue();
    		setPlayMode(PLAY_PAUSED);
    		scenarioSecs = scenarioSlider.getValue();
    		System.out.println("PAUSED currentSecs> " + currentSecs + " scenarioSlider value> " + scenarioSlider.getValue());
    		firePropertyChange(ScenarioController.STOP_SCENARIO_PLAYBACK, null, scenarioSlider.getValue());  
    	}
    }
    
    // not yet implemented
    private void fastReverseButtonActionPerformed()
    {
        if (this.getCurrentPositionNumber() > 0)
            setPositionSpinnerNumber(this.getCurrentPositionNumber() - 1);
    }

    // not yet implemented
    private void reverseButtonActionPerformed()
    {
    		if (scenarioSlider.getValue() != 0)
    		{
    			updateScenarioTime(scenarioSlider.getValue() - 1);
    		}
    		firePropertyChange(ScenarioController.SKIP_BACK, null, scenarioSlider.getValue());
    }

    // not yet implemented
    private void forwardButtonActionPerformed()
    {    
		// TODO: Check for end of scenario
		updateScenarioTime(scenarioSlider.getValue() + 10);
		scenarioSlider.setValue(scenarioSlider.getValue() + 10);
		firePropertyChange(ScenarioController.SKIP_FORWARD, null, scenarioSlider.getValue());
    }

    // not yet implemented
    private void fastForwardButtonActionPerformed()
    {
    		//if (!isLastPosition(this.getCurrentPositionNumber()))
    		//	setPositionSpinnerNumber(this.getCurrentPositionNumber() + 1);
    }
    
    
    void setScenarioTime(int scenarioSecs)
    {   
    		this.scenarioSecs = scenarioSecs;
    }
    
    
    void setElapsedSecs(Integer elapsedSecs) 
    {
    		this.elapsedSecs = elapsedSecs;
    		
    		maxSliderValue = this.elapsedSecs;
    		
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
            // Animate the view motion by controlling the positionSpinner and positionDelta
            public void actionPerformed(ActionEvent actionEvent)
            {
            	if (playMode != STOP_RECORDING && !playbackOnly)
            	{
            		elapsedSecs++;
            	}
            		
            	if (playMode == RECORDING && !playbackOnly)
            	{
            		updateScenarioTime(elapsedSecs);
            	}
            		            		
            	if (playMode == PLAYING)
            	{
            		scenarioSecs++;
            		updateScenarioTime(scenarioSecs);
            	}
            		
            	if (playMode == PLAY_PAUSED)
            	{
            		updateScenarioTime(scenarioSlider.getValue());
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
