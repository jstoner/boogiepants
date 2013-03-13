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

import com.sun.j3d.utils.geometry.Box;

/**
 * the stick we use to play instruments
 * 
 * @author jstoner
 *
 */
public class Stick implements Displayable {

    private TransformGroup stickTrans;

    /**
     * @see boogiepants.model.Displayable#display()
     */
    @Override
    public Node display() {
        stickTrans = new TransformGroup();
        stickTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        stickTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        BranchGroup stickGroup = new BranchGroup();

        TransformGroup down = new TransformGroup();
        Transform3D downTrans = new Transform3D();
        downTrans.setTranslation(new Vector3d(0, -.4, 0));
        down.setTransform(downTrans);
        stickGroup.addChild(down);

        Appearance a = new Appearance();
        Group stick = new Box(.15f, .6f, .15f, a);
        down.addChild(stick);
        a.setColoringAttributes(new ColoringAttributes(.7f, .3f, .7f,
                ColoringAttributes.NICEST));
        stickTrans.addChild(stickGroup);
        Material m = new Material();
        m.setDiffuseColor(new Color3f(.7f, .3f, .7f));
        a.setMaterial(m);
        return stickTrans;
    }

    /**
     * @see boogiepants.model.Displayable#getDisplay()
     */
    @Override
    public Node getDisplay() {
        // TODO Auto-generated method stub
        return stickTrans;
    }

}
