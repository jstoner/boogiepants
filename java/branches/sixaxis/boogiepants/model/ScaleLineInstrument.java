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
import java.util.Enumeration;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import boogiepants.display.BoogiepantsDisplayWindow;
import boogiepants.instruments.InstrumentManager;
import boogiepants.instruments.ScaleInstrumentBehavior;
import boogiepants.instruments.ToggleEventListener;

import com.sun.j3d.utils.geometry.Sphere;

/**
 * This model object defines two endpoints, a low and a high, in the Instrument's
 * frame of reference, meaning they move with the core of the body.
 * 
 * When the stick intersects the line between the endpoints, the behavior class detects
 * that and sends an OSC signal indicating where that intersection is. It
 * indicates that by proportion: a floating point number between 0 and 1. The
 * closer the intersection is to the low point, the closer the value to 0, and
 * the closer to the high point, the closer the value to 1.
 *
 * @author jstoner
 *
 */
public class ScaleLineInstrument implements Serializable, Displayable, Deletable, ToggleEventListener{
    
    static final long serialVersionUID = -2349435494925025379L;
    
    private String oscaddress;
    private Point3d loPoint, hiPoint;
    private transient BranchGroup stickGroup;
    private transient ScaleInstrumentBehavior behavior;
    private transient Locatable loEndpoint, hiEndpoint;

    private transient BranchGroup container;

    private transient TransformGroup tg, transformGroupHi;

    private static Point3d zeroPoint = new Point3d(0d, 0d, 0d);
    
    /**
     * default constructor
     */
    public ScaleLineInstrument(){
        loPoint = new Point3d(-.2, -.9, 0);
        hiPoint = new Point3d(.2, -.9, 0);
    }
    
    /**
     * @param oscAddress
     */
    public void setOSCAddress(String oscAddress){
        this.oscaddress = oscAddress;
    }
    
    /**
     * @return
     */
    public String getOSCAddress() {
        return oscaddress;
    }

    /**
     * test mode creation of ScaleInstrument, directly from code
     * 
     * TODO: may be refactored out
     * 
     * @param oscaddress
     * @param loPoint
     * @param hiPoint
     */
    public ScaleLineInstrument(String oscaddress, Point3d loPoint, Point3d hiPoint){        
        this.oscaddress = oscaddress;
        this.loPoint = loPoint;
        this.hiPoint = hiPoint;
    }
    
    /**
     * provides Locatable interface for both endpoints
     * 
     * @param point
     * @param shape
     * @return
     */
    private Locatable endpoint(Point3d point, TransformGroup shape){
        Locatable endpoint = new Locatable(){

            private Point3d position;
            private TransformGroup shape;

            public void setShape(TransformGroup shape){
                this.shape = shape;
            }
            
            public Point3d getPosition() {
                return (Point3d) position.clone();
            }

            /**
             * this allows the positions of the endpoints to be set in sequence 
             * when we add a new ScaleInstrument.
             * 
             * @see boogiepants.model.Locatable#setPosition(javax.vecmath.Point3d)
             */
            public void setPosition(Point3d position) {
                if (this.position == null){
                    this.position = position;
                }else {
                    this.position.set(position);
                }
                if (behavior != null){
                    behavior.computeLineLen();
                }
            }

            public String toString(){
                String name = (ScaleLineInstrument.this.loEndpoint == this)? "loEndpoint": 
                    ((ScaleLineInstrument.this.hiEndpoint == this)? "hiEndpoint": "unassigned"); 
                return name + " "+ position;
            }
            
            public void delete(){
                ScaleLineInstrument.this.delete();
            }

            
            public boolean releasable() {
                return !ScaleLineInstrument.this.hiEndpoint.getPosition().equals(zeroPoint);
            }
        };
        endpoint.setShape(shape);
        endpoint.setPosition(point);
        return endpoint;
    }
        
    /**
     * @see boogiepants.model.Deletable#delete()
     */
    public void delete() {
        InstrumentManager.getInstance().getInstrumentContainer().delete(this);
        container.detach();
    }
    
    /**
     * @see boogiepants.model.Displayable#display()
     */
    public Node display() {
        TransformGroup loEndPoint =  setupEndpoint(loPoint, new ColoringAttributes(.5f, .5f, 1f, ColoringAttributes.NICEST));
        TransformGroup hiEndPoint =  setupEndpoint(hiPoint, new ColoringAttributes(1f, .5f, .5f, ColoringAttributes.NICEST));

        container= new BranchGroup();
        container.setCapability(BranchGroup.ALLOW_DETACH);
        container.addChild(hiEndPoint);
        container.addChild(loEndPoint);
        container.addChild(tg);
        stickGroup= BoogiepantsDisplayWindow.getInstance().getDancerDisplay().getStick().getStickGroup();

        behavior = new ScaleInstrumentBehavior(oscaddress, 
                loPoint, hiPoint,
                stickGroup); 
        container.addChild(behavior);
        BoundingSphere bounds =
            new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
        behavior.setSchedulingBounds(bounds);
        
        behavior.computeLineLen();
 
        return container;
    }

    /**
     * 
     */
    private TransformGroup setupEndpoint(Point3d p, ColoringAttributes c) {
        Appearance a = new Appearance();
        Group shape=new Sphere(.15f, a);
        a.setColoringAttributes(c);
        a.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, .3f));
        Transform3D t = new Transform3D();

        t.set(new Vector3d(p.x, p.y, p.z));
        TransformGroup tg= new TransformGroup();
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tg.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
        tg.setTransform(t);
        tg.addChild(shape);
        Locatable endpoint = endpoint(p, tg);
        tg.setUserData(endpoint);
        return (tg);
    }

    /**
     * @see boogiepants.instruments.ToggleEventListener#toggleEvent(boolean)
     */
    public void toggleEvent(boolean value) {
        this.behavior.toggleEvent(value);
    }

    /**
     * @see boogiepants.model.Displayable#getDisplay()
     */
    public Node getDisplay() {
        return container;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString(){
        return oscaddress + " " + loPoint + " "+ hiPoint;
    }
        
}
