import sys
from hip_pos_display import *

if sys.platform=='darwin':
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

wd = WiiRemoteDiscovery.new().init()

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
       objc.selector(None, selector="irPointMovedX:",signature="v@:ff@", isRequired=False),
       objc.selector(None, selector="rawIRData:",signature="v@:[4{IRData=iii}]@", isRequired=False),
       objc.selector(None, selector="buttonChanged:",signature="v@:Sc@", isRequired=False),
       objc.selector(None, selector="accelerationChanged:",signature="v@:SCCC@", isRequired=False),
       objc.selector(None, selector="joyStickChanged:",signature="v@:SCC@", isRequired=False),
       objc.selector(None, selector="analogButtonChanged:",signature="v@:SI@", isRequired=False),
       objc.selector(None, selector="wiiRemoteDisconnected:",signature="v@:@", isRequired=False),
   ])

wii = None

class wii_remote_discovery_delegate(NSObject):
   def WiiRemoteDiscovered_(self, wiiRemote):
       global wii
       wii = wiiRemote.retain()
       print "discovered"
       delegate = wii_remote_delegate.new()
       wii.setDelegate_(delegate)
       print "set delegate"
       wii.setLEDEnabled1_enabled2_enabled3_enabled4_(True, False, True, False)
       wii.setMotionSensorEnabled_(True)
       wd.wii_remote_discovery.stop()
   def WiiRemoteDiscoveryError_(self, returnCode):
       print "not discovered, error ", returnCode
       AppHelper.stopEventLoop()

class wii_remote_delegate(NSObject):

   def irPointMovedX_Y_wiiRemote_(self, px, py, wiiRemote):
       print 'irPointMovedX:Y:wiiRemote:'

   def rawIRData_wiiRemote_(self, irData, wiiRemote):
       print 'rawIRData:wiiRemote:'

   def buttonChanged_isPressed_wiiRemote_(self, type, isPressed, wiiRemote):
       print 'buttonChanged:isPressed:wiiRemote:'
       if 'button' not in self.__dict__:
          self.button={}
       self.button[(wiiRemote,type)]=pushed

   def accelerationChanged_accX_accY_accZ_wiiRemote_(self, type, accX, accY, accZ, wiiRemote):
       print 'accelerationChanged:accX:accY:accZ:wiiRemote:'
       if 'acc' not in self.__dict__:
          self.acc={}
       self.acc[(wiiRemote,type)]=(accX, accY, accZ, type)

   def joyStickChanged_tiltX_tiltY_wiiRemote_(self, type, tiltX, tiltY, wiiRemote):
       print 'joyStickChanged:tiltX:tiltY:wiiRemote:'

   def analogButtonChanged_amount_wiiRemote_(self, type, press, wiiRemote):
       print 'analogButtonChanged:amount:wiiRemote:'
       

   def wiiRemoteDisconnected_(self, device):
       print 'wiiRemoteDisconnected:'


x = wii_remote_discovery_delegate.new()
wd.setDelegate_(x)
wd.start()

try:
    AppHelper.runConsoleEventLoop(installInterrupt=True)
except KeyboardInterrupt:
    print "Ctrl-C received, quitting."
    AppHelper.stopEventLoop()


s=hip_pos_display()
