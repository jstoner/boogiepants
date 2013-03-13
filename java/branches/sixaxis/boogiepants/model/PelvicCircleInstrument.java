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

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.geometry.Cone;

import boogiepants.display.Ring;
import boogiepants.instruments.InstrumentManager;
import boogiepants.instruments.PelvicCircleInstrumentBehavior;
import boogiepants.instruments.ToggleEventListener;

/**
 * The pelvic circle instrument allows a dancer to control something by the radial
 * angle of their pelvis with the core of their body.
 *  
 * @see boogiepants.instruments.PelvicCircleInstrumentBehavior
 * @author jstoner
 *
 */
public class PelvicCircleInstrument extends Selectable implements Serializable, Displayable, ToggleEventListener {

    static final long serialVersionUID = -3166900855653438823L;

    private transient PelvicCircleInstrumentBehavior behavior;
    private String oscAddress;
    private Color3f color;
    private transient BranchGroup container;

    /**
     * test mode creation of PCI, directly from code
     * 
     * TODO: may be refactored out
     * @param oscaddress
     * @param color
     */
    public PelvicCircleInstrument(String oscaddress, Color3f color){
        this.oscAddress = oscaddress;
        this.color = color;
    }
    
    /**
     * default constructor
     */
    public PelvicCircleInstrument() {
    }

    /**
     * creates display, behavior objects for instrument
     * 
     * @see boogiepants.model.Displayable#display()
     */
    public Node display() {
        container = new BranchGroup();
        container.setCapability(BranchGroup.ALLOW_DETACH);

        Cone cone = new Cone(.3f, 1f);
        Appearance a = new Appearance();
        a.setColoringAttributes(new ColoringAttributes(color, ColoringAttributes.NICEST));
        cone.setAppearance(a);
        Shape3D geom = (Shape3D) cone.getChild(0);
        geom.setUserData(this);

        TransformGroup coneMove = new TransformGroup();
        coneMove.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
        Transform3D conem = new Transform3D();
        conem.set(new Vector3d(0, -1.1, 0));
        coneMove.setTransform(conem);
        coneMove.addChild(cone);
        container.addChild(coneMove);

        TransformGroup ringTransform = new TransformGroup();
        ringTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        ringTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        
        behavior = new PelvicCircleInstrumentBehavior(oscAddress, ringTransform);
        BoundingSphere bounds =
            new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
        behavior.setSchedulingBounds(bounds);  
        container.addChild(behavior);
        container.addChild(ringTransform);
        ringTransform.addChild(new Ring(color));

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
        this.behavior.toggleEvent(value);
    }

    /**
     * 
     * @return
     */
    public Object getOSCAddress() {
        return oscAddress;
    }

    /**
     * @param oscAddress
     */
    public void setOSCAddress(String oscAddress) {
        this.oscAddress = oscAddress;
    }

    /**
     * @param color
     */
    public void setColor(Color3f color) {
        this.color = color;
    }

    /**
     * select this instrument for editing
     * 
     * @see boogiepants.model.Selectable#select()
     */
    public void select() {
//        EditPositionBehavior posEdit = EditPositionBehavior.getInstance();
//        posEdit.setInstrument(this);
    }


    /** 
     * TODO: determine whether this is necessary
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
