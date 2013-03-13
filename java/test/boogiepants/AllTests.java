package boogiepants;

import boogiepants.display.BoogiepantsDisplayWindowTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for boogiepants");
        //$JUnit-BEGIN$
        suite.addTest(new BoogiepantsDisplayWindowTest("testMakeWindow"));
        suite.addTest(new BoogiepantsDisplayWindowTest("testToggleFullScreen"));
        //$JUnit-END$
        return suite;
    }

}
