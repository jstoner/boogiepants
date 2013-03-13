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

import boogiepants.instruments.InstrumentManager;
import boogiepants.model.InstrumentContainer;
import boogiepants.model.Stick;
import boogiepants.model.VisualInstrumentContainer;
import boogiepants.wiiInput.WiiSensorBehavior;
import boogiepants.wiiInput.WiiInputDevice;

import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Text2D;
import com.sun.j3d.utils.universe.*;

import javax.media.j3d.*;
import javax.vecmath.*;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JPopupMenu;

import wiiremotej.WiiRemote;
import wiiremotej.WiiRemoteJ;

/**
 * BoogiepantsDisplayWindow creates the window where the stick interacts with
 * the instruments. It sets up the 3d display environment.
 * 
 * This class was heavily adapted from Sun's 3D demo code, specifically
 * PrintCanvas3D. It bears a small resemblance to that code.
 * 
 * @author John Stoner
 * 
 */
public class BoogiepantsDisplayWindow extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    private static BoogiepantsDisplayWindow window = null;
    private Canvas3D onScreenCanvas3D;
    private OffScreenCanvas3D offScreenCanvas3D;
    private static final int OFF_SCREEN_SCALE = 3;
    private javax.swing.JPanel drawingPanel;
    private boolean fullScreenMode = false;

    private SimpleUniverse univ = null;
    private Rectangle previousSize;
    private GraphicsDevice graphicsDevice;
    private Switch screenTextSwitch;
    private ArrayList<BranchGroup> editModeTexts;
    private WiiSensorBehavior posBehavior;
    private BranchGroup stickGroup;
    private VisualInstrumentContainer visualContainer;
    private Stick stick;

    /**
     * @param static instance creator for Swing window objects
     */
    public static void makeWindow(final WiiInputDevice wiiInput) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                window = new BoogiepantsDisplayWindow();
                window.createSceneGraph(wiiInput);

                window.setVisible(true);
            }
        });
    }

    /**
     * This may seem like a weird way to do a Singleton. Let me explain: We
     * always create the BoogiepantsDisplayWindow with makeWindow(). It does so
     * in a separate thread. There are other objects that need a handle on the
     * BoogiepantsDisplayWindow object. If they call before it's instantiated,
     * it really should blow up with an NPE. But getting that handle is an
     * explicitly separate operation than creating the object.
     * 
     * @return
     */
    public static BoogiepantsDisplayWindow getInstance() {
        return window;
    }

    /**
     * Creates new Boogiepants display window
     */
    private BoogiepantsDisplayWindow() {

        // Initialize the GUI components
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

        // Create the content branch and add it to the universe
        onScreenCanvas3D = c;

        // Create Canvas3D and SimpleUniverse; add canvas to drawing panel
        drawingPanel.add(onScreenCanvas3D, java.awt.BorderLayout.CENTER);

        // Create the off-screen Canvas3D object
        createOffScreenCanvas(onScreenCanvas3D);

        // prepare objects for edit-mode display
        prepEditDisplay();

    }

    /**
     * createSceneGraph generates and compiles the main graph of objects that
     * represents the scene, including the wii input device and the stick, the
     * lighting and so forth.
     * 
     * @param locale
     * @param wiiRemote
     * @return
     */
    public void createSceneGraph(WiiInputDevice wiiInput) {
        // Create the root of the branch graph
        BranchGroup objRoot = new BranchGroup();

        // Create a TransformGroup to scale all objects so they
        // appear in the scene.
        TransformGroup objScale = new TransformGroup();
        Transform3D t3d = new Transform3D();
        t3d.setScale(0.7);
        objScale.setTransform(t3d);
        objRoot.addChild(objScale);

        univ.getViewer().getPhysicalEnvironment().addInputDevice(wiiInput);

        stick = new Stick();
        objScale.addChild(stick.display());

        visualContainer = new VisualInstrumentContainer();
        objScale.addChild(visualContainer.display());
        
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0),
                100.0);

        InstrumentContainer instruments = visualContainer.getInstruments();
        posBehavior = new WiiSensorBehavior((TransformGroup) visualContainer.getDisplay(), 
                (TransformGroup) stick.getDisplay(), wiiInput, instruments);

        objRoot.addChild(posBehavior);
        posBehavior.setSchedulingBounds(bounds);

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

        objRoot.compile();
        univ.addBranchGraph(objRoot);

    }

    /**
     * creates off-screen canvas
     * 
     * @param onScreenCanvas3D
     * @return
     */
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

    // ----------------------------------------------------------------

    /**
     * This method is called from within the constructor to initialize the form.
     */
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

    /**
     * Sets the screen to full-screen mode and back. Note three things,
     * ill-documented:
     * 
     * <ol>
     * <li>the full screen mode looks best when you undecorate the window--take
     * out the title bar and stuff. But you can't undecorate a displayable
     * window. And of course, when you display it, it's... displayable. So you
     * call this.dispose(), change the undecorated status, and then call
     * this.setVisible(true).</li>
     * <li>There is a bug (I think it's a bug, anyway) that causes the 3d window
     * to scale with the width of the window, not the minimum of the width and
     * height. so the image scales off the bottom and top of the screen when the
     * window is wider than it is tall. This is why we monkey with the
     * univ.getViewingPlatform()--we move it back when going full screen, and
     * forward when going back to the windowed view.</li>
     * <li>the 3d viewing area bounds do not automatically resize to the full
     * screen size. This is why we monkey with the bounds/preferredSize.</li>
     * </ol>
     */
    public void toggleFullScreen() {
        if (fullScreenMode) {
            this.getJMenuBar().setPreferredSize(null);
            this.dispose();
            this.setUndecorated(false);
            this.setResizable(true);
            this.setBounds(previousSize);
            graphicsDevice.setFullScreenWindow(null);
            this.setVisible(true);
            univ.getViewingPlatform().setNominalViewingTransform();
        } else {
            if (graphicsDevice.isFullScreenSupported()) {
                this.getJMenuBar().setPreferredSize(new java.awt.Dimension());
                InstrumentManager manager = InstrumentManager.getInstance();
                if (manager.isEditMode()) {
                    manager.toggleEditMode();
                }
                this.dispose();
                previousSize = this.getBounds();
                this.setUndecorated(true);
                this.setResizable(false);
                graphicsDevice.setFullScreenWindow(this);
                drawingPanel.setPreferredSize(new java.awt.Dimension(this
                        .getBounds().width, this.getBounds().height));
                this.setVisible(true);
                Transform3D t = new Transform3D();
                TransformGroup tg = univ.getViewingPlatform()
                        .getMultiTransformGroup().getTransformGroup(0);
                tg.getTransform(t);
                t.setTranslation(new Vector3d(0.0d, 0.0d, 3.2d));
                tg.setTransform(t);
            }

        }
        fullScreenMode = !fullScreenMode;
    }

    /**
     * Creates the objects ahead of time for the edit mode display.
     */
    public void prepEditDisplay() {
        TransformGroup transformGroup = univ.getViewingPlatform()
                .getMultiTransformGroup().getTransformGroup(0);
        BranchGroup bg = new BranchGroup();
        screenTextSwitch = new Switch();
        screenTextSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
        screenTextSwitch.setCapability(Switch.ALLOW_CHILDREN_EXTEND);
        screenTextSwitch.setCapability(Switch.ALLOW_CHILDREN_WRITE);

        for (String i : WiiInputDevice.BUTTON_SYMBOLS) {
            screenTextSwitch.addChild(statusText("edit mode " + i));
        }

        screenTextSwitch.addChild(statusText("edit mode"));

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

    /**
     * Displays the edit mode message when boogiepants is in edit mode. Switches
     * out of full-screen mode, so we can show other windows if necessary.
     * 
     * @param editMode
     */
    public void editDisplay(boolean editMode) {
        if (editMode) {
            if (fullScreenMode) {
                toggleFullScreen();
            }
            InstrumentContainer instruments = InstrumentManager.getInstance()
                    .getInstrumentContainer();
            screenTextSwitch.setWhichChild(instruments.getLastPushed());
        } else {
            screenTextSwitch.setWhichChild(Switch.CHILD_NONE);
        }
        ((MenuBar) this.getJMenuBar()).editDisplay(editMode);
    }

    /**
     * @param newInstrumentSet
     * 
     *            changes display indicator of instrument set in edit mode
     */
    public void changeInstrumentSetEditDisplay(int newInstrumentSet) {
        screenTextSwitch.setWhichChild(newInstrumentSet);
    }

    /**
     * <p>
     * The job of this method is to release the current instrument container's
     * displayed items when a new set is added, as in File>New or File>Open.
     * </p>
     * 
     */
    public void resetInstrumentView() {
        visualContainer.resetInstruments();
        posBehavior.resetInstruments();
    }

    public BranchGroup getStickGroup() {
        return stickGroup;
    }
}
