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

import java.io.IOException;

import javax.media.j3d.InputDevice;
import javax.media.j3d.Sensor;
import javax.media.j3d.SensorRead;
import javax.media.j3d.Transform3D;

import wiiremotej.ButtonMap;
import wiiremotej.NunchukExtension;
import wiiremotej.WiiRemote;
import wiiremotej.WiiRemoteExtension;
import wiiremotej.WiiRemoteJ;
import wiiremotej.event.WRAccelerationEvent;
import wiiremotej.event.WRButtonEvent;
import wiiremotej.event.WRExtensionEvent;
import wiiremotej.event.WRNunchukExtensionEvent;
import wiiremotej.event.WiiRemoteAdapter;

/**
 * This is the object that bridges from the WiiRemote interface to the Java3D
 * api. It connects with a Wiimote, and translates its output into Transform3D
 * objects and button push values. It also applies centering and gain. It's a
 * boogiepants application object--it's not a pure wiimote/java3d interface.
 * 
 * Centering: establishes the neutral position of a dancer's body, using accelerometer
 * output. 
 * Gain: allows a dancer to compensate for a lack of pelvic flexibility. Multiplies
 * variation from the center by an amount set by the user.
 * 
 * @author jstoner
 * 
 */
public class WiiInputDevice extends WiiRemoteAdapter implements InputDevice {

    public final static int WII_REMOTE_NUM_BUTTONS = 11;

    public final static int BUTTON_INDEX_A = 0;
    //public final static int BUTTON_INDEX_B = 1;
    public final static int BUTTON_INDEX_UP = 2;
    public final static int BUTTON_INDEX_DOWN = 3;
    public final static int BUTTON_INDEX_LEFT = 4;
    public final static int BUTTON_INDEX_RIGHT = 5;
    public final static int BUTTON_INDEX_MINUS = 6;
    public final static int BUTTON_INDEX_PLUS = 7;
    public final static int BUTTON_INDEX_HOME = 8;
    public final static int BUTTON_INDEX_ONE = 9;
    public final static int BUTTON_INDEX_TWO = 10;
    
    public final static String[] BUTTON_SYMBOLS = {"[a]", "[b]", "[up]", "[down]", "[left]", "[right]",
        "[-]", "[+]", "[home]", "[1]", "[2]"};

    public static final int PELVIS_SENSOR_INDEX = 1;
    public static final int CORE_SENSOR_INDEX = 0; 

    private double gain = 1.0d;
    private WiiRemote remote;
    private WiiChainedListener chainedListener;

    Sensor sensors[] = new Sensor[2];
    private SensorRead sensorReadPelvis = new SensorRead();
    private SensorRead sensorReadCore = new SensorRead(WII_REMOTE_NUM_BUTTONS);
    private int[] buttons = new int[WII_REMOTE_NUM_BUTTONS];
    private PitchRoll wii, nunchuk;

    private boolean nunchukPresent;

    private static WiiInputDevice instance;

    /**
     * @return
     */
    public boolean isNunchukPresent() {
        return nunchukPresent;
    }


    /**
     * Inner class represents wii, nunchuk. Encapsulates duplicated functions
     * @author jstoner
     *
     */
    private class PitchRoll {

        private Transform3D pitchTrans = new Transform3D();
        private Transform3D rollTrans = new Transform3D();
        private double pitch, roll, centerPitch, centerRoll;
        private int zindex;
        private int yindex;
        private int xindex;
        private double[] dim;
        private double[] invert;

        public PitchRoll(int xindex, int yindex, int zindex, int xInvert, int yInvert, int zInvert) {
            this.xindex= xindex; this.yindex=yindex; this.zindex=zindex;
            dim = new double[3];
            invert = new double[3];
            invert[xindex] = xInvert; invert[yindex] = yInvert; invert[zindex] = zInvert;
        }

        /**
         * compute pitch and roll data for future use
         * 
         * @param evt
         */
        public void setPitchRoll(WRAccelerationEvent evt) {
            synchronized (WiiInputDevice.this) {
                dim[xindex] = evt.getXAcceleration() * invert[xindex];
                dim[yindex] = evt.getYAcceleration() * invert[yindex];
                dim[zindex] = evt.getZAcceleration() * invert[zindex];
            }

            pitch = Math.atan2(dim[1], dim[2]) - Math.PI / 2;
            roll = Math.PI / 2 - Math.atan2(dim[1], dim[0]);

        }

        /**
         * use pitch and roll data to compute transforms for stick
         *  
         * @return
         */
        public Transform3D getTransform() {
            rollTrans.rotZ((roll - centerRoll) * gain);
            pitchTrans.rotX((pitch - centerPitch) * gain);
            pitchTrans.mul(rollTrans);

            return (pitchTrans);
        }
    }

    /**
     * @return
     */
    public static WiiInputDevice getInstance(){
        if (instance == null){
            instance = new WiiInputDevice(new String[1]);
        }
        return instance;
    }
    /**
     * Sets up sensors for Java3d api to receive data from wii remote and nunchuk
     * 
     * @param args
     */
    private WiiInputDevice(String[] args) {
        sensors[0] = new Sensor(this, 2, WII_REMOTE_NUM_BUTTONS);
        sensors[0].setSensorReadCount(2);
        sensors[1] = new Sensor(this, 2);
        sensors[1].setSensorReadCount(2);
        wii = new PitchRoll(1, 0, 2, -1, 1, 1);
        nunchuk = new PitchRoll(0, 1, 2, 1, 1, 1);
    }

    /**
     * 
     * @see javax.media.j3d.InputDevice#close()
     */
    public void close() {
    }

    /**
     * 
     * @see javax.media.j3d.InputDevice#getProcessingMode()
     */
    public int getProcessingMode() {
        return InputDevice.DEMAND_DRIVEN;
    }

    /**
     * 
     * @see javax.media.j3d.InputDevice#getSensor(int)
     */
    public Sensor getSensor(int sensorIndex) {
        return sensors[sensorIndex];
    }

    /**
     * 
     * @see javax.media.j3d.InputDevice#getSensorCount()
     */
    public int getSensorCount() {
        return sensors.length;
    }

    /**
     * place to add listener between remote and this object
     * 
     * @param listener
     */
    public void setChainedListener(WiiChainedListener listener) {
        this.chainedListener = listener;
    }
    
    /**
     * @param remote
     */
    public void setRemote(WiiRemote remote){
        this.remote=remote;
    }

    /**
     * gets remote object, associate it with chained listener, this object
     * 
     * @see javax.media.j3d.InputDevice#initialize()
     */
    public boolean initialize(){
        // Find and connect to a Wii Remote
        WiiRemoteJ.setConsoleLoggingAll();

        if (chainedListener != null) {
            remote.addWiiRemoteListener(chainedListener);
            chainedListener.setListener(this);
            chainedListener.setRemote(remote);
        } else if (remote !=null){
            remote.addWiiRemoteListener(this);
        } else return false;
        
        remote.getButtonMaps().add(
                new ButtonMap(WRButtonEvent.HOME, ButtonMap.NUNCHUK,
                        WRNunchukExtensionEvent.C,
                        new int[] { java.awt.event.KeyEvent.VK_CONTROL },
                        java.awt.event.InputEvent.BUTTON1_MASK, 0, -1));
        return true;
    }

    /**
     * @see wiiremotej.event.WiiRemoteAdapter#accelerationInputReceived(wiiremotej.event.WRAccelerationEvent)
     */
    public void accelerationInputReceived(WRAccelerationEvent evt) {
        wii.setPitchRoll(evt);
    }

    /**
     * @see wiiremotej.event.WiiRemoteAdapter#extensionInputReceived(wiiremotej.event.WRExtensionEvent)
     */
    public void extensionInputReceived(WRExtensionEvent evt) {
        if (evt instanceof WRNunchukExtensionEvent) {
            WRNunchukExtensionEvent NEvt = (WRNunchukExtensionEvent) evt;
            WRAccelerationEvent AEvt = NEvt.getAcceleration();
            nunchuk.setPitchRoll(AEvt);
        }
    }

    /**
     * maps wii button input to array positions
     * 
     * @see wiiremotej.event.WiiRemoteAdapter#buttonInputReceived(wiiremotej.event.WRButtonEvent)
     */
    public void buttonInputReceived(WRButtonEvent bevt) {
        synchronized (this) {
            buttons[BUTTON_INDEX_A] = bevt.isPressed(WRButtonEvent.A) ? 1 : 0;
            //buttons[BUTTON_INDEX_B] = bevt.isPressed(WRButtonEvent.B) ? 1 : 0;
            buttons[BUTTON_INDEX_UP] = bevt.isPressed(WRButtonEvent.UP) ? 1 : 0;
            buttons[BUTTON_INDEX_DOWN] = bevt.isPressed(WRButtonEvent.DOWN) ? 1 : 0;
            buttons[BUTTON_INDEX_LEFT] = bevt.isPressed(WRButtonEvent.LEFT) ? 1 : 0;
            buttons[BUTTON_INDEX_RIGHT] = bevt.isPressed(WRButtonEvent.RIGHT) ? 1 : 0;
            buttons[BUTTON_INDEX_MINUS] = bevt.isPressed(WRButtonEvent.MINUS) ? 1 : 0;
            buttons[BUTTON_INDEX_PLUS] = bevt.isPressed(WRButtonEvent.PLUS) ? 1 : 0;
            buttons[BUTTON_INDEX_HOME] = bevt.isPressed(WRButtonEvent.HOME) ? 1 : 0;
            buttons[BUTTON_INDEX_ONE] = bevt.isPressed(WRButtonEvent.ONE) ? 1 : 0;
            buttons[BUTTON_INDEX_TWO] = bevt.isPressed(WRButtonEvent.TWO) ? 1 : 0;
        }
    }

    /**
     * @see wiiremotej.event.WiiRemoteAdapter#extensionConnected(wiiremotej.WiiRemoteExtension)
     */
    public void extensionConnected(WiiRemoteExtension extension) {
        try {
            if (extension instanceof NunchukExtension) {
                remote.setExtensionEnabled(true);
                nunchukPresent = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * moves transforms, button data to position where WiiSensorBehahavior
     * can use them
     * 
     * @see javax.media.j3d.InputDevice#pollAndProcessInput()
     */

    public void pollAndProcessInput() {
        synchronized (this) {
            if (nunchukPresent) {
                sensorReadPelvis.set(nunchuk.getTransform());
                sensors[PELVIS_SENSOR_INDEX].setNextSensorRead(sensorReadPelvis);
                sensorReadCore.setButtons(buttons);
                sensorReadCore.set(wii.getTransform());
                sensors[CORE_SENSOR_INDEX].setNextSensorRead(sensorReadCore);
            }
        }
    }

    /**
     * @see javax.media.j3d.InputDevice#processStreamInput()
     */
    public void processStreamInput() {

    }

    /**
     * @see javax.media.j3d.InputDevice#setNominalPositionAndOrientation()
     */
    public void setNominalPositionAndOrientation() {

    }

    /**
     * @see javax.media.j3d.InputDevice#setProcessingMode(int)
     */
    public void setProcessingMode(int mode)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("can't change processing mode");

    }

    /**
     * Gain: allows a dancer to compensate for a lack of pelvic flexibility. Multiplies
     * variation from the center by an amount set by the user.
     * @param gain
     */
    public void setGain(double gain) {
        this.gain = gain;
    }

    /**
     * @return
     */
    public double getGain() {
        return gain;
    }

    /**
     * sets center roll, center pitch values to whatever roll, pitch are right now for both
     * wii and nunchuk
     * <P>Centering: establishes the neutral position of a dancer's body, using accelerometer
     * output.</P> 
     */
    public void setCenter() {
        if (nunchukPresent) {
            nunchuk.centerRoll = nunchuk.roll;
            nunchuk.centerPitch = nunchuk.pitch;
        }
        wii.centerRoll = wii.roll;
        wii.centerPitch = wii.pitch;
    }

}
