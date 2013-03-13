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
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.media.j3d.WakeupOr;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import boogiepants.model.Deletable;
import boogiepants.model.Locatable;
import boogiepants.model.Selectable;
import boogiepants.util.Util;
import boogiepants.wiiInput.WiiInputDevice;

/**
 * This is the complementary behavior to SelectBehavior. SelectBehavior defines what
 * happens when you press the mouse button to select an object for editing. EditPositionBehavior
 * defines the key presses, and the release of the button:
 * 
 * If the object is a Locatable, it's left in the position where it is. If the object is a Selectable,
 * it's just unselected. 
 * 
 *  It also controls vertical positioning along the stick. You can move a Locatable up and down 
 *  along the stick with the up and down cursor keys.
 * 
 * TODO: prime candidate for refactoring
 * 
 * @author jstoner
 *
 */
public class EditPositionBehavior extends Behavior {

    private static EditPositionBehavior instance;
    private BranchGroup group;
    private InstrumentManager manager;
    private WakeupOr condition;
    private Deletable instrument;
    private TransformGroup transformGroup;
    private WiiInputDevice wii;
    private Point3d locPoint, locPointScratch;
    private Vector3d locVecScratch;
    private Transform3D pelvTrans;
    private Transform3D coreTrans;
    private Transform3D relocate;
    private Transform3D matrixScratch;
    private double[] matrix;
    private static final double HEIGHT_STEP = .025;
    private static boolean editInProgress = false;
    private static int deleteKey;

    public static boolean existsSelectedLocatable(){
        return editInProgress;
    }
    
    /**
     * @return
     * @throws MultipleEditException
     */
    public static EditPositionBehavior getInstance() throws MultipleEditException{
        if (Util.isMac()){
            deleteKey = KeyEvent.VK_BACK_SPACE;
        } else {
            deleteKey = KeyEvent.VK_DELETE;
        }
        if (instance==null){
            instance = new EditPositionBehavior();
        } else if (editInProgress){
            System.out.println("whoops-edit in progress. This shouldn't happen.");
            if (instance.instrument != null){
                instance.releaseInstrument();
            }
            //throw new MultipleEditException();
        }
        editInProgress = true;
        return instance;
    }

    /**
     * sets up object, preallocating scratch objects for faster operation later
     * in execution, less garbage collection, also creating BranchGroup and
     * BoundingSphere objects needed for operation
     */
    private EditPositionBehavior(){
        manager= InstrumentManager.getInstance();
        wii = WiiInputDevice.getInstance();
        group = new BranchGroup();
        group.setCapability(BranchGroup.ALLOW_DETACH);
        group.addChild(this);
        locPoint = new Point3d(0, 0, 0);
        locPointScratch = new Point3d(0, 0, 0);
        locVecScratch = new Vector3d(0, 0, 0);
        pelvTrans = new Transform3D();
        coreTrans = new Transform3D();
        relocate = new Transform3D();
        matrixScratch = new Transform3D();
        matrix = new double[16];
        BoundingSphere bounds =
            new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
        setSchedulingBounds(bounds);
    }
    
    /**
     * @see javax.media.j3d.Behavior#initialize()
     */
    public void initialize() {
        condition = new WakeupOr(new WakeupCriterion[]{new WakeupOnElapsedFrames(0),
                                 new WakeupOnAWTEvent(AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK)});        
        wakeupOn(condition);
    }
    
    /**
     * Initialize the distance of a Locatable from (0,0,0)
     * 
     * @param distance
     */
    public void initLocPoint(double distance){
        locPoint.x = 0;
        locPoint.y = -distance;
        locPoint.z = 0;
    }

    /**
     * Process edit operations: mouse release, up and down cursor keys
     * 
     * @see javax.media.j3d.Behavior#processStimulus(java.util.Enumeration)
     */
    @Override
    public void processStimulus(Enumeration criteria) {
        if (manager.isEditMode()){
            while(criteria.hasMoreElements()){
                WakeupCriterion i= (WakeupCriterion) criteria.nextElement();
                if(i instanceof WakeupOnAWTEvent){
                    AWTEvent[] events = ((WakeupOnAWTEvent)i).getAWTEvent();
                    for (AWTEvent j: events){
                        if (j instanceof KeyEvent){
                            if (((KeyEvent)j).getKeyCode() == KeyEvent.VK_UP){
                                locPoint.y += HEIGHT_STEP;
                            }else if (((KeyEvent)j).getKeyCode() == KeyEvent.VK_DOWN){
                                locPoint.y -= HEIGHT_STEP;
                            }else if (((KeyEvent)j).getKeyCode() == deleteKey){
                                editInProgress = false;
                                group.detach();
                                instrument.delete();
                            }

                        }else if(j instanceof MouseEvent){
                            if (((MouseEvent)j).getButton() == MouseEvent.BUTTON1 && 
                                    ((MouseEvent)j).getID() == MouseEvent.MOUSE_RELEASED){
                                editInProgress = false;
                                group.detach();
                                releaseInstrument();
                            }
                        }
                    }
                }
            }
            if(instrument instanceof Locatable){
                computePosition();
            }
        }
        wakeupOn(condition); 
    }

    /**
     * 
     */
    private void releaseInstrument() {
        if(instrument instanceof Locatable){
            computePosition();
            ((Locatable) instrument).setPosition(locPointScratch);
        }
        else if  (instrument instanceof Selectable){
            ((Selectable) instrument).unselect();
        }
    }

    /**
     * 
     */
    private void computePosition() {
        wii.getSensor(WiiInputDevice.PELVIS_SENSOR_INDEX).getRead(pelvTrans);
        wii.getSensor(WiiInputDevice.CORE_SENSOR_INDEX).getRead(coreTrans);
        coreTrans.invert();
        pelvTrans.mul(coreTrans);
        pelvTrans.transform(locPoint, locPointScratch);
        locVecScratch.set(locPointScratch);
        transformGroup.getTransform(matrixScratch);
        matrixScratch.get(matrix);
        matrix[3] = locPointScratch.x;
        matrix[7] = locPointScratch.y;
        matrix[11] = locPointScratch.z;
        relocate.set(matrix);
        
        transformGroup.setTransform(relocate);
    }

    /**
     * @param instrument
     */
    public void setInstrument(Deletable instrument) {
        this.instrument = instrument;
    }

    /**
     * @param transformGroup
     */
    public void setTransformGroup(TransformGroup transformGroup) {
        this.transformGroup = transformGroup;
    }
    
    /**
     * @return
     */
    public BranchGroup getGroup(){
        return group;
    }

}
