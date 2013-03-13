/**
 * 
 */
package boogiepants.display;

import java.io.IOException;

import wiiremotej.WiiRemote;
import wiiremotej.WiiRemoteJ;
import boogiepants.model.InstrumentContainer;
import boogiepants.wiiInput.WiiInputDevice;
import boogiepants.wiiInput.WiiSmoothingAdapter;
import junit.framework.TestCase;

/**
 * @author jstoner
 *
 */
public class ControlPanelTest extends TestCase {

    private InstrumentContainer ic;
    private WiiInputDevice wiiInput;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ic = new InstrumentContainer();
        
        wiiInput = WiiInputDevice.getInstance();
        wiiInput.setChainedListener(new WiiSmoothingAdapter(12));
        wiiInput.setRemote(getRemote());
        wiiInput.initialize();

    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        wiiInput.close();
    }
    
    public void testControlPanel(){
        ControlPanel.makeWindow();
        assert(true);
    }
    
    private static WiiRemote getRemote() {
        WiiRemote remote = null;
        try {
            remote = WiiRemoteJ.findRemote();
            remote.setAccelerometerEnabled(true);
            remote.setLEDIlluminated(0, true);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            if (remote!=null){
                remote.disconnect();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            if (remote!=null){
                remote.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (remote!=null){
                remote.disconnect();
            }
        }
        return remote;
    }


}
