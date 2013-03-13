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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Enumeration;

import javax.media.j3d.Appearance;
import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Geometry;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.illposed.osc.OSCMessage;

import boogiepants.display.FileShape;
import boogiepants.instruments.EditPositionBehavior;
import boogiepants.instruments.InstrumentManager;
import boogiepants.instruments.OSCConnection;

/**
 * This class defines the StrikeInstrument. The StrikeInstrument creates a 3d
 * object that raises an OSC signal when the stick points at the object.
 * 
 * @author jstoner
 * 
 */
/**
 * @author jstoner
 *
 */
public class StrikeInstrument implements Externalizable, Displayable, Locatable{

    static final long serialVersionUID = 540531656791959689L;
    
    private transient TransformGroup shape;
    private transient BranchGroup editGroup;
    private transient OSCConnection oscConnection;
    private String oscAddress;
    private Color3f color;
    private Vector3d size;
    private Point3d position;

    private Matrix3d rot;
    private String shapeName;

    private OSCMessage msg;

    private transient BranchGroup container;

    /**
     * default constructor
     */
    public StrikeInstrument(){
        oscConnection = OSCConnection.getConnection();
    }

    /**
     * @param oscAddress
     */
    public void setOSCAddress(String oscAddress) {
        this.oscAddress = oscAddress;
        Object oscargs[] = new Object[2];
        oscargs[0] = new Integer(1);
        msg = new OSCMessage(oscAddress, oscargs);
    }

    /**
     * @param shapeName
     */
    public void setShapeName(String shapeName) {
        this.shapeName = shapeName;
    }
    
    /**
     * @see boogiepants.model.Locatable#setShape(javax.media.j3d.TransformGroup)
     */
    public void setShape(TransformGroup shape){
        throw new UnsupportedOperationException();
    }

    /**
     * @param color
     */
    public void setColor(Color3f color) {
        this.color = color;
    }

    /**
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        oscAddress = (String) in.readObject();
        color = (Color3f)in.readObject();
        size = (Vector3d)in.readObject();
        position = (Point3d)in.readObject();
        rot = (Matrix3d)in.readObject();
        shapeName = (String)in.readObject();
        
        Object oscargs[] = new Object[2];
        oscargs[0] = new Integer(1);
        msg = new OSCMessage(oscAddress, oscargs);
    }

    /**
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
       out.writeObject(oscAddress);
       out.writeObject(color);
       out.writeObject(size);
       out.writeObject(position);
       out.writeObject(rot);
       out.writeObject(shapeName);
    }

    /**
     * defines rotation, translation and size separately, so editing will
     * be easier to implement later.
     * 
     * test mode constructor--called directly from code. 
     * 
     * TODO: Probably refactor out.
     * 
     * @param oscaddress
     * @param fileShape
     * @param pos
     * @param size
     * @param rot
     */
    public StrikeInstrument(String oscaddress, String shapeName,  
            Point3d pos, Vector3d size, Matrix3d rot, Color3f color) {
        this.shapeName=shapeName;
        this.oscAddress=oscaddress;
        Object oscargs[] = new Object[2];
        oscargs[0] = new Integer(1);
        msg = new OSCMessage(oscAddress, oscargs);
        oscConnection = OSCConnection.getConnection();
        this.size = size;
        if (rot!=null){
            this.rot = rot;
        } else {
            this.rot = new Matrix3d();
            this.rot.setIdentity();
        }
        this.position = pos;
        this.color = color;
    }
    
    
    /**
     * creates 3d object for display. Gets model from filesystem, applies rotation,
     * translation, sizing, color
     * 
     * @see boogiepants.model.Displayable#display()
     */
    public Node display() {
        
        InstrumentManager im=InstrumentManager.getInstance();
        FileShape fileShape = im.getShapes().importShape(shapeName);
        shapeName = im.getShapes().shapeCacheKey(shapeName);
        fileShape.setLocatable(this);
        
        if (size==null){
            fileShape.resize(FileShape.UNIT_SIZE);
        } else {
            fileShape.resize(size);            
        }
        fileShape.rotate(rot, position);
        shape= fileShape.getTransform();
        
        if (color!= null){
            Enumeration<Shape3D> parts =  fileShape.getTransform().getAllChildren();
            while(parts.hasMoreElements()){
                Shape3D part= parts.nextElement();
                if(part.getAppearance()==null){
                    part.setAppearance(new Appearance());
                }
                Material m = new Material();
                m.setDiffuseColor(color);
                part.getAppearance().setMaterial(m);
                ColoringAttributes c = new ColoringAttributes();
                c.setColor(color);
                c.setShadeModel(ColoringAttributes.NICEST);
                part.getAppearance().setColoringAttributes(c);
            }
        }

        container = new BranchGroup();
        container.setCapability(BranchGroup.ALLOW_DETACH);
        container.addChild(shape);
        editGroup = new BranchGroup();
        editGroup.setCapability(BranchGroup.ALLOW_DETACH);
        editGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        editGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        shape.addChild(editGroup);

        return container;
    }

    /**
     * @return
     */
    public Object getOSCAddress() {
        return oscAddress;
    }
    
    /**
     * @see boogiepants.model.Displayable#getDisplay()
     */
    public TransformGroup getDisplay(){
        return shape;
    }
    
    /**
     * @see boogiepants.model.Locatable#setRelocate()
     */
    public void setRelocate(){
        setRelocate(.9);
    }
    
    
    /**
     * @see boogiepants.model.Locatable#setRelocate(double)
     */
    public void setRelocate(double distance){
        EditPositionBehavior posEdit = EditPositionBehavior.getInstance();
        posEdit.setInstrument(this);
        posEdit.setTransformGroup(shape);
        posEdit.initLocPoint(distance);
        BoundingSphere bounds =
            new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
        posEdit.setSchedulingBounds(bounds);
        BranchGroup bg = posEdit.getGroup();
        editGroup.addChild(bg);
    }

    /**
     * @see boogiepants.model.Locatable#getPosition()
     */
    public Point3d getPosition() {
        return (Point3d) position.clone();
    }

    /**
     * @see boogiepants.model.Locatable#setPosition(javax.vecmath.Point3d)
     */
    public void setPosition(Point3d position) {
        this.position = position;
    }

    /**
     * called when the strike event is detected
     */
    public void strike(){
        oscConnection.send(msg);
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString(){
        return  oscAddress + " " + shapeName + " " + color + " " + position;
    }

    /**
     * @see boogiepants.model.Deletable#delete()
     */
    @Override
    public void delete() {
        InstrumentManager.getInstance().getInstrumentContainer().delete(this);
        container.detach();
    }

}
