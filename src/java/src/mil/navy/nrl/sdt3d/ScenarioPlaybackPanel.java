package mil.navy.nrl.sdt3d;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;

/**
 * @author thompson
 * @since Aug 16, 2019
 */
public class ScenarioPlaybackPanel extends JPanel //implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private PropertyChangeListener propertyChangeListener;
	
	private final PropertyChangeSupport propertyChangeSupport;

	private ScenarioController listener;
	
    // panel components
    private boolean suspendPositionEvents = false;
    private JLabel scenarioTime;
    private JLabel scenarioTimeValue; 
    private JSpinner scenarioSpinner;
    private JSlider scenarioSlider;
    private JButton fastReverseButton;
    private JButton reverseButton;
    private JButton startStopButton;
    private JButton forwardButton;
    private JButton fastForwardButton;
    private JLabel speedLabel;
    private JSpinner speedSpinner;
    private JSpinner speedFactorSpinner;
    
    private int maxSliderValue = 1000;
    private Timer player;
    
    private static final int PLAY_FORWARD = 1;
    private static final int PLAY_BACKWARD = -1;
    //private static final int PLAY_STOP = 0;
    private static final int PLAY_PAUSED = 0;
    private static final int PLAYING = 2;

    private int playMode = PLAY_PAUSED;

    private int elapsedSecs = -1;

 
    public ScenarioPlaybackPanel()
    {
        initComponents();
        this.updateEnabledState();
         
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        
        // TODO: Keep me?
        this.propertyChangeListener = new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent event)
            {
                if (event.getPropertyName().equals(ScenarioController.SCENARIO_MODIFIED))
                {
                		System.out.println("ScenarioPlaybackPanel::SCENARIO_MODIFIED");
                    updatePositionList(false);
                }
            }
        };
    }
    
    
    private void initComponents()
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
                    this.scenarioTime = new JLabel();
                    this.scenarioTime.setText("Time:");
                    time.add(this.scenarioTime);
                    time.add(Box.createHorizontalStrut(3));

                    this.scenarioTimeValue = new JLabel();
                    this.scenarioTimeValue.setText("0");
                    time.add(this.scenarioTimeValue);
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
                this.scenarioSpinner.setModel(new SpinnerListModel(new String[] {"   0"}));
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
                    		// TODO: How to tell that user changed the state?
                    		//System.out.println("State changed slider");
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
                this.startStopButton = new JButton();
                this.startStopButton.setText("Start");
                this.startStopButton.setEnabled(true);
                this.startStopButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        startStopButtonActionPerformed();
                    }
                });
                vcrPanel.add(this.startStopButton);
                vcrPanel.add(Box.createHorizontalStrut(3));

                //---- ">" Button ----
                this.forwardButton = new JButton();
                this.forwardButton.setText(">");
                this.forwardButton.setBorder(UIManager.getBorder("Button.border"));
                this.forwardButton.setEnabled(false);
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
                    new String[] {"x.12", "x.25", "x.50", "x1", "x2", "x3", "x4", "x5", "x7", "x10"}));
                this.speedFactorSpinner.setValue("x1");
                this.speedFactorSpinner.setEnabled(false);
                size = new Dimension(60, this.speedFactorSpinner.getPreferredSize().height);
                this.speedFactorSpinner.setMinimumSize(size);
                this.speedFactorSpinner.setPreferredSize(size);
                this.speedFactorSpinner.setMaximumSize(size);
                speedPanel.add(this.speedFactorSpinner);
                speedPanel.add(Box.createHorizontalGlue());
            }
            positionPanel.add(speedPanel);
            positionPanel.add(Box.createVerticalGlue());
        }
        positionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(positionPanel);
        this.add(Box.createVerticalGlue());
    }

    
	void setUpListeners(ScenarioController controller)
	{
		// TODO: implement all our buttons
		this.listener = controller;
		//sliderButton().addChangeListener(c);
		//stopButton.addChangeListener(c);
		//reverseButton.addChangeListener(c);
		//orwardButton.addChangeListener(c);
		// TODO: Do we really want to listen for changes to the slider??
		//scenarioTimeSlider.addChangeListener(c);
		//previousTimemarkButton().addChangeListener(c);
		//nextTimemarkButton().addChangeListener(c);
		//gearButton().addChangeListener(c);

		startStopButton.addActionListener(Void -> controller.togglePlayOrStop());
		
		// TODO: use time context stuff?
		
		//reverseButton.addActionListener((Void -> c.stepBackward()));
		//forwardButton.addActionListener(Void -> c.stepForward());
		//previousTimemarkButton().addActionListener(Void -> c.goToPreviousTimemark());
		//nextTimemarkButton().addActionListener(Void -> c.goToNextTimemark());
		//gearButton().addActionListener(Void -> c.showPopupMenu());
	}

	private JButton playButton()
	{
		return startStopButton;
	}
	
	
	public boolean isPlayButtonPressed()
	{
		return playButton().isEnabled();
	}
	

    JSlider getScenarioSlider()
    {
    		return scenarioSlider;
    }
    

    private void updatePositionList(boolean resetPosition)
    {
    		
     	//String[] strings = new String[this.sdtCmdMap != null? this.sdtCmdMap.size() : 0];
    		String[] strings = new String[0];
    	
        for (int i = 0; i < strings.length; i++)
        {
            strings[i] = String.format("%,4d", i);
        }

        if (strings.length == 0)
            strings = new String[] {"   0"};

        int currentPosition = Math.min(this.getCurrentPositionNumber(), strings.length - 1);
        int currentSliderValue = this.scenarioSlider.getValue();
        this.scenarioSpinner.setModel(new SpinnerListModel(strings));
        this.scenarioSpinner.setValue(resetPosition ? strings[0] : strings[currentPosition]);
        this.scenarioSlider.setValue(resetPosition ? 0 : currentSliderValue);
    }

    private void setPositionSpinnerNumber(int n)
    {
        this.scenarioSpinner.setValue(String.format("%,4d", n));
    }

    private void updateEnabledState()
    {
        //boolean state = this.sdtCmdMap != null;
        boolean state = true;
        
        this.scenarioSpinner.setEnabled(state);
        this.scenarioSlider.setEnabled(state);
        this.scenarioTime.setEnabled(state);

        this.fastReverseButton.setEnabled(state);
        this.reverseButton.setEnabled(state);
//        this.stopButton.setEnabled(state);
        this.forwardButton.setEnabled(state);
        this.fastForwardButton.setEnabled(state);
        this.speedLabel.setEnabled(state);
        this.speedSpinner.setEnabled(state);
        this.speedFactorSpinner.setEnabled(state);

        //this.updateReadout(this.sdtCmdMap != null && sdtCmdMap.size() > 0 ? sdtCmdMap.get(0)state : null);
    }

    private void positionSpinnerStateChanged()
    {
        if (!this.suspendPositionEvents)
        {
            setTimeDelta(getCurrentPositionNumber(), 0);
            //this.firePropertyChange(POSITION_CHANGE, -1, 0);
        }
    }

    private void positionSliderStateChanged()
    {
        if (!this.suspendPositionEvents)
        {
            //updateTimeDelta();
        		updateReadout(scenarioSlider.getValue());
            listener.firePropertyChange(ScenarioController.POSITION_CHANGE, 0, scenarioSlider.getValue());
        }
    }

    private int getCurrentPositionNumber()
    {
        Object o = this.scenarioSpinner.getValue();
        if (o == null)
            return -1;

        return Integer.parseInt(o.toString().trim().replaceAll(",", ""));
    }

 
    private void setTimeDelta(int positionNumber, double positionDelta)
    {
        // Update UI controls without firing events
        this.suspendPositionEvents = true;
        {
            setPositionSpinnerNumber(positionNumber);
            int min = this.scenarioSlider.getMinimum();
            int max = this.scenarioSlider.getMaximum();
            int value = (int) (min + (double) (max - min) * positionDelta);
            this.scenarioSlider.setValue(value);
        }
        this.suspendPositionEvents = false;

        //this.positionDelta = positionDelta;
    }


    void updateReadout(int time)
    {
        this.scenarioTimeValue.setText(String.valueOf(time)); 
    }

    private double getSpeedFactor()
    {
        String speedFactor = ((String)this.speedFactorSpinner.getValue()).replace("x", "");
        return Double.parseDouble(speedFactor);
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
    		updateReadout(currentScenarioValue);
    }
    
    
    private void startStopButtonActionPerformed()
    {
        // TODO: Add listener or setter/getters
    		if (playMode == PLAY_PAUSED)
    		{
    			startStopButton.setText("Stop");
    			suspendPositionEvents = true;  // TODO: Using?
    			setPlayMode(PLAYING);
    			listener.firePropertyChange(ScenarioController.PLAY_STARTED, null, scenarioSlider.getValue());
    		}
    		else
    		{
    			startStopButton.setText("Start");
    			suspendPositionEvents = false;
    			setPlayMode(PLAY_PAUSED);
    			listener.firePropertyChange(ScenarioController.PLAY_STOPPED, null, scenarioSlider.getValue());    			
    		}
    }
    
    
    private void fastReverseButtonActionPerformed()
    {
        if (this.getCurrentPositionNumber() > 0)
            setPositionSpinnerNumber(this.getCurrentPositionNumber() - 1);
    }

    private void reverseButtonActionPerformed()
    {
 
    		// suspend position events 
    		suspendPositionEvents = true;
    		if (scenarioSlider.getValue() != 0)
    		{
    			updateScenarioTime(scenarioSlider.getValue() - 1);
    		}
    		listener.firePropertyChange(ScenarioController.SKIP_BACK, null, scenarioSlider.getValue());
    		// resume position events
    		//suspendPositionEvents = false;

    }


    private void forwardButtonActionPerformed()
    {    
    		// suspend position events 
		suspendPositionEvents = true;
		// TODO: Check for end of scenario
		updateScenarioTime(scenarioSlider.getValue() + 1);
		listener.firePropertyChange(ScenarioController.SKIP_FORWARD, null, scenarioSlider.getValue());
		// resume position events
		suspendPositionEvents = false;

    }

    private void fastForwardButtonActionPerformed()
    {
       // if (!isLastPosition(this.getCurrentPositionNumber()))
        //    setPositionSpinnerNumber(this.getCurrentPositionNumber() + 1);
    }

    int getElapsedSecs()
    {
    		return this.elapsedSecs;
    }
    
    
    private void initPlayer()
    {
        if (player != null)
            return;
        
        player = new Timer(1000, new ActionListener()
        {
            // Animate the view motion by controlling the positionSpinner and positionDelta
            public void actionPerformed(ActionEvent actionEvent)
            {
            		//elapsedSecs = (int)((System.currentTimeMillis() - ScenarioController.scenarioStartTime)/1000);   
            		elapsedSecs++;
            		runPlayer();
            }
        });
    }
    
    private boolean isPlayerActive()
    {
        return this.playMode != PLAY_PAUSED;
    }
    
    
    private void setPlayMode(int mode)
    {
        this.playMode = mode;
        
        if (player == null)
        {
            initPlayer();
        }
        player.start();
    }

    
    private void runPlayer()
    {
    		if (this.playMode == PLAYING)
    		{
    			updateScenarioTime(elapsedSecs);
    		}
    }
    
 

}
