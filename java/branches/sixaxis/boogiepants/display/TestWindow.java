package boogiepants.display;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Screen3D;
import javax.media.j3d.Switch;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import boogiepants.display.DancerDisplay;
import boogiepants.display.MenuBar;
import boogiepants.instruments.KeyBehavior;
import boogiepants.model.Stick;
import boogiepants.model.VisualInstrumentContainer;

import com.sun.j3d.utils.geometry.Text2D;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.picking.behaviors.PickTranslateBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class TestWindow extends JFrame {

    private static TestWindow window;
    public static TestWindow getWindow() {
        return window;
    }
    private static final int OFF_SCREEN_SCALE = 3;
    
    public static void main(String argv[]){
        Thread thread = new Thread() {
            public void run() {
                window = new TestWindow();
                window.createSceneGraph();

                window.setVisible(true);
            }
        };
        java.awt.EventQueue.invokeLater(thread);
    }
    private SimpleUniverse univ;
    private JPanel drawingPanel;
    private OffScreenCanvas3D offScreenCanvas3D;
    private Switch screenTextSwitch;
    private DancerDisplay dancerDisplay;
    private Stick stick;
    private VisualInstrumentContainer visualContainer;
    private boolean sideView;
    private TransformGroup vpTrans;
    private Transform3D frontViewTrans;
    private Transform3D sideViewTrans;
    private Canvas3D onScreenCanvas3D;
    private GraphicsDevice graphicsDevice;

    public TestWindow(){
        
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        setJMenuBar(new MenuBar());
        initComponents();
        GraphicsConfiguration config = SimpleUniverse
                .getPreferredConfiguration();

        Canvas3D c = new Canvas3D(config);

        univ = new SimpleUniverse(c);

        // This will move the ViewPlatform back a bit so the
        // objects in the scene can be viewed.
        univ.getViewingPlatform().setNominalViewingTransform();

        // Ensure at least 5 msec per frame (i.e., < 200Hz)
        univ.getViewer().getView().setMinimumFrameCycleTime(5);
        
        sideView = false;
        vpTrans = univ.getViewingPlatform().getViewPlatformTransform();
        
        
        frontViewTrans = new Transform3D();
        vpTrans.getTransform(frontViewTrans);
        sideViewTrans = new Transform3D(new Matrix4d(0, 1, 0, -2.414213562373095,
                                                     1, 0, 0, 0,
                                                     0, 0, 1, 0,
                                                     0, 0, 0, 1));

        // Create the content branch and add it to the universe
        onScreenCanvas3D = c;

        // Create Canvas3D and SimpleUniverse; add canvas to drawing panel
        drawingPanel.add(onScreenCanvas3D, java.awt.BorderLayout.CENTER);

        // Create the off-screen Canvas3D object
        createOffScreenCanvas(onScreenCanvas3D);

        // prepare objects for edit-mode display
        prepEditDisplay();
    }
    
    public void createSceneGraph(){
        // Create the root of the branch graph
        BranchGroup objRoot = new BranchGroup();

        // Create a TransformGroup to scale all objects so they
        // appear in the scene.
        TransformGroup objScale = new TransformGroup();
        Transform3D t3d = new Transform3D();
        t3d.setScale(0.7);
        objScale.setTransform(t3d);
        objRoot.addChild(objScale);

        dancerDisplay = DancerDisplay.getInstance();
        univ.getViewer().getPhysicalEnvironment().addInputDevice(dancerDisplay.getWiiInput());
        stick = dancerDisplay.getStick();
        objScale.addChild(stick.getDisplay());
        
        visualContainer = dancerDisplay.getVisualInstrumentContainer();
        objScale.addChild(visualContainer.getDisplay());
        
        lightingAndBackground(objRoot);
        screenTextSwitch.setWhichChild(0);
        
        LocatableCallback locatableCallback = new LocatableCallback();

        BranchGroup bg = new BranchGroup();
        KeyBehavior knb = new KeyBehavior(locatableCallback);
        Bounds b = new BoundingSphere(new Point3d(), Double.POSITIVE_INFINITY);
        knb.setSchedulingBounds(b);
        bg.addChild(knb);
        univ.addBranchGraph(bg);        
        
        bg = new BranchGroup();
        PickTranslateBehavior pickBehavior =new PickTranslateBehavior(objRoot, onScreenCanvas3D, 
                new BoundingSphere(new Point3d(), Double.POSITIVE_INFINITY));
//        pickBehavior.setupCallback(locatableCallback);
        pickBehavior.setMode(PickTool.GEOMETRY);
        pickBehavior.setTolerance(0.0f);
        bg.addChild(pickBehavior);
        univ.addBranchGraph(bg);        

        objRoot.compile();
        univ.addBranchGraph(objRoot);
        
    }

    private void lightingAndBackground(BranchGroup objRoot) {
        
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0),
                100.0);
        
        // Set up the background
        Color3f bgColor = new Color3f(.9f, .9f, .9f);
        Background bgNode = new Background(bgColor);
        bgNode.setApplicationBounds(bounds);
        objRoot.addChild(bgNode);

        // Set up the ambient light
        Color3f ambientColor = new Color3f(0.1f, 0.1f, 0.1f);
        AmbientLight ambientLightNode = new AmbientLight(ambientColor);
        ambientLightNode.setInfluencingBounds(bounds);
        objRoot.addChild(ambientLightNode);

        // Set up the directional lights
        Color3f light1Color = new Color3f(.6f, .5f, 0.5f);
        Vector3f light1Direction = new Vector3f(4.0f, -7.0f, -12.0f);
        Color3f light2Color = new Color3f(0.3f, 0.3f, 0.4f);
        Vector3f light2Direction = new Vector3f(-6.0f, -2.0f, -1.0f);

        DirectionalLight light1 = new DirectionalLight(light1Color,
                light1Direction);
        light1.setInfluencingBounds(bounds);
        objRoot.addChild(light1);

        DirectionalLight light2 = new DirectionalLight(light2Color,
                light2Direction);
        light2.setInfluencingBounds(bounds);
        objRoot.addChild(light2);
    }

    private void initComponents() {
        drawingPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Boogiepants V0.2.0--proving the concept");
        drawingPanel.setLayout(new java.awt.BorderLayout());

        drawingPanel.setPreferredSize(new java.awt.Dimension(500, 500));
        getContentPane().add(drawingPanel, java.awt.BorderLayout.CENTER);

        // prep for fullscreen mode
        GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        graphicsDevice = ge.getDefaultScreenDevice();

        pack();
    }

    private OffScreenCanvas3D createOffScreenCanvas(Canvas3D onScreenCanvas3D) {
        // Create the off-screen Canvas3D object
        // request an offscreen Canvas3D with a single buffer configuration
        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
        template.setDoubleBuffer(GraphicsConfigTemplate3D.UNNECESSARY);
        GraphicsConfiguration gc = GraphicsEnvironment
                .getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .getBestConfiguration(template);

        offScreenCanvas3D = new OffScreenCanvas3D(gc, true);
        // Set the off-screen size based on a scale factor times the
        // on-screen size
        Screen3D sOn = onScreenCanvas3D.getScreen3D();
        Screen3D sOff = offScreenCanvas3D.getScreen3D();
        Dimension dim = sOn.getSize();
        dim.width *= OFF_SCREEN_SCALE;
        dim.height *= OFF_SCREEN_SCALE;
        sOff.setSize(dim);
        sOff.setPhysicalScreenWidth(sOn.getPhysicalScreenWidth()
                * OFF_SCREEN_SCALE);
        sOff.setPhysicalScreenHeight(sOn.getPhysicalScreenHeight()
                * OFF_SCREEN_SCALE);

        // attach the offscreen canvas to the view
        univ.getViewer().getView().addCanvas3D(offScreenCanvas3D);

        return offScreenCanvas3D;

    }
    
    /**
     * Creates the objects ahead of time for the edit mode display.
     */
    public void prepEditDisplay() {
        TransformGroup transformGroup = univ.getViewingPlatform()
                .getMultiTransformGroup().getTransformGroup(0);
        BranchGroup bg = new BranchGroup();
        screenTextSwitch = new Switch();
        screenTextSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
        screenTextSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
        screenTextSwitch.setCapability(Switch.ALLOW_CHILDREN_EXTEND);
        screenTextSwitch.setCapability(Switch.ALLOW_CHILDREN_WRITE);

        screenTextSwitch.addChild(statusText("on"));

        screenTextSwitch.addChild(statusText("off"));

        bg.addChild(screenTextSwitch);
        transformGroup.addChild(bg);
    }

    /**
     * @param text
     * @return
     */
    private TransformGroup statusText(String text) {
        TransformGroup tg = new TransformGroup();
        Transform3D t = new Transform3D();
        t.setTranslation(new Vector3d(-0.33, -0.33, -1.0));
        Text2D textDisplay = new Text2D(text, new Color3f(0.0f, 0.0f, 0.0f),
                "Helvetica", 12, Font.ITALIC);
        tg.setTransform(t);
        tg.addChild(textDisplay);
        return tg;
    }
    
    public Switch getScreenTextSwitch() {
        return screenTextSwitch;
    }
}
