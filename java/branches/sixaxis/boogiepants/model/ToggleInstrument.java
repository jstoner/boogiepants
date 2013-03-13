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

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

import com.illposed.osc.OSCMessage;
import com.sun.j3d.utils.geometry.Cone;

import boogiepants.instruments.InstrumentManager;
import boogiepants.instruments.OSCConnection;
import boogiepants.instruments.ToggleEventListener;

/**
 * @author jstoner
 *
 */
/**
 * this instrument simply sends a toggle event when its instrument group is
 *  selected. click once--on, click twice--off.
 * 
 * @author jstoner
 *
 */
public class ToggleInstrument extends Selectable implements Serializable, Displayable, ToggleEventListener {

    static final long serialVersionUID = -5196174550809977196L;
    
    private transient OSCConnection oscConnection;
    private String oscAddress;
    private Color3f color;
    private transient TransformGroup coneMove;
    private transient boolean state;
    private Float[] oscargs;
    private transient BranchGroup container;

    /**
     * @param oscAddress
     * @param color
     */
    public ToggleInstrument(String oscAddress, Color3f color){
        this();
        this.oscAddress = oscAddress;
        this.color = color;
        this.oscConnection = OSCConnection.getConnection();        
    }
    
    /**
     * default constructor
     */
    public ToggleInstrument(){
        this.state = false;
        this.oscargs = new Float[1];
        this.oscConnection = OSCConnection.getConnection();        
    }
    
    /**
     * @param oscAddress
     */
    public void setOSCAddress(String oscAddress) {
        this.oscAddress = oscAddress;
    }

    /**
     * @return
     */
    public String getOSCAddress() {
        return oscAddress;
    }

    /**
     * @param color
     */
    public void setColor(Color3f color) {
        this.color = color;
    }

    /**
     * @see boogiepants.model.Displayable#display()
     */
    
    public Node display() {
        Cone cone = new Cone(.3f, 1f);
        Appearance a = new Appearance();
        a.setColoringAttributes(new ColoringAttributes(color, ColoringAttributes.NICEST));
        cone.setAppearance(a);
        coneMove = new TransformGroup();
        coneMove.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
        Transform3D conem = new Transform3D();
        conem.set(new Vector3d(0, -1.1, 0));
        coneMove.setTransform(conem);
        coneMove.addChild(cone);
        container = new BranchGroup();
        container.setCapability(BranchGroup.ALLOW_DETACH);
        container.addChild(coneMove);
        cone.getShape(Cone.BODY).setUserData(this);

        return container;
    }

    /**
     * @see boogiepants.model.Displayable#getDisplay()
     */
    
    public Node getDisplay() {
        return container;
    }

    /**
     * @see boogiepants.instruments.ToggleEventListener#toggleEvent(boolean)
     */
    
    public void toggleEvent(boolean value) {
        if (value){
            state = !state;
            oscargs[0]= new Float(state ? 1f : 0f);
            OSCMessage message = new OSCMessage(oscAddress, oscargs);
            //TODO: fix this later
            this.oscConnection = OSCConnection.getConnection();        
            oscConnection.send(message);
        }
    }

    /**
     * @see boogiepants.model.Selectable#select()
     */
    
    public void select() {
//        EditPositionBehavior posEdit = EditPositionBehavior.getInstance();
//        posEdit.setInstrument(this);
    }

    /**
     * @see boogiepants.model.Selectable#unselect()
     */
    
    public void unselect() {
        
    }

    /**
     * @see boogiepants.model.Deletable#delete()
     */
    
    public void delete() {
        InstrumentManager.getInstance().getInstrumentContainer().delete(this);
        container.detach();
    }

}
