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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.media.j3d.Behavior;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Node;
import javax.media.j3d.PickSegment;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.media.j3d.WakeupOr;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;

import boogiepants.model.Locatable;
import boogiepants.model.StrikeInstrument;
import boogiepants.wiiInput.WiiInputDevice;

/**
 * this detects collision of the stick with the strike instrument. We use picking instead
 * of collision detection as a workaround--there is a bad bug in collision detection. Also,
 * if two objects are in a state of collision already, collision detection won't work, but 
 * picking will.
 * 
 * @author jstoner
 *
 */
public class StrikeInstrumentBehavior extends Behavior {

    private PickTool detector;
    private InstrumentManager manager;
    private WakeupOnElapsedFrames condition;
    private WiiInputDevice wii;
    private Point3d basePoint, locPoint, locPointScratch;
    private BranchGroup parent;
    private Transform3D pelvTrans;
    private Set<Node> currentShapes;

    /**
     * preallocating objects for performance
     * 
     * @param parent
     */
    public StrikeInstrumentBehavior(BranchGroup parent){
        this.parent = parent;
        detector = new PickTool(parent);
        currentShapes = new HashSet<Node>();
        detector.setMode(PickTool.GEOMETRY);
        manager= InstrumentManager.getInstance();
        wii = WiiInputDevice.getInstance();
        basePoint = new Point3d(0, 0, 0);
        locPoint = new Point3d(0, -.6, 0);
        locPointScratch = new Point3d(0, 0, 0);
        pelvTrans = new Transform3D();
    }

    /**
     * @see javax.media.j3d.Behavior#initialize()
     */
    @Override
    public void initialize() {
        condition = new WakeupOnElapsedFrames(0);
        wakeupOn(condition);
    }

    /**
     * picking doesn't support collision entry/exit detection, so we have to
     * inmplement it here. we add new collisions to currrentShapes, and remove 
     * instruments when they aren't colliding anymore. We send a collision 
     * signal to OSC when adding.
     * 
     * @see javax.media.j3d.Behavior#processStimulus(java.util.Enumeration)
     */
    @Override
    public void processStimulus(Enumeration arg0) {
        if (!manager.isEditMode()){
            wii.getSensor(WiiInputDevice.PELVIS_SENSOR_INDEX).getRead(pelvTrans);
            pelvTrans.transform(locPoint, locPointScratch);
            detector.setShapeSegment(basePoint, locPointScratch);
            PickResult[] result = detector.pickAll();
            if (result != null){

                for (int i = 0; i < result.length; i++) {
                    Node shape = result[i].getNode(PickResult.SHAPE3D);
                    if (!currentShapes.contains(shape)){
                        Object instrument = shape.getUserData();
                        if (instrument instanceof StrikeInstrument){
                            ((StrikeInstrument)instrument).strike();
                            currentShapes.add(shape);
                        }
                    }
                }

                Iterator<Node> it = currentShapes.iterator();
                while ( it.hasNext()) {
                    Node i = it.next();
                    boolean found = false;
                    for (int j = 0; j < result.length; j++) {
                        Node shape = result[j].getNode(PickResult.SHAPE3D);
                        if (i == shape){
                            found = true;
                            break;
                        }
                    }
                    if (!found){
                        it.remove();
                    }                    
                }
            } else {
                currentShapes.clear();
            }
        }
        wakeupOn(condition);
    }
}
