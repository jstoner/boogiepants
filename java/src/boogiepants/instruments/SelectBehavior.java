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
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Node;
import javax.media.j3d.PickInfo;
import javax.media.j3d.PickRay;
import javax.media.j3d.Transform3D;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.media.j3d.WakeupOr;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;

import boogiepants.model.Locatable;
import boogiepants.model.Selectable;
import boogiepants.wiiInput.WiiInputDevice;

/**
 * This class allows selection of instruments in edit mode. 
 * 
 * TODO: prime candidate for refactoring
 * 
 * @author jstoner
 *
 */
public class SelectBehavior extends Behavior {

    private PickTool chooser;
    private InstrumentManager manager;
    private WakeupOr condition;
    private Vector3d defaultVec;
    private Vector3d scratchVec;
    private Point3d origin;
    private WiiInputDevice wii;
    private Transform3D pelvTrans;
    private Transform3D coreTrans;
    private BranchGroup parent;

    /**
     * 
     * @param parent
     */
    public SelectBehavior(BranchGroup parent) {
        this.parent = parent;
        chooser = new PickTool(parent);
        chooser.setMode(PickTool.GEOMETRY);
        origin = new Point3d(0, 0, 0);
        manager = InstrumentManager.getInstance();
        wii = WiiInputDevice.getInstance();
        pelvTrans = new Transform3D();
        coreTrans = new Transform3D();
        defaultVec = new Vector3d(0, -1, 0); 
        scratchVec = new Vector3d();
    }

    /**
     * @see javax.media.j3d.Behavior#initialize()
     */
    @Override
    public void initialize() {
        condition = new WakeupOr(new WakeupCriterion[]{new WakeupOnElapsedFrames(0),
                new WakeupOnAWTEvent(AWTEvent.MOUSE_EVENT_MASK)});        
        wakeupOn(condition);
    }

    /**
     * in edit mode, if something isn't already selected this detects a mouse-pressed
     * event, and if the stick is pointed at an instrument, it's selected.  
     * 
     *  @see javax.media.j3d.Behavior#processStimulus(java.util.Enumeration)
     */
    @Override
    public void processStimulus(Enumeration criteria) {
        if(manager.isEditMode()){
            if( !EditPositionBehavior.existsSelectedLocatable()){
                while(criteria.hasMoreElements()){
                    WakeupCriterion i= (WakeupCriterion) criteria.nextElement();
                    if(i instanceof WakeupOnAWTEvent){
                        AWTEvent[] events = ((WakeupOnAWTEvent)i).getAWTEvent();
                        for (AWTEvent j: events){
                            if(j instanceof MouseEvent){
                                if (((MouseEvent)j).getButton() == MouseEvent.BUTTON1 && 
                                      ((MouseEvent)j).getID() == MouseEvent.MOUSE_PRESSED){
                                    wii.getSensor(WiiInputDevice.PELVIS_SENSOR_INDEX).getRead(pelvTrans);
                                    wii.getSensor(WiiInputDevice.CORE_SENSOR_INDEX).getRead(coreTrans);
                                    pelvTrans.transform(defaultVec, scratchVec);
                                    chooser.setShapeRay(origin, scratchVec);
                                    PickResult result = chooser.pickClosest();
                                    if (result != null){
                                        Node shape = result.getNode(PickResult.SHAPE3D);
                                        Object instrument =  shape.getUserData();
                                        if(instrument instanceof Locatable){
                                            Vector3d pos = new Vector3d(((Locatable) instrument).getPosition());
                                            double distance = pos.length();
                                            ((Locatable) instrument).setRelocate(distance);
                                        }else if (instrument instanceof Selectable){
                                            ((Selectable) instrument).select();
                                        }
                                        ((MouseEvent)j).consume();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        wakeupOn(condition);
    }

}
