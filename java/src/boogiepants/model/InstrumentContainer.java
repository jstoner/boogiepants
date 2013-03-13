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

package boogiepants.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Node;
import javax.media.j3d.Switch;
import javax.vecmath.Point3d;

import boogiepants.display.BoogiepantsDisplayWindow;
import boogiepants.instruments.InstrumentManager;
import boogiepants.instruments.SelectBehavior;
import boogiepants.instruments.StrikeInstrumentBehavior;
import boogiepants.instruments.ToggleEventListener;
import boogiepants.wiiInput.WiiInputDevice;

/**
 * This class contains all the instrument groups for a .pants file. It directs
 * the translation of the model instruments into 3d objects, and it mediates
 * toggling instrument groups. It is serialized into the .pants file.
 * 
 * @author jstoner
 * 
 */
public class InstrumentContainer implements Serializable, Displayable {
    
    static final long serialVersionUID = -2025879990031329410L;
    
    ArrayList<ArrayList<Displayable>> instrumentGroups = new ArrayList<ArrayList<Displayable>>();
    private transient Switch instrumentSwitch;
    private transient int lastPushed;
    private transient boolean updated;
    private transient BranchGroup group;

    /**
     * creates empty container
     */
    public InstrumentContainer() {
        for (int i = 0; i < WiiInputDevice.WII_REMOTE_NUM_BUTTONS; i++) {
            instrumentGroups.add(new ArrayList<Displayable>());
        }
    }

    /**
     * adds an instrument to a group. That group is indexed by the wiiButtonIndex
     * 
     * @param wiiButtonIndex
     * @param instrument
     */
    public void add(int wiiButtonIndex, Displayable instrument) {
        instrumentGroups.get(wiiButtonIndex).add(instrument);
    }

    /**
     * Lets other objects turn on and off instrument sets in the switch.
     * Only show one instrument set at a time. Initiates ToggleEvents to
     * instruments that need them
     * 
     * @param button
     */
    public void toggle(int button) {
        BitSet mask = instrumentSwitch.getChildMask();
        if(InstrumentManager.getInstance().isEditMode()){
            mask.clear();
            BoogiepantsDisplayWindow.getInstance().changeInstrumentSetEditDisplay(button);
        }else {
            int oldbutton = instrumentSwitch.getWhichChild();

            if(oldbutton >= 0){
                for (Displayable i : instrumentGroups.get(oldbutton)) {
                    if (i instanceof ToggleEventListener) {
                        ((ToggleEventListener) i).toggleEvent(false);
                    }
                }
            }
            
            for (Displayable i : instrumentGroups.get(button)) {
                if (i instanceof ToggleEventListener) {
                    ((ToggleEventListener) i).toggleEvent(true);
                }
            }

        }

        lastPushed = button;
        instrumentSwitch.setWhichChild(button);
    }

    /** 
     * coordinates the creation of 3d objects 
     * 
     * @see boogiepants.model.Displayable#display()
     */
    public Node display() {
        group = new BranchGroup();
        group.setCapability(BranchGroup.ALLOW_DETACH);
        group.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        group.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        instrumentSwitch = new Switch();
        instrumentSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
        instrumentSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
       
        group.addChild(instrumentSwitch);

        for (ArrayList<Displayable> i : instrumentGroups) {
            BranchGroup bg = new BranchGroup();
            bg.setCapability(BranchGroup.ALLOW_DETACH);
            bg.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
            bg.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
            bg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
            instrumentSwitch.addChild(bg);
            for (Displayable j : i) {
                bg.addChild(j.display());
            }

            StrikeInstrumentBehavior strikeBehavior = new StrikeInstrumentBehavior(bg);
            BoundingSphere bounds =
                new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
            strikeBehavior.setSchedulingBounds(bounds);
            bg.addChild(strikeBehavior);
            
            SelectBehavior selectLocatable = new SelectBehavior(bg);
            bounds =
                new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
            selectLocatable.setSchedulingBounds(bounds);
            bg.addChild(selectLocatable);
        }
        return group;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString(){
        StringBuffer text = new StringBuffer();
        text.append("{");
        for (int i = 0; i < instrumentGroups.size(); i++) {
            text.append(WiiInputDevice.BUTTON_SYMBOLS[i] + ":");
            
            for (Displayable j: instrumentGroups.get(i)){
                text.append(j).append(", ");
            }
            text.append("\n");
        }
        text.append("}\n");
        return text.toString();
    }

    /**
     * returns index of most recent button pushed
     * 
     * @return
     */
    public int getLastPushed() {
        return lastPushed;
    }

    /**
     * indicates whether this .pants file was updated since last save
     * 
     * @return
     */
    public boolean isUpdated() {
        return updated;
    }

    /**
     * call when updating/saving this .pants file
     * @param updated
     */
    public void setUpdated(boolean updated) {
        this.updated = updated;
    }
    
    /**
     * display new added object
     */
    public void displayNew() {
        for (int i = 0; i < instrumentGroups.size(); i++) {
            BranchGroup bg = (BranchGroup) instrumentSwitch.getChild(i);
            ArrayList<Displayable> grp = instrumentGroups.get(i); 
            for (int j = 0 ; j < grp.size(); j++) {
                Displayable d = grp.get(j);
                if (d.getDisplay()==null){
                    bg.addChild(d.display());
                    updated=true;
                }
            }
        }
    }

    /**
     * @see boogiepants.model.Displayable#getDisplay()
     */
    @Override
    public Node getDisplay() {
        return instrumentSwitch;
    }
    
    /**
     * delete an instrument
     * @param instrument
     */
    public void delete(Deletable instrument){
        for (ArrayList<Displayable> i : instrumentGroups){
            i.remove((Displayable)instrument);
        }
        
    }
    
}
