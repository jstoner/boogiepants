from hip_pos_display import *

if sys.platform=='darwin':
    import sys
    sys.path.extend(['/System/Library/Frameworks/Python.framework/Versions/2.5/lib/python2.5/plat-darwin',
                     '/System/Library/Frameworks/Python.framework/Versions/2.5/lib/python2.5/plat-mac',
                     '/System/Library/Frameworks/Python.framework/Versions/2.5/Extras/lib/python',
                     '/System/Library/Frameworks/Python.framework/Versions/2.5/Extras/lib/python/PyObjC',
                     '/Users/jstoner/Documents/projects/boogiepants/boogiepants/build/Release/boogiepants.app/Contents/MacOS',
                     '/Users/jstoner/Documents/projects/boogiepants/boogiepants/build/Release/boogiepants.app/Contents/Resources',
                     '/Users/jstoner/Documents/projects/boogiepants/WiiRemoteFramework/build/Release',
                     '/Library/Frameworks'])

import objc
objc.loadBundle("WiiRemote", globals(), bundle_path="WiiRemote.framework")


from Foundation import *
from AppKit import *
from PyObjCTools import AppHelper

IRData = objc.createStructType("IRData", "iii", ["x", "y", "s"])

WiiRemoteDiscoveryDelegate = objc.informal_protocol(
   "WiiRemoteDiscoveryDelegate",
   [
       objc.selector(None,selector="WiiRemoteDiscovered:",signature="v@:@",isRequired=0),
       objc.selector(None, selector="WiiRemoteDiscoveryError:",signature="v@:i",isRequired=0)
   ])

WiiRemoteDelegate = objc.informal_protocol(
   "WiiRemoteDelegate",
   [
       objc.selector(None, selector="irPointMovedX:Y:wiiRemote:",signature="v@:ff@", isRequired=False),
       objc.selector(None, selector="rawIRData:wiiRemote:",signature="v@:[4{IRData=iii}]@", isRequired=False),
       objc.selector(None,selector="buttonChanged:isPressed:wiiRemote:", signature="v@:Sc@",isRequired=False),
       objc.selector(None,selector="accelerationChanged:accX:accY:accZ:wiiRemote:",signature="v@:SCCC@", isRequired=False),
       objc.selector(None,selector="joyStickChanged:tiltX:tiltY:wiiRemote:",signature="v@:SCC@", isRequired=False),
       objc.selector(None,selector="analogButtonChanged:amount:wiiRemote:", signature="v@:SI@",isRequired=False),
       objc.selector(None, selector="wiiRemoteDisconnected:",signature="v@:@", isRequired=False),
   ])

wii = None
wd = WiiRemoteDiscovery.new().init()

class wii_remote_discovery_delegate(NSObject):
   def WiiRemoteDiscovered_(self, wiimote):
       global wii
       wii = wiimote.retain()
       print "discovered"
       delegate = wii_remote_delegate.new()
       wii.setDelegate_(delegate)
       print "set delegate"
       wii.setLEDEnabled1_enabled2_enabled3_enabled4_(True, False, True, False)
       wii.setMotionSensorEnabled_(True)
       wd.wii_remote_discovery.stop()
   def WiiRemoteDiscoveryError_(self, code):
       print "not discovered, error ", code


d = wii_remote_discovery_delegate.new()
wd.setDelegate_(d)
wd.start()

