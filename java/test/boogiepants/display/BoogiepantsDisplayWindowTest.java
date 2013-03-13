/**
 * 
 */
package boogiepants.display;

import junit.framework.TestCase;

/**
 * @author jstoner
 *
 */
public class BoogiepantsDisplayWindowTest extends TestCase {

    private BoogiepantsDisplayWindow instance;

    static{
        BoogiepantsDisplayWindow.makeWindow();
    }
    
    public BoogiepantsDisplayWindowTest(String string) {
        super(string);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        Thread.sleep(5000);
        instance = BoogiepantsDisplayWindow.getInstance();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        instance.dispose();
    }

    /**
     * Test method for {@link boogiepants.display.BoogiepantsDisplayWindow#makeWindow()}.
     */
    public void testMakeWindow() {
        assertNotNull("failed to get BoogiepantsDisplayWindow", instance);
    }

    /**
     * Test method for {@link boogiepants.display.BoogiepantsDisplayWindow#toggleFullScreen()}.
     */
    public void testToggleFullScreen() {
        for(int i = 0; i<2; i++){
            try{
                instance.toggleFullScreen();
            }catch (Exception e){
                fail ("threw exception:\n"+ e.getStackTrace());
            }
        }
    }

}
