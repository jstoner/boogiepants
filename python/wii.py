
import sys

if sys.platform=='darwin':
    sys.path.extend(['/System/Library/Frameworks/Python.framework/Versions/2.5/lib/python2.5/plat-darwin',
                     '/System/Library/Frameworks/Python.framework/Versions/2.5/lib/python2.5/plat-mac',
                     '/System/Library/Frameworks/Python.framework/Versions/2.5/Extras/lib/python',
                     '/System/Library/Frameworks/Python.framework/Versions/2.5/Extras/lib/python/PyObjC',
                     '/Users/jstoner/Documents/projects/boogiepants/boogiepants/build/Release/boogiepants.app/Contents/MacOS',
                     '/Users/jstoner/Documents/projects/boogiepants/boogiepants/build/Release/boogiepants.app/Contents/Resources',
                     '/Users/jstoner/Documents/projects/boogiepants/WiiRemoteFramework/build/Release',
                     '/Library/Frameworks'])

    unused=['/System/Library/Frameworks/Python.framework/Versions/2.5/lib/python2.5/plat-mac/lib-scriptpackages',
            '/System/Library/Frameworks/Python.framework/Versions/2.5/lib/python2.5/lib-tk',
            '/System/Library/Frameworks/Python.framework/Versions/2.5/lib/python2.5/lib-dynload',
            '/Library/Python/2.5/site-packages']

    def _load(name, g):
        import objc
        objc.loadBundle(name, g, bundle_path='WiiRemote.framework')

    _load(__name__,  globals())
    from Foundation import *
    from AppKit import *
    from hip_pos_display import *

    WiiRemoteDiscoveryDelegate = objc.informal_protocol(
        "WiiRemoteDiscoveryDelegate",
        [
            # (void) WiiRemoteDiscovered:(WiiRemote*)wiimote;
            objc.selector(
                None,
                selector='WiiRemoteDiscovered:',
                signature='v@:@',
                isRequired=0,
                ),
            # (void) WiiRemoteDiscoveryError:(int)code;
            objc.selector(
                None,
                selector='WiiRemoteDiscoveryError:',
                signature='v@:i',
                isRequired=0,
                ),
        ]
    )
    class wii_remote_discovery_delegate(NSObject):
        def WiiRemoteDiscovered_(self, wiiRemote):
            self.wiiRemote=wiiRemote
            print "discovered"
        def WiiRemoteDiscoveryError_(self, returnCode):
            print "not discovered"
    wd = WiiRemoteDiscovery.new()
    x=wii_remote_discovery_delegate.new()
    wd.setDelegate_(x)
    wd.start()

    s=hip_pos_display()
