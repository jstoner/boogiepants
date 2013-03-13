Boogiepants requirements:

Mac--OSX 10.5 (Leopard)
a Nintendo Wiimote
a Nintendo Nunchuck
patience. This is a prototype. It mostly works. 

To install boogiepants:

On MAC OS X:
1. Open the install file;
2. Drag the boogiepants application to the Applications folder;
3. Drag the boogiepants folder to the user's home folder (/Users/[username])

separately, install Pure Data. Find the most recent appropriate 
release of pd-extended at 

http://puredata.info/downloads

Download and install it.

To run boogiepants:

1. ensure the wiimote and nunchuck are connected;
2. ensure Bluetooth is active on your machine: on the Mac, go into System 
Preferences. Under Hardware, click Bluetooth. Make sure the checkbox 
indicating 'On' is checked;
3. start the boogiepants application. There will be a 'boogiepants' menu item at
the top of the screen, and a Java icon in the Dock, and nothing else. Yes, it 
started. Did I mention this is a prototype?;
4. press the 1 and 2 buttons on the wiimote at the same time to establish the 
wireless connection. This is a bumpy process. It may fail the first couple times.
If it doesn't start immediately when you do this, or if the animation seems frozen,
kill the application and go back to step 2;
5. start Pd-extended;
6. in pd-extended, open the file in the boogiepants folder at 
boogiepants/pd/oscserv.pd. 