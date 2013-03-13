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

import wiiremotej.WiiRemote;
import wiiremotej.WiiRemoteJ;

/**
 * future repackaging of wii connection as a service
 * 
 * @author jstoner
 *
 */
public class WiiConnectionManager implements Runnable {

    WiiRemote wiiRemote=null;
    
    public static void makeManager(){
        WiiConnectionManager mgr = new WiiConnectionManager();
        Thread t= new Thread(mgr);
        t.start();
        final WiiRemote remoteF = mgr.getWiiRemote();
        Runtime.getRuntime().addShutdownHook(
                new Thread(
                        new Runnable(){
                            public void run(){
                                if(remoteF!=null){
                                        remoteF.disconnect();
                                }
                            }
                        }));
    }
    
    public void run() {
        try {
            wiiRemote=WiiRemoteJ.findRemote();
            wiiRemote.setAccelerometerEnabled(true);
            wiiRemote.setLEDIlluminated(0, true);
         } catch (Exception e) {
            e.printStackTrace();
            if (wiiRemote!=null){
                wiiRemote.disconnect();
            }
        }
        
        System.out.println("failure--null "  + (wiiRemote==null));
        if(wiiRemote!=null){
            System.out.println("failure--connected " + (wiiRemote.isConnected()));
            System.out.println("failure--ext connected " + (wiiRemote.isExtensionConnected()));
        }
    }
    
    public WiiRemote getWiiRemote() {
        return wiiRemote;
    }

}
