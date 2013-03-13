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

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Group;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

import boogiepants.instruments.InstrumentManager;

import com.sun.j3d.utils.geometry.Box;

/**
 * contains the instruments which move with the core of the body
 * 
 * @author jstoner
 *
 */
public class VisualInstrumentContainer implements Displayable {

    private BranchGroup instrumentGroup;
    private InstrumentContainer instruments;
    private TransformGroup motionTrans;
    
    /**
     * 
     */
    public VisualInstrumentContainer() {
        motionTrans = new TransformGroup();
        motionTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        motionTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        InstrumentManager instrumentmgr = new InstrumentManager();
        instruments = instrumentmgr.getInstrumentContainer();
    }

    /* (non-Javadoc)
     * @see boogiepants.model.Displayable#display()
     */
    public Node display() {
        instrumentGroup = new BranchGroup();
        instrumentGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        instrumentGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        instrumentGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

        instrumentGroup.addChild(instruments.display());
        // instrumentmgr.writeInstruments(System.getProperty("user.home") +
        // "/boogiepants" + "/example.pants");
        motionTrans.addChild(instrumentGroup);
        
        Color3f c = InstrumentManager.getSticksColor();

        TransformGroup up = new TransformGroup();
        Transform3D upTrans = new Transform3D();
        upTrans.setTranslation(new Vector3d(0, .7, 0));
        up.setTransform(upTrans);
        Appearance a = new Appearance();
        Group topstick = new Box(.15f, .4f, .15f, a);
        a.setColoringAttributes(new ColoringAttributes(c, ColoringAttributes.NICEST));
        Material m = new Material();
        m.setDiffuseColor(c);
        a.setMaterial(m);

        up.addChild(topstick);
        motionTrans.addChild(up);


        return motionTrans;
    }

    /** 
     * @see boogiepants.model.Displayable#getDisplay()
     */
    
    public Node getDisplay() {
        return motionTrans;
    }

    /**
     * @return
     */
    public InstrumentContainer getInstruments() {
        return instruments;
    }

    /**
     * @param instruments
     */
    public void setInstruments(InstrumentContainer instruments) {
        this.instruments = instruments;
    }
    
    /**
     * 
     */
    public void resetInstruments(){
        InstrumentManager instrumentmgr = InstrumentManager.getInstance();
        InstrumentContainer instruments = instrumentmgr
                .getInstrumentContainer();
        
        instrumentGroup.removeChild(this.instruments.getDisplay());
        instrumentGroup.addChild(instruments.display());
    }

}
