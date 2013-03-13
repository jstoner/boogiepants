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

import wiiremotej.*;
import wiiremotej.event.*;

/**
 * This adapter smoothes the Wii remote accelerometer output--the raw data is
 * quite jittery. It takes a number num at instantiation and keeps a running
 * window of num samples, and returns the average accelerometer values over that
 * window.
 * 
 * There is a tradeoff here: the fewer samples we average, the more jitter we
 * see. But the more samples we use, the longer the interval of time we are
 * averaging, so the longer the delay. The right value strikes a balance between
 * these two considerations.
 * 
 * @author jstoner
 * 
 */
public class WiiSmoothingAdapter extends WiiRemoteAdapter implements WiiChainedListener {
    private SmoothingData wii, nun;
    private WiiRemote remote;
    private WRNunchukExtensionEvent lastevent= WRNunchukExtensionEvent.createBlankEvent();
    private WiiRemoteListener listener;

    /**
     * @see boogiepants.wiiInput.WiiChainedListener#setListener(wiiremotej.event.WiiRemoteListener)
     */
    public void setListener(WiiRemoteListener listener){
    	this.listener=listener;
    }
    
    /**
     * @see boogiepants.wiiInput.WiiChainedListener#setRemote(wiiremotej.WiiRemote)
     */
    public void setRemote(WiiRemote remote){
    	this.remote=remote;
    }
    
    /**
     * computes average x, y, z  positions for recent events
     * @author jstoner
     *
     */
    private class SmoothingData{
        double x[], y[], z[];
        private int curInd = 0;
        private double avgx, avgy, avgz;
        
        /**
         * @param size
         */
        public SmoothingData(int size){
            x=new double[size];
            y=new double[size];
            z=new double[size];            
        }

        /**
         * computes average over num most recent samples
         * leaves results in instance variables avgx, avgy, avgz
         * @param evt
         */
        public void averageRecentEvents(WRAccelerationEvent evt) {
            x[curInd]=evt.getXAcceleration();
            y[curInd]=evt.getYAcceleration();
            z[curInd]=evt.getZAcceleration();
            curInd= (curInd+1) % x.length;

            avgx = avgy = avgz = 0;
            for (int i=0; i<x.length; i++){
                avgx += x[i];
                avgy += y[i];
                avgz += z[i];
            } 
            avgx /= x.length;
            avgy /= y.length;
            avgz /= z.length;
        }
    }
    
    /**
     * 
     * @param num defines number of recent samples to use
     */
    public  WiiSmoothingAdapter(int num){
        wii= new SmoothingData(num);
        nun= new SmoothingData(num);
    }
    
    /**
     * @see wiiremotej.event.WiiRemoteAdapter#accelerationInputReceived(wiiremotej.event.WRAccelerationEvent)
     */
    public void accelerationInputReceived(WRAccelerationEvent evt) {
        wii.averageRecentEvents(evt);
        
        listener.accelerationInputReceived(new WRAccelerationEvent(remote, wii.avgx, wii.avgy, wii.avgz));
    }

    /**
     * @see wiiremotej.event.WiiRemoteAdapter#extensionInputReceived(wiiremotej.event.WRExtensionEvent)
     */
    public void extensionInputReceived(WRExtensionEvent evt) {
        if (evt instanceof WRNunchukExtensionEvent)
        {
            WRNunchukExtensionEvent NEvt = (WRNunchukExtensionEvent)evt;
            WRAccelerationEvent AEvt = NEvt.getAcceleration();
            nun.averageRecentEvents(AEvt);
            
            WRAccelerationEvent newaccevt=new WRAccelerationEvent(remote, nun.avgx, nun.avgy, nun.avgz);
            int buttonMask =
                NEvt.isPressed(WRNunchukExtensionEvent.C) ? WRNunchukExtensionEvent.C : 0;
            buttonMask &= 
                NEvt.isPressed(WRNunchukExtensionEvent.Z) ? WRNunchukExtensionEvent.Z : 0;
            WRNunchukExtensionEvent newextevt = 
                new WRNunchukExtensionEvent(remote, newaccevt, buttonMask, 
                                            NEvt.getAnalogStickData(), lastevent);
            listener.extensionInputReceived(newextevt);
            lastevent=newextevt;
        } else {
            listener.extensionInputReceived(evt);
       }
    }     

    /**
     * @see wiiremotej.event.WiiRemoteAdapter#disconnected()
     */
    public void disconnected(){
        listener.disconnected();
    }
    
    /**
     * @see wiiremotej.event.WiiRemoteAdapter#statusReported(wiiremotej.event.WRStatusEvent)
     */
    public void statusReported(WRStatusEvent evt){
        listener.statusReported(evt);
    }

    /**
     * @see wiiremotej.event.WiiRemoteAdapter#buttonInputReceived(wiiremotej.event.WRButtonEvent)
     */
    public void buttonInputReceived(WRButtonEvent evt){
        listener.buttonInputReceived(evt);
    }

    /**
     * @see wiiremotej.event.WiiRemoteAdapter#combinedInputReceived(wiiremotej.event.WRCombinedEvent)
     */
    public  void combinedInputReceived(WRCombinedEvent evt) {
        listener.combinedInputReceived(evt);
    }

    /**
     * @see wiiremotej.event.WiiRemoteAdapter#extensionConnected(wiiremotej.WiiRemoteExtension)
     */
    public  void extensionConnected(WiiRemoteExtension extension){
        listener.extensionConnected(extension);
    }

    /**
     * @see wiiremotej.event.WiiRemoteAdapter#extensionDisconnected(wiiremotej.WiiRemoteExtension)
     */
    public  void extensionDisconnected(WiiRemoteExtension extension){
        listener.extensionDisconnected(extension);
    }

    /**
     * @see wiiremotej.event.WiiRemoteAdapter#extensionPartiallyInserted()
     */
    public  void extensionPartiallyInserted() {
        listener.extensionPartiallyInserted();
    }

    /**
     * @see wiiremotej.event.WiiRemoteAdapter#extensionUnknown()
     */
    public  void extensionUnknown() {
        listener.extensionUnknown();
    }

    /**
     * @see wiiremotej.event.WiiRemoteAdapter#IRInputReceived(wiiremotej.event.WRIREvent)
     */
    public  void IRInputReceived(WRIREvent evt){
        listener. IRInputReceived(evt);
    }
}
