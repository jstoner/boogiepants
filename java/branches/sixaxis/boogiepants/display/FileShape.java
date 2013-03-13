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

package boogiepants.display;
 

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
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

import boogiepants.model.Locatable;
import boogiepants.util.Util;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.geometry.Box;

/**
 * @author jstoner
 *
 * this object encapsulates 3d graphical objects as obtained from disk.
 * Given a filename, it returns an object, and resizes it to an appropriate
 * size.
 */
public class FileShape implements Cloneable{

    private BranchGroup scene = null;
    private TransformGroup transform = null;
    private String filename;
    private String tempLoc;
    private BoundingBox objectBounds;
    
    public static Vector3d UNIT_SIZE =new Vector3d(.25d, .25d, .25d);
    
    /** 
     * creates copy... mostly used to copy objects out of imported objects
     * @see java.lang.Object#clone()
     */
    public FileShape clone(){
        BranchGroup scene= (BranchGroup) this.scene.cloneTree(true);
        return new FileShape(scene, filename, tempLoc);
    }
        
    /**
     * @param scene
     * @param filename
     * @param tempLoc
     */
    private FileShape(BranchGroup scene, String filename, String tempLoc){
        this.scene= scene;
        this.filename=filename;
        this.tempLoc=tempLoc;
        this.transform = new TransformGroup();
    }
    
    /**
     * this creates a 3d model from a description on disk.
     * 
     * @param filename
     * @param creaseAngle
     */
    public FileShape(String filename, double creaseAngle) {
        this.filename=filename.substring(filename.lastIndexOf(File.separator));
        int flags = ObjectFile.RESIZE;
        flags |= ObjectFile.TRIANGULATE;
        flags |= ObjectFile.STRIPIFY;
        ObjectFile f = new ObjectFile(flags,
                (float) (creaseAngle * Math.PI / 180.0));
        InputStream fileStream= null;
        try {
            File file= new File(filename);
 
            scene = f.load(file.getCanonicalPath()).getSceneGroup();
            String extension = filename.substring(filename.lastIndexOf('.'));
            fileStream = new FileInputStream(file);
            tempLoc= Util.copyInputStreamToTmpFile(fileStream, extension);
        } catch (IOException e) {
            System.err.println(e);
            scene = (BranchGroup) errorBox();
        } catch (ParsingErrorException e) {
            System.err.println(e);
            scene = (BranchGroup) errorBox();
        } catch (IncorrectFormatException e) {
            System.err.println(e);
            scene = (BranchGroup) errorBox();
        }  finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * this creates a 3d model from an input stream.
     * 
     * @param filename
     * @param creaseAngle
     */
    public FileShape(InputStream input, String filename, double creaseAngle) {
        this.filename=filename;
        int flags = ObjectFile.RESIZE;
        flags |= ObjectFile.TRIANGULATE;
        flags |= ObjectFile.STRIPIFY;
        ObjectFile f = new ObjectFile(flags,
                (float) (creaseAngle * Math.PI / 180.0));
        try {
            String extension = filename.substring(filename.lastIndexOf('.'));
            tempLoc= Util.copyInputStreamToTmpFile(input, extension);
            scene = f.load(tempLoc).getSceneGroup();
        } catch (IOException e) {
            e.printStackTrace();
            scene = (BranchGroup) errorBox();
        } catch (ParsingErrorException e) {
            e.printStackTrace();
            scene = (BranchGroup) errorBox();
        } catch (IncorrectFormatException e) {
            e.printStackTrace();
            scene = (BranchGroup) errorBox();
        }
    }
    
    /**
     * creates a model for display in the event that an error occurs--
     * a red box
     * 
     * @return
     */
    private Node errorBox(){
        return getModelBox(Color.RED);
    }

    /**
     * Returns the 3D model of this piece that fits 
     * in a box whose size is defined by the vector passed in,
     * centered at the origin.
     */
    public void resize(Vector3d size) {
      // Get model bounding box size
        objectBounds = new BoundingBox(new Point3d(
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY), new Point3d(
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY));
        computeBounds(scene, objectBounds);
        Point3d lower = new Point3d();
        objectBounds.getLower(lower);
        Point3d upper = new Point3d();
        objectBounds.getUpper(upper);

        // Translate model to its center
        Transform3D translation = new Transform3D();
        translation.setTranslation(new Vector3d(-lower.x - (upper.x - lower.x)
                / 2, -lower.y - (upper.y - lower.y) / 2, -lower.z
                - (upper.z - lower.z) / 2));
        // Scale model to make it fill a 1 unit wide box
        Transform3D scaleUnitTransform = new Transform3D();
        scaleUnitTransform.setScale(new Vector3d(size.x / (upper.x - lower.x),
                size.y / (upper.y - lower.y), size.z / (upper.z - lower.z)));
        scaleUnitTransform.mul(translation);

        transform.setTransform(scaleUnitTransform);
        Enumeration<Node> e = scene.getAllChildren();
        while(e.hasMoreElements()){
            Node next= e.nextElement();
            scene.removeChild(next);
            transform.addChild(next);            
        }
        transform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        transform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transform.setCapability(TransformGroup.ENABLE_COLLISION_REPORTING);
        transform.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        transform.setCapability(TransformGroup.ENABLE_PICK_REPORTING);

    }

    /**
     * rotates and repositions object
     * 
     * @param rot
     * @param pos
     */
    public void rotate(Matrix3d rot, Point3d pos) {
        if (pos==null){
            pos=new Point3d(0d, 0d, 0d);
        }
        if (rot == null){
            rot = new Matrix3d();
            rot.setIdentity();
        }
        Transform3D t= new Transform3D();
        transform.getTransform(t);
        Matrix4d m4 = new Matrix4d();
        t.get(m4);
        Matrix3d m3 = new Matrix3d();
        m4.getRotationScale(m3);
        Matrix3d temprot = (Matrix3d) rot.clone();
        temprot.mul(m3);

        t.set(temprot, new Vector3d(pos), 1);
        transform.setTransform(t);
    }


    /**
     * returns transform of this shape
     * @return
     */
    public TransformGroup getTransform() {
        return transform;
    }
    /**
     * Returns a box that may replace model. 
     */
    public static Node getModelBox(Color color) {
        Material material = new Material();
        material.setDiffuseColor(new Color3f(color));
        material.setAmbientColor(new Color3f(color.darker()));

        Appearance boxAppearance = new Appearance();
        boxAppearance.setMaterial(material);
        return new Box(0.5f, 0.5f, 0.5f, boxAppearance);
    }
      
     /**
     * computes the bounds of a 3d model
     * 
     * @param node
     * @param bounds
     */
    private static void computeBounds(Node node, BoundingBox bounds) {
        if (node instanceof Group) {
          // Compute the bounds of all the node children
          Enumeration enumeration = ((Group)node).getAllChildren();
          while (enumeration.hasMoreElements ()) {
            computeBounds((Node)enumeration.nextElement (), bounds);
          }
        } else if (node instanceof Shape3D) {
          Bounds shapeBounds = ((Shape3D)node).getBounds();
          bounds.combine(shapeBounds);
        }
      }


    /**
     * @return
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @return
     */
    public String getTempLoc() {
        return tempLoc;
    }

    /**
     * sets Locatable (model object for movable instruments) in this shape, so
     * we can trace back to it on selection
     * 
     * @param locatable
     */
    public void setLocatable(Locatable locatable){
        transform.setUserData(locatable);
        Enumeration e = scene.getAllChildren();
        while (e.hasMoreElements()){
            Node n = (Node) e.nextElement();
            if (n instanceof Shape3D){
                n.setUserData(locatable);
                n.setCapability(Node.ENABLE_PICK_REPORTING);
            }
        }
    }
    
    /**
     * takes shape file out of its temp file and streams it to output
     */
    public void saveShapeToOutputStream(OutputStream output){
        try {
            InputStream input = new FileInputStream(new File(tempLoc));
            Util.copyInputStreamToOutput(input, output);
            input.close();
            output.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}