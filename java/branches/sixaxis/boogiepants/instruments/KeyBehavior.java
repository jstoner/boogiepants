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

import java.awt.AWTEvent;
import java.awt.event.KeyListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.awt.event.KeyEvent;

import javax.media.j3d.Behavior;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import javax.media.j3d.WakeupOnBehaviorPost;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.media.j3d.WakeupOr;

import boogiepants.display.KnowsSelected;
import boogiepants.util.Util;

public class KeyBehavior extends Behavior {

    private static int deleteKey;
    private WakeupCondition condition;
    private HashMap<Integer, KeyListener> map = new HashMap<Integer, KeyListener>();
    private InstrumentManager manager;
    
    public KeyBehavior(KnowsSelected knows) {
        manager= InstrumentManager.getInstance();
        if (Util.isMac()){
            deleteKey = KeyEvent.VK_BACK_SPACE;
        } else {
            deleteKey = KeyEvent.VK_DELETE;
        }
        map.put(deleteKey, new DeleteListener(knows));
    }

    @Override
    public void initialize() {
//        condition = new WakeupOnAWTEvent(AWTEvent.KEY_EVENT_MASK);
        condition = new WakeupOr(new WakeupCriterion[]{new WakeupOnElapsedFrames(0),
            new WakeupOnAWTEvent(AWTEvent.KEY_EVENT_MASK)});
        wakeupOn(condition);
        System.out.println("initialized");
    }

    @Override
    public void processStimulus(Enumeration criteria) {
        if (manager.isEditMode()){
            while(criteria.hasMoreElements()){
                WakeupCriterion w = (WakeupCriterion) criteria.nextElement();
                if (w instanceof WakeupOnAWTEvent){
                    KeyEvent kevt = (KeyEvent) ((WakeupOnAWTEvent)w).getAWTEvent()[0];
                    int kevtcode = kevt.getKeyCode();
                    int kevttype = kevt.getID();
                    System.out.println("key event code "+ kevtcode + " == delete "+ deleteKey+"?");
                    if (kevttype == KeyEvent.KEY_PRESSED && map.containsKey(kevtcode)){
                        map.get(kevtcode).keyPressed(kevt);
                        kevt.consume();
                    }
                }
            }
        }
        wakeupOn(condition);
    }

}
