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

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.InputDevice;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;

import wiiremotej.WiiRemote;
import wiiremotej.WiiRemoteJ;

import boogiepants.model.InstrumentContainer;
import boogiepants.model.Stick;
import boogiepants.model.VisualInstrumentContainer;
import boogiepants.wiiInput.WiiInputDevice;
import boogiepants.wiiInput.WiiSensorBehavior;
import boogiepants.wiiInput.WiiSmoothingAdapter;

/**
 * This object manages the construction of the display objects for a single
 * dancer--the instruments and stick--and their connection with the Wii devices.
 * This is the base for future addition of multiple six axis devices.
 * 
 * @author jstoner
 * 
 */
public class DancerDisplay {

    private static DancerDisplay instance;
    private VisualInstrumentContainer visualContainer;
    private Stick stick;
    private WiiInputDevice wiiInput;
    private WiiSensorBehavior posBehavior;
    private WiiRemote wiiRemote;

    /**
     * @return instance
     */
    public static DancerDisplay getInstance() {
        if (instance == null) {
            instance = new DancerDisplay();
        }
        return instance;
    }

    /**
     * private constructor
     */
    private DancerDisplay() {

        visualContainer = new VisualInstrumentContainer();

        stick = new Stick();

        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0),
                100.0);
        InstrumentContainer instruments = visualContainer.getInstruments();
        wiiInput = WiiInputDevice.getInstance();
        posBehavior = new WiiSensorBehavior((TransformGroup) visualContainer
                .display(), (TransformGroup) stick.display(), wiiInput,
                instruments);
        posBehavior.setSchedulingBounds(bounds);
    }

    /**
     * establishes the connection with Wii remote
     */
    public void connect() {
//        Thread connectThread = new Thread(new Runnable() {
//            public void run() {
                ControlPanel panel = ControlPanel.getInstance();
                try {
                    System.setProperty("bluecove.jsr82.psm_minimum_off",
                                    "true");
                    System.setProperty("bluecove.stack", "widcomm");
                    // System.setProperty("bluecove.debug", "true");
                    wiiRemote = WiiRemoteJ.findRemote();
                    wiiRemote.setAccelerometerEnabled(true);
                    wiiRemote.setLEDIlluminated(1, true);
                    wiiInput.setChainedListener(new WiiSmoothingAdapter(12));
                    wiiInput.setRemote(wiiRemote);
                    wiiInput.initialize();
                    panel.handleConnectionSuccess();
                } catch (Exception e) {
                    e.printStackTrace();
                    panel.handleConnectionFailure();
                }

//            }
//        });
//        connectThread.start();

    }

    /**
     * @return
     */
    public VisualInstrumentContainer getVisualInstrumentContainer() {
        return visualContainer;
    }

    /**
     * @return
     */
    public Stick getStick() {
        return stick;
    }

    /**
     * @return
     */
    public WiiSensorBehavior getPosBehavior() {
        return posBehavior;
    }

    /**
     * @return
     */
    public InputDevice getWiiInput() {
        return wiiInput;
    }
    
    public void setEditMode(boolean mode){

        posBehavior.setStopCore(mode);
    }

}
