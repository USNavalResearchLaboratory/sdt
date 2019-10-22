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
public class ScenarioPlaybackPanel extends JPanel 
{
	private static final long serialVersionUID = 1L;
	
    // panel components
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
    
    //private boolean suspendPositionEvents = false;
    private int maxSliderValue = 1000;
    private Timer player;
    
    private static final int PLAY_FORWARD = 1;
    private static final int PLAY_BACKWARD = -1;
    private static final int PLAY_PAUSED = 0;
    static final int PLAYING = 2;
    private static final int TAPING = 3;

    private int playMode = PLAYING; 

    // elapsedSecs is total taped scenario time
    private int elapsedSecs = 0; 
    // scenarioSecs is scenario play time
    private int scenarioSecs = 0;
		 
    public ScenarioPlaybackPanel()
    {
        initComponents();
         
        updateEnabledState(true);

        setPlayMode(TAPING);
        
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
                this.startStopButton.setText("Stop");
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
                    new String[] {"x.01", "x.12", "x.25", "x.50", "x.75", "x1", "x2", "x3", "x4", "x5", "x7", "x10"}));
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
        this.scenarioSpinner.setValue(String.format("%,4d", n));
    }

    
    private void updateEnabledState(boolean state)
    {
        this.scenarioSpinner.setEnabled(state);
        this.scenarioSlider.setEnabled(state);
        this.scenarioTime.setEnabled(state);
        this.fastReverseButton.setEnabled(false);
        this.reverseButton.setEnabled(false);
        this.forwardButton.setEnabled(false);
        this.fastForwardButton.setEnabled(false);
        this.speedLabel.setEnabled(state);
        this.speedSpinner.setEnabled(false);
        this.speedFactorSpinner.setEnabled(false);
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
    		setTimeDelta(getCurrentPositionNumber(), 0);
    }

    
    private void positionSliderStateChanged()
    {
    		// Don't allow slider to go beyone our scenario elapsed time
    		if (scenarioSlider.getValue() >= elapsedSecs)
    		{
    			scenarioSlider.setValue(elapsedSecs);
    		}
    		updateReadout(scenarioSlider.getValue());
    		firePropertyChange(ScenarioController.POSITION_CHANGE, 0, scenarioSlider.getValue());
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


    void updateReadout(int time)
    {
        this.scenarioTimeValue.setText(String.valueOf(time)); 
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
    
    
    // TODO: Resume not yet fully implemented - eventually call this from scenario thread
    void resumeScenarioPlayback()
    {
    		//startStopButton.setText("Start");
    		//speedFactorSpinner.setEnabled(true);
    		// Resuming live play will set our slider value to total scenario elapsed time
    		setPlayMode(TAPING);
    		firePropertyChange(ScenarioController.RESUME_LIVE_PLAY, null, null);
    }
    
    
    // Called by scenario thread when we've played back all
    // commands in the scenario model.  "Continuing" will append
    // buffered commands and continue playback
    void stopScenarioPlayback()
    {
		setPlayMode(PLAY_PAUSED);   
    		startStopButton.setText("Continue");
    		speedFactorSpinner.setEnabled(false);
     }
    
    
    void startStopButtonActionPerformed()
    {
    		if (playMode == PLAY_PAUSED)
    		{   			
    			startStopButton.setText("Stop");
    			speedFactorSpinner.setEnabled(false);
    			scenarioSecs = scenarioSlider.getValue();
    			setScenarioTime(scenarioSecs);
    			System.out.println("PLAY_PAUSED PLAYBACK scenarioSecs> " + scenarioSecs);
    			setPlayMode(PLAYING);   			
    			firePropertyChange(ScenarioController.PLAY_STARTED, null, scenarioSecs);
    		}
    		else
    		{
    			startStopButton.setText("Start");    			
    			speedFactorSpinner.setEnabled(true);
    			
    			setPlayMode(PLAY_PAUSED);
    			scenarioSecs = scenarioSlider.getValue();
    			System.out.println("PLAY_STOPPED scenarioSlider value> " + scenarioSlider.getValue());
    			firePropertyChange(ScenarioController.PLAY_STOPPED, null, scenarioSlider.getValue());  
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
		updateScenarioTime(scenarioSlider.getValue() + 1);
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
    
    
    private void initPlayer()
    {
        if (player != null)
            return;
        // Player runs continuously keeping track of elapsed scenario time
        // We only update the slider display if taping or playing.
        player = new Timer(1000, new ActionListener()
        {
            // Animate the view motion by controlling the positionSpinner and positionDelta
            public void actionPerformed(ActionEvent actionEvent)
            {
            		elapsedSecs++;
                     
            		if (playMode == TAPING)
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
        this.playMode = mode;
        if ((this.playMode == PLAYING || this.playMode == TAPING) 
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
