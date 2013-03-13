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

package boogiepants.wiiInput;

import javax.media.j3d.*;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import boogiepants.instruments.InstrumentManager;
import boogiepants.model.InstrumentContainer;

import java.util.*;

/**
 * 
 * This transfers the data from the sensor objects to the actual display objects. 
 * Animates the stick and core motion.
 * 
 * @author jstoner
 *
 */
public class WiiSensorBehavior extends Behavior {

    private WakeupOnElapsedFrames conditions = new WakeupOnElapsedFrames(0);
    private TransformGroup transformGroup1;
    private TransformGroup transformGroup2;
    private Sensor coreSensor;
    private Sensor pelvicSensor;
    private Transform3D identity = new Transform3D(new Matrix3d(1,0,0,
                                                                  0,1,0,
                                                                  0,0,1), new Vector3d(0,0,0), 1d);
    private int[] buttons;
    private int[] prevButtons;
    private InstrumentContainer instruments;
    private Transform3D coreTransform = new Transform3D();
    private Transform3D pelvicTransform = new Transform3D();

    /**
     * @param tg1
     * @param tg2
     * @param wiiInput
     * @param ic
     */
    public WiiSensorBehavior( TransformGroup tg1, TransformGroup tg2, 
            WiiInputDevice wiiInput, InstrumentContainer ic) {
        transformGroup1 = tg1;
        transformGroup2 = tg2;
        this.coreSensor = wiiInput.getSensor(0);
        this.pelvicSensor = wiiInput.getSensor(1);
        buttons=new int[WiiInputDevice.WII_REMOTE_NUM_BUTTONS];
        prevButtons=new int[WiiInputDevice.WII_REMOTE_NUM_BUTTONS];
        this.instruments=ic;
    }

    /**
     * Allows change to new instrument container when opening/creating new
     * .pants file
     * 
     * @param ic
     */
    public void resetInstruments(){
        InstrumentManager instrumentmgr = InstrumentManager.getInstance();
        InstrumentContainer instruments = instrumentmgr
                .getInstrumentContainer();
        
        this.instruments=instruments;
    }
    
    /**
     * @see javax.media.j3d.Behavior#initialize()
     */
    public void initialize() {
    	wakeupOn( conditions );
    }

    /**
     * does the actual transfer--the transforms, and toggling the buttons on release
     * 
     * @see javax.media.j3d.Behavior#processStimulus(java.util.Enumeration)
     */
    public void processStimulus( Enumeration criteria ) {
        coreSensor.getRead( coreTransform );
        coreSensor.lastButtons(buttons);
        pelvicSensor.getRead( pelvicTransform );
        for (int i=0; i<buttons.length; i++){
            if (buttons[i]==0 && prevButtons[i]==1){
                instruments.toggle(i);
                break;
            }
        }
        System.arraycopy(buttons, 0, prevButtons, 0, buttons.length);
		try{
            transformGroup1.setTransform( coreTransform );
            transformGroup2.setTransform( pelvicTransform );
		}
		catch(BadTransformException e){
		    System.out.println("bad transform!");
            transformGroup1.setTransform( identity );           
            transformGroup2.setTransform( identity );           
		}
		wakeupOn( conditions );
    }

}
