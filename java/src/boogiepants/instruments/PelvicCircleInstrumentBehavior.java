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

package boogiepants.instruments;

import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.illposed.osc.OSCMessage;

import boogiepants.wiiInput.WiiInputDevice;

/**
 * This defines the behavior for the pelvic circle instrument. It computes the radial angle of
 * the pelvis in the core's frame of reference. It does two things with that value:
 * 
 * <li>uses it to rotate the display of the ring for the instrument and </li>
 * <li>forwards it to OSC (actually forwards an alpha of the value to OSC)</li>
 * 
 * @author jstoner
 *
 */
public class PelvicCircleInstrumentBehavior extends Behavior implements ToggleEventListener {

    private WakeupOnElapsedFrames condition;
    private InstrumentManager manager;
    private WiiInputDevice wii;
    private Vector3d vecScratch;
    private Transform3D pelvTrans;
    private Transform3D coreTrans;
    private Vector3d vec;
    private String oscToggleDest;
    private String oscScaleDest;
    private OSCConnection oscConnection;
    private Object oscargs[] = new Object[1];
    private TransformGroup ringTransform;
    private Transform3D ringTrans;

    /**
     * creates scratch objects for faster execution, less garbage collection
     * 
     */
    public PelvicCircleInstrumentBehavior(String oscDest, TransformGroup ringTransform) {
        vec = new Vector3d(0, -1, 0);
        vecScratch = new Vector3d();
        pelvTrans = new Transform3D();
        coreTrans = new Transform3D();
        manager= InstrumentManager.getInstance();
        wii = WiiInputDevice.getInstance();
        
        ringTrans = new Transform3D();
        this.ringTransform = ringTransform;
        oscConnection = OSCConnection.getConnection();
        oscScaleDest = oscDest + "/scale";
        oscToggleDest = oscDest + "/toggle";
    }

    /**
     * @see javax.media.j3d.Behavior#initialize()
     */
    @Override
    public void initialize() {
        condition = new WakeupOnElapsedFrames(0);
        wakeupOn(condition);
    }

    /**
     * <ol>
     * <li>computes radial angle of pelvis with respect to core.</li>
     * <li>Transforms ring display.</li>
     * <li>Computes alpha value, sends to OSC.</li>
     * 
     * @see javax.media.j3d.Behavior#processStimulus(java.util.Enumeration)
     */
    @Override
    public void processStimulus(Enumeration arg0) {
        if (!manager.isEditMode()){
            wii.getSensor(WiiInputDevice.PELVIS_SENSOR_INDEX).getRead(pelvTrans);
            wii.getSensor(WiiInputDevice.CORE_SENSOR_INDEX).getRead(coreTrans);
            coreTrans.invert();
            pelvTrans.mul(coreTrans);
            pelvTrans.transform(vec, vecScratch);
            double angle = Math.atan2(vecScratch.z, vecScratch.x);
            ringTrans.rotY(angle);
            ringTrans.invert();
            ringTransform.setTransform(ringTrans);
            float angleSignal = (float) ((Math.PI + angle) / (2 * Math.PI));
            messageToOSC(oscScaleDest, angleSignal);
        }
        wakeupOn(condition);
    }

    /**
     * @see boogiepants.instruments.ToggleEventListener#toggleEvent(boolean)
     */
    public void toggleEvent(boolean value) {
        messageToOSC(oscToggleDest, value ? 1f : 0f);
    }

    /**
     * sends an actual numerical message to one of this instrument's addresses
     * 
     * @param oscaddr
     * @param signal
     */
    private void messageToOSC(String oscaddr, float signal) {
        oscargs[0]= new Float(signal);
        OSCMessage message = new OSCMessage(oscaddr, oscargs);
        oscConnection.send(message);
    }

}
