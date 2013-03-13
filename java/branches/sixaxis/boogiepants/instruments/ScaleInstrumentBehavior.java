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

import javax.media.j3d.Behavior;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Locale;
import javax.media.j3d.Node;
import javax.media.j3d.PickInfo;
import javax.media.j3d.PickRay;
import javax.media.j3d.PickSegment;
import javax.media.j3d.SceneGraphPath;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.illposed.osc.OSCMessage;

/**
 * This class implements the behavior of the ScaleInstrument. The
 * ScaleInstrument defines two endpoints, a low and a high, in the Instrument's
 * frame of reference, meaning they move with the core of the body.
 * 
 * When the stick intersects the line between the endpoints, this class detects
 * that and sends an OSC signal indicating where that intersection is. It
 * indicates that by proportion: a floating point number between 0 and 1. The
 * closer the intersection is to the low point, the closer the value to 0, and
 * the closer to the high point, the closer the value to 1.
 * 
 * It does this by detecting the points where the stick intersects this line,
 * and averaging them. This gives five cases:
 * <ol>
 * <li>intersection at two points: as above, detecting both and averaging them;
 * <li>the low endpoint inside the stick, and one intersection: to detect this,
 * it detects that there is one intersection, and that there is an intersection
 * with a ray extending out from the low point. It then averages those two
 * points, and if it falls within the line it returns a value;
 * <li>the high endpoint inside the stick, and one intersection: to detect this,
 * it detects that there is one intersection, and that there is an intersection
 * with a ray extending out from the high point. It then averages those two
 * points, and if it falls within the line it returns a value;
 * <li>an edge intersection: the line touches a corner of the stick. It returns
 * that location;
 * <li>no intersection.
 * </ol>
 * 
 * This signal is sent to the /[oscDest]/scale OSC address.
 * 
 * A signal is sent to the /[oscDest]/intersect OSC address when an intersection occurs.
 * Also, when this instrument's instrument group is toggled on or off, a signal is sent to
 * /[oscDest]/toggle--a 1 when it is turned on and a 0 when turned off. 
 * 
 * If the line is short enough to fit entirely inside the stick, no intersection
 * will be detected.
 * 
 * @author jstoner
 * 
 */
public class ScaleInstrumentBehavior extends Behavior implements ToggleEventListener { 

    private WakeupOnElapsedFrames conditions = new WakeupOnElapsedFrames(0);
    private OSCConnection oscConnection;
    private String oscScaleDest, oscIntersectDest, oscToggleDest;
    private Object oscargs[] = new Object[1];
    private OSCMessage msg;
    private double linelen;
    
    private PickSegment line=new PickSegment();
    private PickRay outer=new PickRay();
    private Vector3d slopeVec = new Vector3d();
    private Point3d loPoint, hiPoint,  loScratch= new Point3d(), hiScratch=new Point3d();
    private BranchGroup stickGroup;
    private Transform3D steptrans=new Transform3D(), trans = new Transform3D();
    private boolean intersect= false, prevIntersect = false;
    private InstrumentManager manager= InstrumentManager.getInstance();

    /**
     * 
     * @param oscDest
     * @param loPoint
     * @param hiPoint
     * @param stickGroup
     */
    public ScaleInstrumentBehavior(String oscDest,
                                       Point3d loPoint, Point3d hiPoint, 
                                       BranchGroup stickGroup) {
        this.oscConnection = OSCConnection.getConnection();
        this.oscScaleDest = oscDest + "/scale";
        this.oscIntersectDest = oscDest + "/intersect";
        this.oscToggleDest = oscDest + "/toggle";
        
        this.loPoint=loPoint;
        this.hiPoint=hiPoint;
        this.stickGroup=(BranchGroup) stickGroup;
    }

    /**
     * @see javax.media.j3d.Behavior#initialize()
     */
    @Override
    public void initialize() {
        wakeupOn( conditions );
    }
    
    /**
     * computes the length of the line after the position is established
     */
    public void computeLineLen(){
        this.linelen = hiPoint.distance(loPoint);
    }

    /** 
     * Does detection as described by class javadoc. Called at render of every frame. 
     * 
     * @see javax.media.j3d.Behavior#processStimulus(java.util.Enumeration)
     */
    @Override
    public void processStimulus(Enumeration criteria) {
        // get transform defining frame of reference for this object, and 
        // apply it to the endpoints 
        if(!manager.isEditMode()){
    
            trans.setIdentity();
            Node parent= this.getParent();
            while (parent!= null){
                if(parent instanceof TransformGroup){
                    ((TransformGroup)parent).getTransform(steptrans);
                    trans.mul(steptrans);
                }
                parent= parent.getParent();
            }
            trans.transform(loPoint, loScratch);
            trans.transform(hiPoint, hiScratch);
            line.set(loScratch, hiScratch);
            
            // detect point of intersection
            // if your IDE registers an error for pickAll(), this is because this code uses the
            // Java 3d 1.5 libraries, and your IDE is referring to the 1.3 libraries (perhaps packaged
            // with OSX 10.5.) It all comes out ok in the build.
            PickInfo[] info=stickGroup.pickAll(PickInfo.PICK_GEOMETRY, PickInfo.ALL_GEOM_INFO, line);
            double crossDist = 0;
            if(info != null){
                if (info.length== 2){
                    intersect=true;
                    crossDist = (info[0].getIntersectionInfos()[0].getDistance() +
                           info[1].getIntersectionInfos()[0].getDistance())/2;
                } else if(info.length==1){
                    slopeVec.x=hiScratch.x-loScratch.x;
                    slopeVec.y=hiScratch.y-loScratch.y;
                    slopeVec.z=hiScratch.z-loScratch.z;
                    outer.set(hiScratch, slopeVec);
                    PickInfo[] outinfo = stickGroup.pickAll(PickInfo.PICK_GEOMETRY, PickInfo.ALL_GEOM_INFO, outer);
                    if (outinfo != null){
                        if(outinfo.length==1){
                            crossDist = (info[0].getIntersectionInfos()[0].getDistance() +
                                         linelen+ outinfo[0].getIntersectionInfos()[0].getDistance())/2;
                            if(crossDist > linelen){
                                crossDist=linelen;
                                intersect=false;
                            }
                            else {
                                intersect=true;
                            }
                        }
                    } else {
                        slopeVec.negate();
                        outer.set(loScratch,slopeVec);
                        outinfo = stickGroup.pickAll(PickInfo.PICK_GEOMETRY, PickInfo.ALL_GEOM_INFO, outer);
                        if (outinfo!=null){
                            if (outinfo.length==1){
                                crossDist = (info[0].getIntersectionInfos()[0].getDistance() -
                                             outinfo[0].getIntersectionInfos()[0].getDistance())/2;
                                if(crossDist < 0){
                                    crossDist=0;
                                    intersect=false;
                                }else {
                                    intersect = true;
                                }
                                
                            }else {
                                crossDist = info[0].getIntersectionInfos()[0].getDistance();
                                intersect=true;
                            }
                        }
                    }   
            
                }
            }else {
                intersect = false;
            }

            if (intersect) {
                float msg = (float) (crossDist / linelen);
                messageToOSC(oscScaleDest, msg);
            }
            if (!intersect && prevIntersect) {
                messageToOSC(oscIntersectDest, 0f);
            } else if (intersect && !prevIntersect) {
                messageToOSC(oscIntersectDest, 1f);
            }
            prevIntersect = intersect;
        }
        wakeupOn( conditions );
    }

    /**
     * @see boogiepants.instruments.ToggleEventListener#toggleEvent(boolean)
     */
    public void toggleEvent(boolean value) {
        messageToOSC(oscToggleDest, value ? 1f : 0f);
        if (!value){
            messageToOSC(oscIntersectDest, 0f);
        }
    }

    /**
     * sends an actual numerical message to one of this instrument's addresses
     * 
     * @param oscaddr
     * @param signal
     */
    private void messageToOSC(String oscaddr, float signal) {
        oscargs[0]= new Float(signal);
        OSCMessage message = new OSCMessage(oscaddr, oscargs);
        oscConnection.send(message);
    }

}
