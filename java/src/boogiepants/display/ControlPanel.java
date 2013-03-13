/*
 *
 * Copyright (c) 2008-2009, John Stoner
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list 
 * of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this 
 * list of conditions and the following disclaimer in the documentation and/or 
 * other materials provided with the distribution.
 * Neither the name of the John Stoner nor the names of its contributors may be 
 * used to endorse or promote products derived from this software without specific 
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 * 
 * John Stoner is reachable at johnstoner2 [[at]] gmail [[dot]] com.
 * His current physical address is
 * 
 * 2358 S Marshall 
 * Chicago, IL 60623
 */

package boogiepants.display;

import java.awt.AWTKeyStroke;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import wiiremotej.WiiRemote;
import wiiremotej.WiiRemoteJ;

import boogiepants.instruments.OSCConnection;
import boogiepants.util.Util;
import boogiepants.wiiInput.WiiInputDevice;
import boogiepants.wiiInput.WiiSmoothingAdapter;

/**
 * The Control Panel object creates the control panel for the boogiepants 
 * application.
 * 
 * @author jstoner
 *
 */
public class ControlPanel extends JFrame {


    private WiiInputDevice wii;
    private WiiRemote wiiRemote;
    private GridBagConstraints wiiSetLocation;
    private JButton wiiSetButton;
    private ArrayList<JComponent> greyedItems = new ArrayList<JComponent>();
    private JLabel message;
    private static ControlPanel window;

    /**
     * Launch the panel for the application under a separate thread
     * @return 
     * 
     */
    public static void makeWindow() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    window = new ControlPanel();
                    window.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * This may seem like a weird way to do a Singleton. Let me explain:
     * We always create the ControlPanel with makeWindow(). It does so in a 
     * separate thread. There are other objects that need a handle on the
     * ControlPanel object. If they call before it's instantiated, it really
     * should blow up with an NPE. But getting that handle is an explicitly
     * separate operation than creating the object. 
     * 
     * @return
     */
    public static ControlPanel getInstance(){
        return window;
    }

    /**
     * constructor initializes the contents of the frame:
     * <ul>
     * <li> uses a GridBagLayout;<li>
     * <li>a slider to control gain on stick and core motion</li>
     * <li>a button to get the wiimote connection;<li>
     * <li>a button to establish the center for the stick and core;</li> 
     * <li>two text entry boxes to allow boogiepants to send OSC
     * signals anywhere on the network</li>
     * </ul>
     */
    private ControlPanel() {
        setTitle("boogiepants control");
        final GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0,0,7};
        gridBagLayout.rowHeights = new int[] {0,7,7,0,7};
        getContentPane().setLayout(gridBagLayout);
        if (Util.isWindows()){
            setBounds(10, 10, 275, 275);
        } else if (Util.isMac()){
            setBounds(10, 10, 385, 225);
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Set<AWTKeyStroke> forwardKeys = getFocusTraversalKeys(
                KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        Set<AWTKeyStroke> newForwardKeys = new HashSet<AWTKeyStroke>(forwardKeys);
        newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            newForwardKeys);
        
        final JSlider slider = new JSlider();
        greyedItems.add(slider);
        slider.setOrientation(SwingConstants.VERTICAL);
        if (wii!=null){
            slider.setValue((int) ((wii.getGain()-1) *100));
        }else{
            slider.setValue(0);
        }
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                if (!source.getValueIsAdjusting() && wii!=null) {
                    double gain = source.getValue() / 100d + 1d;
                    wii.setGain(gain);
                }
            }
        });
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridx = 0;
        getContentPane().add(slider, gridBagConstraints);

        wiiSetButton= new JButton();
        wiiSetButton.setText("connect to Wii Remote");
        wiiSetButton.addActionListener((ActionListener) new WiiConnectActionListener());

        wiiSetLocation = new GridBagConstraints();
        wiiSetLocation.gridwidth = 2;
        wiiSetLocation.gridx=1;
        wiiSetLocation.gridy=0;
        getContentPane().add(wiiSetButton, wiiSetLocation);

        final JButton button = new JButton();
        greyedItems.add(button);
        button.setText("center boogiepants position");
        final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
        gridBagConstraints_1.insets = new Insets(0, 0, 5, 0);
        gridBagConstraints_1.gridwidth = 2;
        gridBagConstraints_1.gridy = 1;
        gridBagConstraints_1.gridx = 1;
        getContentPane().add(button, gridBagConstraints_1);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                if (wii!=null){
                    wii.setCenter();
                    message.setText("Wii Remote recentered");
                }
            }
        });

        final JLabel label_2 = new JLabel();
        greyedItems.add(label_2);
        label_2.setText("OSC endpoint");
        final GridBagConstraints gridBagConstraints_6 = new GridBagConstraints();
        gridBagConstraints_6.insets = new Insets(0, 0, 0, 10);
        gridBagConstraints_6.gridy = 2;
        gridBagConstraints_6.gridx = 1;
        getContentPane().add(label_2, gridBagConstraints_6);

        final JLabel label_1 = new JLabel();
        greyedItems.add(label_1);
        label_1.setText("OSC port");
        final GridBagConstraints gridBagConstraints_5 = new GridBagConstraints();
        gridBagConstraints_5.ipady = 5;
        gridBagConstraints_5.gridy = 2;
        gridBagConstraints_5.gridx = 2;
        getContentPane().add(label_1, gridBagConstraints_5);

        final OSCConnection oscConnection = OSCConnection.getConnection();
        final JTextField oscEndpoint = new JTextField(20);
        greyedItems.add(oscEndpoint);
        oscEndpoint.setText(oscConnection.getOscEndpoint());
        final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
        gridBagConstraints_2.anchor = GridBagConstraints.NORTH;
        gridBagConstraints_2.gridy = 3;
        gridBagConstraints_2.gridx = 1;
        getContentPane().add(oscEndpoint, gridBagConstraints_2);

        final JTextField oscEndpointPort = new JTextField(5);
        greyedItems.add(oscEndpointPort);
        String port = Integer.toString(oscConnection.getOscEndpointPort());
        oscEndpointPort.setText(port);
        oscEndpointPort.setMinimumSize(new Dimension(100, 20));
        oscEndpointPort.addFocusListener(new FocusListener(){
            public void focusGained(FocusEvent e) {               
            }

            public void focusLost(FocusEvent e) {
                oscConnection.setOscEndpoint(oscEndpoint.getText());
                try {
                    int port = Integer.parseInt(oscEndpointPort.getText());
                    oscConnection.setOscEndpointPort(port);
                    message.setText("OSC endpoint changed");
                } catch (NumberFormatException e1) {
                    e1.printStackTrace();
                }
                
                 
            }
        });
        final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
        gridBagConstraints_3.anchor = GridBagConstraints.NORTH;
        gridBagConstraints_3.gridy = 3;
        gridBagConstraints_3.gridx = 2;
        getContentPane().add(oscEndpointPort, gridBagConstraints_3);


        message = new JLabel();
        message.setText("<html><center>click above, and then push the 1 and 2<br>" +
        		        "buttons on your Wii Remote simultaneously</center></html>");
        final GridBagConstraints gridBagConstraints_7 = new GridBagConstraints();
        gridBagConstraints_7.insets = new Insets(0, 0, 45, 0);
        gridBagConstraints_7.gridwidth = 2;
        gridBagConstraints_7.gridy = 4;
        gridBagConstraints_7.gridx = 1;
        getContentPane().add(message, gridBagConstraints_7);
        enableItems(false);
        
        setJMenuBar(new MenuBar());
    }


    /**
     * greys out/ungreys items that can't be used because there's no wiimote connection
     * Adds explanatory tooltip
     * 
     * @param enable
     */
    private void enableItems(boolean enable) {
        for (JComponent i: greyedItems){
            i.setEnabled(enable);
            String text = !enable ? "connect to Wii Remote before using" : null;
            i.setToolTipText(text);
        }
    }

    /**
     * Operations to perform when we get a wii remote connection
     * <ul><li>enable the other items on the control panel;</li>
     * <li>set up the WiiInputDevice that connects the wiimote w/java 3d</li>
     * <li>get the main display window for boogiepants</li>
     * <li>change text on button to indicate connected status</li>
     * </ul>
     */
    protected void handleConnectionSuccess() {
        enableItems(true);
        wii= WiiInputDevice.getInstance();
        wii.setChainedListener(new WiiSmoothingAdapter(12));
        wii.setRemote(wiiRemote);
        wii.initialize();
        BoogiepantsDisplayWindow.makeWindow(wii);        
        wiiSetButton.setText("Connected!");
    }

    /**
     * Private class handles connect button event. Tries to find remote, 
     * if successful sets up connection status and calls 
     * handleConnectionSuccess(). If not, re-enables button and changes
     * messages. Initiates this process in a separate thread.
     * 
     * @author jstoner
     *
     */
    private class WiiConnectActionListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            wiiSetButton.setEnabled(false);
            wiiSetButton.setText("Connecting...");
           
            Thread connectThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        System.setProperty("bluecove.jsr82.psm_minimum_off", "true");
                        System.setProperty("bluecove.stack", "widcomm");
                        //System.setProperty("bluecove.debug", "true");
                        wiiRemote = WiiRemoteJ.findRemote();
                        wiiRemote.setAccelerometerEnabled(true);
                        wiiRemote.setLEDIlluminated(1, true);
                        handleConnectionSuccess();
                        message.setText("Wii Remote connected");
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        wiiSetButton.setText("Try again");
                        wiiSetButton.setEnabled(true);
                        message.setText("<html><center>It may take a few tries to get it to connect.<br>" +
                               		    "Don't give up!</center></html>");
                    }
                   
                }
            });
            connectThread.start();
        }
    }
    
    /**
     * called on exit
     * 
     * @param evt
     */
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        System.exit(0);
    }
    

    /**
     * @param editMode
     */
    public void editDisplay(boolean editMode) {
        ((MenuBar)this.getJMenuBar()).editDisplay(editMode);        
    }
    
}
