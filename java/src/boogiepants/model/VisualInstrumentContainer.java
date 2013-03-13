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
        InstrumentManager instrumentmgr = new InstrumentManager();
        instruments = instrumentmgr.getInstrumentContainer();
    }

    /* (non-Javadoc)
     * @see boogiepants.model.Displayable#display()
     */
    @Override
    public Node display() {
        motionTrans = new TransformGroup();
        motionTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        motionTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        instrumentGroup = new BranchGroup();
        instrumentGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        instrumentGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        instrumentGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

        instrumentGroup.addChild(instruments.display());
        // instrumentmgr.writeInstruments(System.getProperty("user.home") +
        // "/boogiepants" + "/example.pants");
        motionTrans.addChild(instrumentGroup);

        TransformGroup up = new TransformGroup();
        Transform3D upTrans = new Transform3D();
        upTrans.setTranslation(new Vector3d(0, .7, 0));
        up.setTransform(upTrans);
        Appearance a = new Appearance();
        Group topstick = new Box(.15f, .4f, .15f, a);
        a.setColoringAttributes(new ColoringAttributes(.7f, .3f, .7f,
                ColoringAttributes.NICEST));
        Material m = new Material();
        m.setDiffuseColor(new Color3f(.7f, .3f, .7f));
        a.setMaterial(m);

        up.addChild(topstick);
        motionTrans.addChild(up);

        // TODO Auto-generated method stub
        return motionTrans;
    }

    /* (non-Javadoc)
     * @see boogiepants.model.Displayable#getDisplay()
     */
    @Override
    public Node getDisplay() {
        // TODO Auto-generated method stub
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
        
        instrumentGroup.removeAllChildren();
        instrumentGroup.addChild(instruments.display());
    }

}
