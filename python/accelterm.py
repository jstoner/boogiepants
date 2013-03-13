appTemplate.py                                                                                      000755  000765  000024  00000007370 10777462424 014562  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         """     simpleOSC 0.2
    ixi software - July, 2006
    www.ixi-software.net

    simple API  for the Open SoundControl for Python (by Daniel Holth, Clinton
    McChesney --> pyKit.tar.gz file at http://wiretap.stetson.edu)
    Documentation at http://wiretap.stetson.edu/docs/pyKit/

    The main aim of this implementation is to provide with a simple way to deal
    with the OSC implementation that makes life easier to those who don't have
    understanding of sockets or programming. This would not be on your screen without the help
    of Daniel Holth.

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    Thanks for the support to Buchsenhausen, Innsbruck, Austria.
"""


import osc

# just importing the osc module creates under the hood an outbound socket and the callback manager
# (osc addressManager). But we dont have to worry about that.


def myTest():
    """ a simple function that creates the necesary sockets and enters an enless
        loop sending and receiving OSC
    """
    osc.init()
    
##    osc.createListener() # this defaults to port 9001 as well
    osc.listen('127.0.0.1', 9001)

    # bind addresses to functions -> printStuff() function will be triggered everytime a
    # "/test" labeled message arrives
    osc.bind(printStuff, "/test")

    import time # in this example we will have a small delay in the while loop

    print 'ready to receive and send osc messages ...'
    
    while 1:
##        osc.sendMsg("/test", [444], "127.0.0.1", 9000) # send normal msg to a specific ip and port
        osc.sendMsg("/test", [444]) # !! it sends by default to localhost ip "127.0.0.1" and port 9000 
        # create and send a bundle
        bundle = osc.createBundle()
        osc.appendToBundle(bundle, "/test/bndlprt1", [1, 2, 3]) # 1st message appent to bundle
        osc.appendToBundle(bundle, "/test/bndlprt2", [4, 5, 6]) # 2nd message appent to bundle
##        osc.sendBundle(bundle, "127.0.0.1", 9000) # send it to a specific ip and port

        osc.sendBundle(bundle) # !! it sends by default to localhost ip "127.0.0.1" and port 9000 
        #osc.getOSC(inSocket) # listen to incomming OSC in this socket
        time.sleep(0.5) # you don't need this, but otherwise we're sending as fast as possible.
        

    osc.dontListen() # finally close the connection bfore exiting or program


""" Below some functions dealing with OSC messages RECEIVED to Python.

    Here you can set all the responders you need to deal with the incoming
    OSC messages. You need them to the callBackManager instance in the main
    loop and associate them to the desired OSC addreses like this for example
    addressManager.add(printStuff, "/print")
    it would associate the /print tagged messages with the printStuff() function
    defined in this module. You can have several callback functions in a separated module if you wish
"""

def printStuff(*msg):
    """deals with "print" tagged OSC addresses """

    print "printing in the printStuff function ", msg
    print "the oscaddress is ", msg[0][0]
    print "the value is ", msg[0][2]




if __name__ == '__main__': myTest()














                                                                                                                                                                                                                                                                        bp_display.py                                                                                       000644  000765  000024  00000010604 11005254731 014404  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         from visual.controls import *
from math import *
from bputil import *
from random import *
from marks_control import *
from sphere_line import *

class bp_display(object):
    def __init__(self, marks):
        self.scene = display(title='boogiepants',
                             width=600, height=600,
                             background=(1,1,1), forward=(0,1,-.1))
        rate(50)
        self.hip_pos=hip_pos_display()
        self.all_disp=self.process_marks(marks)
        self.active_disp=meth_dict({}, 'show', 'hide')

    def process_marks(self, marks):
        disp={}
        for i in marks:
            disp[i]=disp_map(marks[i])
            disp[i].hide()
        return disp
    
def disp_map(busobj):
    ''' returns an object of the class bp_display.disp_<busobj's class>
        initialized with busobj '''
    cl_name=busobj.__class__.__name__
    disp_class= "disp_"+cl_name
    cl=globals()[disp_class]
    ret=cl.__new__(cl, [busobj])
    cl.__init__(ret, busobj)
    return ret

class hip_pos_display(object):
    '''creates spherical-coordinate grid pattern
        assumes scene is already created'''
    def __init__(self):
        self.f=frame()
        self.active_lines={}
        for i in range(0, 91, 10):
            ring(frame=self.f, pos=(0,0,-dcos(i)*10), radius=dsin(i)*10,
                 thickness=.04, axis=(0,0,1), color=(0.8,0.8,0.8))
        for i in range(0,360, 10):
            c=[]
            for j in range(0,91,5):
                c.append(spherical_coord(10, j, i).sph2xyz())            
            r=curve(frame=self.f, color=(0.8,0.8,0.8), pos=c)

class disp_object(object):
    def show(self):
        self.line.show()

    def hide(self):
        self.line.hide()

    def annotation_point(self):
        return self.line.annotation_point()

class disp_list_mark(list, disp_object):
    def __init__(self, modobj):
        for i in modobj:
            self.append(disp_map(i))

    def show(self):
        for i in self:
            i.show()

    def hide(self):
        for i in self:
            i.hide()

class disp_longitude(disp_object):
    def __init__(self, modobj):
        self.line=dir_marked_sphere_line(spherical_coord(10, 0, modobj.angle),
                                         spherical_coord(10, 90, modobj.angle),
                                         modobj.direction)

class disp_segment(disp_object):
    def __init__(self, modobj):
        self.line=dir_marked_sphere_line(spherical_coord(10, modobj.start.phi, modobj.start.theta),
                               spherical_coord(10, modobj.end.phi, modobj.end.theta),
                               modobj.direction)

from visual import *
class disp_series_mark(disp_list_mark):
    def __init__(self, *modobj):
        disp_list_mark.__init__(self, *modobj)
        self.arrows=[]
        print self
        for i in range(0,len(self)-1):
            st=self[i].annotation_point().sph2xyz()
            end=self[i+1].annotation_point().sph2xyz()
            arr=arrow(pos=(st),axis=subtract(end, st), shaftwidth=.2, color=(1,0,0))
            self.arrows.append(arr)
            
    def show(self):
        for i in self.arrows:
            i.visible=1
        disp_list_mark.show(self)

    def hide(self):
        for i in self.arrows:
            i.visible=0
        disp_list_mark.hide(self)


class meth_dict(dict):
    def __init__(self, dct, add_meth, del_meth):
        self.add_meth=add_meth
        self.del_meth=del_meth
        for i in dct.values():
            i.__getattribute__(self.add_meth)()
        dict.__init__(self, dct)

    def __delitem__(self, i):
        self[i].__getattribute__(self.del_meth)()
        dict.__delitem__(self, i)

    def __setitem__(self, i, x):
        x.__getattribute__(self.add_meth)()
        if i in self:
            self[i].__getattribute__(self.del_meth)()
        dict.__setitem__(self, i, x)

    def pop(self, i):
        self[i].__getattribute__(self.del_meth)()
        return dict.pop(self, i)
        
    def clear(self):
        for i in self:
            self[i].__getattribute__(self.del_meth)()
        dict.clear(self)    

    def update(self, d):
        for i in d:
            if i in self:
                self[i].__getattribute__(self.del_meth)()
            d[i].__getattribute__(self.add_meth)()
        dict.update(self, d)

                                                                                                                            ./._bputil.py                                                                                       000755  000765  000024  00000000305 10736567473 014153  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                             Mac OS X            	   2   �      �                                      ATTR �   �   �   -                  �   -  com.apple.quarantine q/0000;477adfe2;Firefox;|org.mozilla.firefox                                                                                                                                                                                                                                                                                                                            bputil.py                                                                                           000755  000765  000024  00000004362 10736567473 013610  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         
from  math import *

def dsin(angle):
    '''sin defined in degrees, not radians'''
    return sin(angle*pi/180)

def dcos(angle):
    '''cos defined in degrees, not radians'''
    return cos(angle*pi/180)

class spherical_coord(object):
    def __init__(self, rho, phi, theta):
        self.rho, self.phi, self.theta = rho, phi, theta
    def sph2xyz(self):
        ''' converts spherical cooordinates (in degrees) to cartesian 3d (xyz)
            inverted--(1,0,0)==(0,0,1) -because we are always measuring degrees from the
            negative z axis '''
        return ((self.rho*dsin(self.phi)*dcos(self.theta),
                 self.rho*dsin(self.phi)*dsin(self.theta),
                 -self.rho*dcos(self.phi)))

# from Python Cookbook
def Enum(*names):
   ##assert names, "Empty enums are not supported" # <- Don't like empty enums? Uncomment!

   class EnumClass(object):
      __slots__ = names
      def __iter__(self):        return iter(constants)
      def __len__(self):         return len(constants)
      def __getitem__(self, i):  return constants[i]
      def __repr__(self):        return 'Enum' + str(names)
      def __str__(self):         return 'enum ' + str(constants)

   class EnumValue(object):
      __slots__ = ('__value')
      def __init__(self, value): self.__value = value
      Value = property(lambda self: self.__value)
      EnumType = property(lambda self: EnumType)
      def __hash__(self):        return hash(self.__value)
      def __cmp__(self, other):
         # C fans might want to remove the following assertion
         # to make all enums comparable by ordinal value {;))
         assert self.EnumType is other.EnumType, "Only values from the same enum are comparable"
         return cmp(self.__value, other.__value)
      def __invert__(self):      return constants[maximum - self.__value]
      def __nonzero__(self):     return bool(self.__value)
      def __repr__(self):        return str(names[self.__value])

   maximum = len(names) - 1
   constants = [None] * len(names)
   for i, each in enumerate(names):
      val = EnumValue(i)
      setattr(EnumClass, each, val)
      constants[i] = val
   constants = tuple(constants)
   EnumType = EnumClass()
   return EnumType
                                                                                                                                                                                                                                                                              controller.py                                                                                       000644  000765  000024  00000005343 11543551336 014455  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         from mark import *
from bp_display import *
from marks import marks

class boogiepants(object):
    def __init__(self, mdict=marks):
        ''' load sets,  set them up for display, turn them off initially '''
        self.all_marks=mdict
        self.active_marks={}
        self.view =bp_display(self.all_marks) 
        self.active_disp=self.view.active_disp
        self.all_disp=self.view.all_disp
        self.mark_buttons={}
        self.ctrl_window = controls.controls(x=600, y=0,
                                    height=50+50*
                                           len(self.all_marks),
                                    width=350)
        for i, j in enumerate(self.all_marks.keys()):
            act= self.set_button(j)
            self.mark_buttons[j]=button(text=j+' on', pos=(0,25*(i-1)),
                                      width=80, height=30,
                                      action=act)
            self.calibrate_button=button(pos=(0,-50), width=80, height=30,
                                         text='set',
                                         action = lambda: self.calibrate())
        while(True):
            rate(20)
            self.ctrl_window.interact()

    def set_on(self, set_name):
        ''' activate a set '''
        self.active_disp[set_name]=self.all_disp[set_name]
        self.active_marks[set_name]=self.all_marks[set_name]

    def set_off(self, set_name):
        ''' deactivate a set '''
        del self.active_disp[set_name]
        del self.active_marks[set_name]

    def detect_event(self):
        ''' detect whether it's time to initiate an event '''
        for i in self.active_marks:
            self.active_marks[i].cross()
        
    def set_button(self, label):
        return lambda: self.sb(label)

    def sb(self, label):
        if self.mark_buttons[label].text[-2:]=='on':
            self.mark_buttons[label].text=self.mark_buttons[label].text[:-2]+'off'
            self.set_on(label)
        else:
            self.mark_buttons[label].text=self.mark_buttons[label].text[:-3]+'on'
            self.set_off(label)

    def calibrate(self):
        self.calibrate_button.text='reset'


                
class composite_list(object):
    def __init__(self, items, methods):
        for i in methods:
            new.instancemethod( )
        list.__init__(self, items)
    def call(self, f, *params):
        for i in self:
            f(*params)


class display_longitude():
    def __init__(longitude):
        self.line=dir_marked_sphere_line(spherical_coord(10, 0, longitude.angle),
                               spherical_coord(10, 90, longitude.angle),
                               longitude.direction())
    def show():
        self.line.show()

    def hide():
        self.line.hide()
                                                                                                                                                                                                                                                                                             controlstest.py                                                                                     000755  000765  000024  00000006564 10742510667 015050  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         from visual.controls import *

# Create "call-back" routines, routines that are called by the interact
# machinery when certain mouse events happen:

def setdir(direction): # called on button up events
    cube.dir = direction

def togglecubecolor(): # called on toggle switch flips
    if t1.value:
        cube.color = color.cyan
    else:
        cube.color = color.red

def cubecolor(value): # called on a menu choice
    cube.color = value
    if cube.color == color.red:
        t1.value = 0 # make toggle switch setting consistent with menu choice
    else:
        t1.value = 1
    
def setrate(obj): # called on slider drag events
    cuberate(obj.value) # value is min-max slider position
    if obj is s1:
        s2.value = s1.value # demonstrate coupling of the two sliders
    else:
        s1.value = s2.value

def cuberate(value):
    cube.dtheta = 2*value*pi/1e4

w = 350
display(x=w, y=0, width=w, height=w, range=1.5, forward=-vector(0,1,1), newzoom=1)
cube = box(color=color.red)

# In establishing the controls window, range=60 means what it usually means:
# (0,0) is in the center of the window, and (60,60) is the lower right corner.
# If range is not specified, the default is 100.
c = controls(x=0, y=0, width=w, height=w, range=60)

# Buttons have a "text" attribute (the button label) which can be read and set.
# Toggles have "text0" and "text1" attributes which can be read and set.
# Toggles and sliders have a "value" attribute (0/1, or location of indicator) which can be read and set.

# The pos attribute for buttons, toggles, and menus is the center of the control (like "box").
# The pos attribute for sliders is at one end, and axis points to the other end (like "cylinder").

# By default a control is created in the most recently created "controls" window, but you
# can change this by specifying "controls=..." when creating a button, toggle, slider, or menu.

# The Python construct "lambda: setdir(-1)" below passes the location of the setdir function
# to the interact machinery, which uses "apply" to call the function when an action
# is to be taken. This scheme ensures that the execution of the function takes place
# in the appropriate namespace context in the case of importing the controls module.

bl = button(pos=(-30,30), height=30, width=40, text='Left', action=lambda: setdir(-1))
br = button(pos=(30,30), height=30, width=40, text='Right', action=lambda: setdir(1))
s1 = slider(pos=(-15,-40), width=7, length=70, axis=(1,0.7,0), action=lambda: setrate(s1))
s2 = slider(pos=(-30,-50), width=7, length=50, axis=(0,1,0), action=lambda: setrate(s2))
t1 = toggle(pos=(40,-30), width=10, height=10, text0='Red', text1='Cyan', action=lambda: togglecubecolor())
m1 = menu(pos=(0,0,0), height=7, width=25, text='Options')

# After creating the menu heading, add menu items:
m1.items.append(('Left', lambda: setdir(-1))) # specify menu item title and action to perform
m1.items.append(('Right', lambda: setdir(1)))
m1.items.append(('---------',None)) # a dummy separator
m1.items.append(('Red', lambda: cubecolor(color.red)))
m1.items.append(('Cyan', lambda: cubecolor(color.cyan)))

s1.value = 70 # update the slider
setrate(s1) # set the rotation rate of the cube
setdir(-1) # set the rotation direction of the cube

while 1:
    rate(100)
    c.interact() # check for events, drive actions; must be executed repeatedly in a loop
    cube.rotate(axis=(0,1,0), angle=cube.dir*cube.dtheta)
       
                                                                                                                                            ./._hip_pos_display.py                                                                              000755  000765  000024  00000000305 10741331431 016015  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                             Mac OS X            	   2   �      �                                      ATTR �   �   �   -                  �   -  com.apple.quarantine q/0000;477adfe2;Firefox;|org.mozilla.firefox                                                                                                                                                                                                                                                                                                                            hip_pos_display.py                                                                                  000755  000765  000024  00000001627 10741331431 015453  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         from visual import *
from math import *
from bputil import *
from random import *

class hip_pos_display(object):
    def __init__(self):
        self.scene = display(title='boogiepants',\
                     width=600, height=600,\
                     background=(1,1,1), forward=(0,1,-.1),)
        self.f=frame()
        for i in range(0, 91, 10):
            ring(frame=self.f, pos=(0,0,-dcos(i)*10), radius=dsin(i)*10, thickness=.04,
                 axis=(0,0,1), color=(0.8,0.8,0.8))
            
        for i in range(0,360, 10):
            c=[]
            for j in range(0,91,5):
                c.append(spherical_coord(10, j, i).sph2xyz())            
            r=curve(frame=self.f, color=(0.8,0.8,0.8), pos=c)
        self.marks=[]
    def add_mark(self, mark):
        pass
    def randomize_background(self):
        self.scene.background=(random(), random(), random())

                                                                                                         ./._mark.py                                                                                         000755  000765  000024  00000000305 11543551217 013570  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                             Mac OS X            	   2   �      �                                      ATTR 0�v   �   �   -                  �   -  com.apple.quarantine q/0000;477adfe2;Firefox;|org.mozilla.firefox                                                                                                                                                                                                                                                                                                                            mark.py                                                                                             000755  000765  000024  00000015766 11543551217 013237  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         from bputil import *
from time import *
from threading import Thread

''' This is basically the model objects module. Say that three times fast.
    I knew you could.'''

class cc(list):
    ''' this is basically a callable composite for callable objects.
        it's useful to contain a bunch of closures to be called in sequence
        '''
    def __init__(self, *l):
        list.__init__(self, l)
    def __call__(self):
        for i in self:
            i()

def p(f, *params):
    ''' packages a closure of f with some params. Useful with cc.
        reduces typing.'''
    return lambda p=params: f(*params)

class invoker(object):
    '''abstract superclass that provides all event-model classes with
    framework to participate as an invoker'''
    def __init__(self, listeners=None):
        if listeners:
            self.listeners=cc(*listeners)
        else:
            self.listeners=None

    def invoke_listeners(self):
        if self.listeners: self.listeners()
        
                
class mark(invoker):
    '''abstracted superclass for all boundaries which generate events when crossed'''

    dirs = Enum('LEFT', 'RIGHT', 'BOTH')
    
    def __init__(self, direction, listeners):
        self.set_direction(direction)
        invoker.__init__(self, listeners)

    def set_direction(self, dir):
        if dir in mark.dirs:
           self.direction=dir
        else:
            raise ValueError('get direction from mark.dirs')

    def cross(self):
        self.invoke_listeners()

    def cross_zero(self, prevstep, now):
        '''detect crossing the zero angle'''
        if abs(now.theta - prevstep.theta)>350:
            if now.theta>350:
                now=spherical_coord(now.rho, now.phi, now.theta-360)
            else:
                prevstep=spherical_coord(prevstep.rho, prevstep.phi, prevstep.theta-360)
        return (prevstep, now)

class longitude(mark):
    '''boundary along a longitudinal line'''
    def __init__(self, dir, angle, *listeners):
        self.angle=angle
        mark.__init__(self, dir, listeners)

    def set_angle(self, angle):
        if angle >= 0 and angle < 360:
            self.angle=angle
        else:
            raise ValueError('out of range')

    def cross(self, prevstep, now):
        prevstep, now= self.cross_zero(prevstep, now)
        # if this longitude is between the previous angle and the current angle
        if abs(prevstep.theta - self.angle) + abs(now.theta - self.angle)== \
           abs(now.theta - prevstep.theta):
            if self.direction==mark.dirs.BOTH or \
               (self.direction==mark.dirs.LEFT and now.theta <= self.angle) or \
               (self.direction==mark.dirs.RIGHT and now.theta >= self.angle):
                self.invoke_listeners()
                return True
        return False
    
class segment(mark):
    def __init__(self, dir, start, end, *listeners):
        self.set_start(start)
        self.set_end(end)
        self.bx=self.end.phi -self.start.phi
        self.by=self.end.theta - self.start.theta
        self.angle=atan2(self.by, self.bx)
        self.listeners=listeners
        mark.__init__(self, dir, listeners)
        
    def set_start(self, start):
        self.start = start

    def set_end(self, end):
        self.end = end

    def cross(self, prevstep, now):
        
        prevstep, now= self.cross_zero(prevstep, now)

        dx = now.phi - prevstep.phi
        dy = now.theta - prevstep.theta
 
        b_dot_d_perp = self.bx * dy - self.by * dx
 
        if(b_dot_d_perp != 0):
 
            cx = prevstep.phi - self.start.phi
            cy = prevstep.theta - self.start.theta
     
            t = (cx * dy - cy * dx) / b_dot_d_perp
            if(t >= 0 and t <= 1):
     
                u = (cx * self.by - cy * self.bx) / b_dot_d_perp
                if(u >= 0 and u <= 1):
                    if self.direction==mark.dirs.BOTH:
                        self.invoke_listeners()
                        return True
                    else:
                        crossangle=atan2(dy,dx)
                        if crossangle<=self.angle and self.direction==mark.dirs.LEFT or \
                           crossangle>self.angle and self.direction==mark.dirs.RIGHT:
                            self.invoke_listeners()
                            return True
        return False
                    #return new Vector2f(self.start.phi+t*self.bx, self.start.theta+t*self.by);

class series_mark(list):
    ''' packages marks so crossing is only detected in the sequence... you
        have to cross mark 0 before mark 1 will be detected, and so forth,
        cylically. Useful to make hips go around a central point'''
    def __init__(self, *marks):
        self.index=0
        list.__init__(self, marks)
        
    def cross(self, prevstep, now):
        if self[self.index].cross(prevstep, now):
            self.index+=1
            if self.index == len(self):
                self.index=0
            return True
        return False

class list_mark(list):
    def __init__(self, *l):
        list.__init__(self, l)
        
    def cross(self, prevstep, now):
        for i in self:
            if i.cross(prevstep, now):
                return True
        return False

class rhythm_average(invoker):
    '''for a stream of events, averages their period in time.
    interval defines how long of a period of time to keep the events.
    initial_period sets an initial periodicity of events, in seconds.'''
    def __init__(self, interval, initial_period, *listeners):
        self.outthread=Thread(target=self.out_thread)
        self.alive=True
        self.interval=interval
        self.events=[]
        t=time()
        self.per=initial_period
        rng=range(int(interval / initial_period))
        rng.reverse()
        for i in rng:
            self.events.append(t-initial_period * i)
        invoker.__init__(self, listeners)

    def in_event(self):
        t=time()
        self.events.append(t)

    def set_interval(self, interval):
        self.interval=interval
        
    def period(self):
        i=len(self.events)-1
        t=time()
        while i > 0 and self.events[i] > t - self.interval:
           i -= 1
        numevents=len(self.events)-i-1
        if numevents>0:
            self.per=(self.events[-1] - self.events[i]) / numevents
        del self.events[0:len(self.events)-numevents-1]
        return self.per

    def __str__(self):
        s=''
        i=len(self.events)-1
        t=time()
        while i > 0 and self.events[i] > t - self.interval:
            s+=str(t-self.events[i])+' '
            i -= 1
        s+=';'+str(self.period())
        return s
    
    def start(self):
        self.outthread.start()

    def stop(self):
        self.alive=False
        
    def out_thread(self):
        while self.alive:
            p=self.period()
            self.invoke_listeners()
            sleep(p)
          marks.py                                                                                            000644  000765  000024  00000001774 11543522651 013411  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         from bputil import *
from mark import *

ra=rhythm_average(10, .5, None)

marks={'three marks':list_mark(longitude(mark.dirs.LEFT,0),
                               longitude(mark.dirs.RIGHT, 120),
                               longitude(mark.dirs.BOTH, 240)), 
       'ordered marks':
        series_mark(longitude(mark.dirs.RIGHT,60),
                               longitude(mark.dirs.RIGHT, 180),
                               longitude(mark.dirs.RIGHT, 300)),
       'rhthym average':
        series_mark(longitude(mark.dirs.RIGHT,60,
                              p(rhythm_average.in_event, ra)),
                              longitude(mark.dirs.RIGHT, 180),
                              longitude(mark.dirs.RIGHT, 300)),
      'hip bop':list_mark(segment(mark.dirs.LEFT, spherical_coord(1, 40,75),
                                  spherical_coord(1, 60,35)),
                          segment(mark.dirs.LEFT, spherical_coord(1, 40,285),
                                  spherical_coord(1, 60,325)))
      }
    marks_control.py                                                                                    000644  000765  000024  00000001240 10751477221 015137  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         

class marks_control(object):
    def __init__(self):
        import marks
        self.stored_mark_sets=marks.marks
        self.active_mark_sets={}

    def activate_mark_set(self, set_name):
        self.active_mark_sets[set_name]=self.stored_mark_sets[set_name]

    def deactivate_mark_set(self, set_name):
        del self.active_mark_sets[set_name]

    def get_mark_set(self, set_name):
        return self.stored_mark_sets[set_name]

    def mark_set_keys(self):
        return self.stored_mark_sets.keys()

    def cross_marks(self, prevstep, now):
        for i in self.active_mark_sets.values():
            for j in i:
                j.cross(prevstep, now)
                                                                                                                                                                                                                                                                                                                                                                ./._pd_patches.zip                                                                                  000644  000765  000024  00000000305 11030352100 015074  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                             Mac OS X            	   2   �      �                                      ATTR`��   �   �   -                  �   -  com.apple.quarantine q/0000;4861d440;Firefox;|org.mozilla.firefox                                                                                                                                                                                                                                                                                                                            pd_patches.zip                                                                                      000644  000765  000024  00000054443 11030352100 014536  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         PK    �j2���E  ZV     Note Transposer Delay.pd��]�۸����*���d&�?*W{N|��nžH*�5�K�J��;�ן�A� h��;^��F| P���T�^�^�,�QV�0y!���������B�U��� �U��?~��yx[���P�W�R���̓�cJ��L>�z���(ZTB�%�G?����6�g��v�O��뛅;RJў�Y�R&���W��+1z���oQk��w��K[	��h���O�S��}}����)���2b�(�~z���!\��
Rn�ks�ZY!@���C�v��R	;�{�y�� ��W`|�������Z<��A/��FD���fX������'��T�A��B�R�/���A�����|�l�|Gn�"yn���]v���]d>�p����ơ�~A&����z�6�!{(>�J&*�D/9�F͢�i�뛨��)4�����rc��!x�M4^��I,�GY1���Vu)����T������qw���b�G)�4� � =��K�7Qf��/=;��=?8?�q[��%�N�9�u����z�Y͗=��jiav�.-̪\��e_Zc����k$����P���u�7(En��u�I��qθ��ތ�e��f����F�Io��	�({׼�͙/�ټ�2R<�㍑7Ѭ��V�U�j��#��0�Ų��Ά���_�l��¬�_[��94O��)�p��"zR
z2�z:ɃP�B���Ps���pW�w����,᳟1���69r�9녊��g��3ϙ�F�ΛU��>T[�Ո~PԖ��h�p���R/��i4����4��]yW͞��@̪\uk��^���|��z'���i�P�g�)�*�
h6�J�h4h�Z�Rb������L8)U���î�*�j�B�{WLD�M�?O��n�+����*�jd�L5��+��tR5I5��TD��G~Ɓqm��M��MUO�U)�.(ER�S�.�j�U���X�T-�j���BU&z�U�MV���I5]V��I5��$�<��T����FE����&�DI��������S�M��M���,Ri��(K|�j܊�j���j�I�
��
C��%V+<?�@dB��F�q"��ܕX�UJͳ��P��T�Z$�06�;�"�+Iդ�\%U�*՘�?�jn��M�y��R�ڔy�d�&����Vquָz.��.vqI�*iR�T�|Q���Cwi�VJ�w~��o�(��J\��'��n���1���$�Ȇ�QɌD��K40Q�c��ڎb{Z��v�=.D)��f����PK�Z�*�J*�]�Jk��:g�o$��E����ׯ����7���?}���/_?��˗��jA�FoL*bw%v͡~[���K�mR��[��	۴�����w��h�H�3�gD����i�k��@j�{hEI��������E�e���d�_�\���ĸ�L�͒6�ÍH?����ph�y��Ź���=��a�1]a���{��>�I�7ڤl!�^�^�*^G aB-�ԓ:�5�u@
>��ʢJ�2À��)[\��;���%���Cn����!����{�*�mr�#�e��dV�=����(a#����b>:J��^�c�%�z�z�������;M�v��̗�q�]�֫�%S�	���=P�>с�{.r.Z:P�\�"(��.�>����r>5��\�juw�4��O�^&c�b�i���k�#��\\rg�3�"w��3�8�Ǌ���x��dRqY�e�A�+e=��эv7�MQ��:�-=Ǖ����{\J�$�Sʋ�j�T֓�进�6��e�{.r.�Csw ��z\�}t?���Id�A������j�Z���dl��"���1tL�sq)�8�i�6�d}N~�,����t��Z)�Nಞ����D���Ghd佬�u�z�/L���e=�RK�-�	9��)s�T֓=w�����(��s�s�N�M�bIyS$�>��y:����&�>���N��*��'�W��Z����.�f����Ͽ��D|� ���O_�.{�hJ=_R������/�E�֮�ՙvU g���( ֞/�S��z�(�T�X�8j.���?2e��AY�@�q�>5��D�+d����v:i�1 �~��&�E9�s{Z�υ�����QeY���{C��Cj��Y�ߜy� Ps+��:4�&�in_��'l�� ���������[�Ԇ$i�Xރ �xU�
q�T��l�M�\h��`у�1z��`Sb���ޖ0��O��9�i@�7��R{;���f:t�Z��EWua)K�frU�O��,(D����k�_��<+G������y��[�<�OXS��A���W��j<���|�J�J��s3����j5ށ�uXYR潻�s��RWC���YB��zY;���h�:\������T�t*�}���'V�m9n��9>���S8�<�� ��jBu\��b��Mu���<Gs�O�����-�j'!T4�T�e��2@���!�s%CU&���Ɲ:��	��ldB��r�;���l�3�*�2T�*:ˉ[3�aq,�J��cl�\λ1���T��a�H�VD�%(�Aq�a��b)�w1��2�`71zCC�*�!�y�ʬ����۾�7\���Cw�Ò�y�����o��c!z^�}�ڄEuI�[3�mӜ�
OM���W�{%~�^[�e��m%&�C�k.ai@.�HO��G�J�҈�9,\��}�MX�0u"�W#���M��~���q����q��1�l:�k�lala���h��=y�>Q<n7�=���C���rN;.�8���C<K�>!�)��f;�]F��!�x�p��{,zJ:�f�"����X*�!���U��yb`x�c`�eյ��Q�:�;*&Dˠ�UtB��šb���Q����-"�s��CE�^�N�`Մ�%Ո�F
��T�l�Z���&�Ɏ�U����ԋH*�&�M��+^k�X���H8���up@��P9�QV�"m��j���qeCu�����{(��E��4ц�"U��*�*a%�JeJ7�{�T	�,3j%��VrX�a%��VrX�L��$�a���9�VrX�a%��VrX�a%��VrX�a%��VrX�a%��VrXɥ��VrX�a%��VrX�ae2�VrX�a%��VrX�a%��L�p��ae(rX�a%��VrX�a%��VrX�a%��VrX�a%��VrX�a%��VrX�a%��VrX�a%��VrX�a%��z�<U�%UV���x)�V�pT�Q%G�UrT�Q%3�rs(G����sT�Q%G�UrT�Q%G�UrT�Q%G�UrT�Q%G�U� �*#��J��R%�vUrT�Q%G�UrT�Q%G�UrT�Q%G�UrT�Q%G�Ur��Q%G�UrT�Q%G�UrT�Q%G�UrT�Q%G�UrT�Q%G�UrT�Q%G�UrT�Q%G�UrT�Q%G�UrT�Qe^�Rj���W���~����������<  ��sG�g��\� `�>c�ݥ~y��l{����x��㫇���7��死R��$��Dӡ��3s۝ꃨ���4��������۰?����Ar���߼�ReW����7�uMnj.�s�_��������:��;6�������ŪA���A�� g�[G�����S9to���;5x~�.n���-݉B�x�����??�Ωiv�Q�9���'j�o�����-��p���IJO����om?`4nF����H�ty�{w=��zh6��(>|�\Ӹ2'�7���gj�+�3}���<���\6?��Ю	��?.�H�_d���7�Wy����f����0��q���3�l�o��`�rmo6&:Y�w����u;P�H��H7�0�=Z���l��u8_�fŏ���~�o=卤�=u���@-������As���e5�4�|B��9�rBSD�;�u���4���8��?���".��|��`���χ�f{p]��� >]/d��1��.oϨ2fxO�5�Gѝ��#ܓS��\�u/5 _h�
%�������������ڣ��7\�J�)��������/{*:}o�-����V��S��l�Z���C�ӡ�`��칻^n����Y��m̻�fc���]�ٶlk=3j\$b�iO��:4�E�}���P/&X�\7���\�ךf1d!��������۶�15���p������ꆚ�L�c�ե�\湅��E�G}"s�)$g�K8O��C��#D����pp3#9qi_0!��k�z{h6�%�c��aZY"���2���1]N�*t}\S��|ۣ��S��`Jx,ph�jbX�+���F���o�a?�6��}ih2�z����)����A�&��X�x�"���1gh��	������ng��=�P�;����m�]�<�,��ӕ���:����+"TO����<��6�>��z4�ψ�W9���1�j���T\�-@JY���r����7y����j�N� ��/����bk��;� �)-�8jRe�^l�5X&��ENe�ZV�Y���ܚ�k���+�� �2I�U���,zWs�A�^���/�pB��f�OD�c{��IV	ڍLWSaӪ5ɯ СzkϮe;�z-W��WpB�����<��(�	g'Y�eJ�"0a*lֲ���;ږT����Z�G9�w�Z)��p�'T� q(�I�k9��Ns���4���Yˊ
�v��C�֞]��$���v-�{⑎�ܧ/�r��PK    �j2���n�  �     Realtime CC Processor.pd�V˒�6���T|X;6���\�J��%>��EAc�P����O�A:�ծ�Ơ����g*��9�(�C�ӌ�$�(�H�8��!u���8#W�T�jFa0�zm�S�r�ku� ��Ky JȇU[7��\rg�P�$�G�y�K*�y�ZJ":4%����#|<���~��̍�'%׏}��q�MňQ�o\5���Y,cA�O�x,�"����q�Q����=zb&���ݬ�\U�:w[�U?�.��R��5d&�R���O����P����-yV Q�J��������I;�]����9"{���+:��,�� ��Z�j�Oo��CI���@��Ċ3�f�ŏ�tWE�}�O�{Y�*�Y75H�$�b��� ��[�؛�Xh�1.`��ΰ�TA��A�!���.�,�Z�؆�7��w�=W+ID"6�h��U���L�,x�JK��^����'�W�}�J��|fc�R;�ü`�8�c��S d9��0K!�����e+ݓjݪ9�q����	U�/gI�i��/��2o:��s�ѓjt�jz2L�V��T��T��#X`ϵ�޼U}s�[��Ti��{��3x�i9M-ϵ�J����wt;W���3+0�������p���W�Ԥ��V�[v�y�Q�Y�u�1\�^�K�v==�����5������x��\�Ŷ.�TK~�S�j��g"��.�\ppSrU�����#�WR-խ�jCW����N�a��t<ϙ~8i�:����w��C�:�N�|E���`�>х/�va�C皷�E�l䤑�3m@�;׬d=�}�e^5��[�XV��?�&��fZ�6Q��T[ s�B�e�,�a:�x����ɝ���&8A<���D�m�Y����-o8�:�u�+� �A���p�<xS�Q��Pʐxʳ�wb�ƿe-�ALŠ������E����I���u`� nF����t���~x��>�5)#��|PЭ�k��P6P��hk���1�Eh;>��?:�%�L�
���tS��23�u 3s����K�ם�U�cMc���ry~s׹��S<��;��G�<0hڪ���h��9��h�g�>͜/&iH�����	Mc��x�N��q�T�?=���P0o�J�Z�e̓>��|'˙z���Tk�\_-%�rp5�����|�g�%#7��"�U��#��l*i��VQ��>}�4�������"�p��Hc@ӵ6*r�*�QK��� ������g�fZn�b����M�o�ģ'n� e����H?d��B�|S]����2(�`dY��_a!�J,��'Xha|��!&|�[����[ E�Q�h`��N�n�s�~��f�hh� ��.h�h���6�e&{����`owԌ�����F}��v������m��F̰��:ڼ	�V��K[���M�Q1���,�63*l������5�m[�-�}D�fe��֔m3���`_�*"5�	5"]WƜ"feM7(;��&F�{�oͺ�ĒC��F���PK    �j2ݕjv�0  NH    Sidestep Sequencer.pd���r丑��@��f<��!���sٍ��aױ����T�jG���J���L�E�D�_pZ%� ��#H��wǯ�K��Y�e�˳:o������N����eח����<ۿ\�O����mv�jW�ev_�b)�B �o��uw=�gU#�g>�u��z����IC��\���fU�9�O��h�+rq��8�4���.�>��7^�wt]�I�;v�#�0}���t����$��!͒)c���>����ֺ�V9���xq�z�y��ri��Ƙ=m��"]'��e�M�,5+/��G���,�U���ϓ�q��[4{�O�|�ǘ��Zj[������/Ӭ���g�DSS�d���9�C�_>/�Y�H��{8�wOC����~R��v,�D�^��ew=5$�����>{�|��E}}����p|�+]%�7��D���}�km:�����K���j��aw�T;�вOV��c��BJ��%d�/b�%t)��$����<�J$��Y-.��l�F2���>���Ӛ\f�-7c�.�4Ƞjo���YY�Rvy�r�}��!������G�vz��`���ˊ����:��t����t~���RuN*�PW����t�gN4�|��`̧N��J����o6x�_^��J�tM��?Ō��;��¸��O))��Q�m��D��˛��Q��y�k$�b#r�<�i续b)C�fW�ߎ�b~<���k�c�k[!�vj�gc]�/�E�&���X����Ԗ&ǅXR��WbkR�4fg�Rk�[�������-����-�!�bn�S3m�E����Ӥ��6֮��,�O	avB�����{=)m��������~�M����[�jI�ۤ����>���3ڝ�UL��4`Cw���$}d߱^�@��D ����ī*�l4�����c�TEK$_�o��ׇ��r��9v�����28��6�.�Y���lb�uV���%F���^��2.�,�0S�_ʤ�=���k��Vfm-��2�.E"K2%�*�~��RT�5����������7��K��*�.�����B��Z�uq>��pCɆ̘[�B/Mk��Ž�!�oj-�:O��w�"C��Y��E&Ml��ҹ�����"��@8���f�NZÕ�(��������<�]Wfu���Jߏ1�T����.m~���"v�y>���7�0j�b�U�6�Zkw�#�r�z���S&)�g�(�^R�ګ�����|�EJ�cvRb�T�t=�����8	��\k��ӓ��$���A���pO׃�JtW�)� ����?�\_oٮᄰ�[�-��鲗��j~f|�<b@zi��pmg-<��JE��L�"$�%���VW�/;��b�1$Y�BĈb��,uEQ���hH%/F�$��ɋ1���Y�\Y���Tx��E���O_�)��LJ�l����VTcYx�˿��ۨ���2	��1xi)�ܙ����!!��G�E�C�I5`-��(�O*��|�.EAQ����T�jPMO�éx��\3��i����\���)fղ�2�j���[�ԡ�;qTs/c�P�w�3�'4{|�B�Rέ��m���]fO*�������վ�ٜ+��q�����I�$Y�u�����2���YjE^��NQ Z��9Z�:�q����|UL�J����a�6[�D�/>\m%����i���.E�vݟ�2ʚ��E�y�w�?e�c�%/)Y�(��^�	ݸ�AA�c�ڰ�0%�[:�ѻ�5���Z&�Q��"y�G䖵��ޯ�}j�s�j?H�!ڄ��4��I|��&W�	W)�,��ń�Է4i+����U�tlK�Y�`�@إI[��Z�$�Z%Ѧ�����ʶ:�Z���.�>�֔/��_j��f�flfT�D��\��z�]�E0��ti��wqb��.R�K.XZC���-�J���[����eqo!�y���`��Ж�K��6K�R��{��hF�N$i�ClK�&L7.�mh�!t��&?-h��iA��ɴ��K�#�r>�h�Q��Qb�O� I+Z�6wLs�qF!��@(�e�e���h:��ךK�ą&��	#���%>�I'��d���hD;޹���f�h.�t��5�sR��Ԭ�D�k7&�i��k�Q�ͮ6'��֘h���q�f�i69�~Q������&�i��&���@��4ٜ�&�l2�&��ً�kn�\gM'�t2M'�t�[�N�����t�d+4�L��4�Um���d+MA�\j��)������L��'�c���:C��9��)&ƉP�N�`�7必1�B��L�Fc�D�1�cNc)� �ٸd��o{�9R�[���c���c�����;U�'�4]�Ͳ���]2���,�0I�����%�n�����9k�2;{��ٳ�n:��*mT��������R�V�6�m�ۜev�,�U]=N9�,si�-0�f�k�e�''�l�9�]T�|r�<�b5g���Js�1�4���4�H��2��4��zR$E�/f��Gj-�3�4�L��4��-�e!ɿ[c5E��hf#f+,f��=A�z�¶މ�� �.�l�[S���j$7�F�6kC�܈܈܈܈܈܈܈ܚ����܈܈�f��ܨ�$r#r#r#r�z^�(T�}U�ȍȍȍ�ͯ���ْ��Q8DnDnDnDnTm��(h
��-�N�*�zN'�*5�i\�g��H�2uH��+8G�F�F���|H�F��B�F�F�F��"�d�K�[���K2�P��GBnDnDn2��%Lf$@ �0���������Wz�_�hn�hv��|;���V�W�St�g}�-Z��n���)����n�0��[4����	��������%!��ns_B7B7B7B7B7���j�ne=��4ݦ�=��z�B���Ѝ��೉na&���x7~�)��E���������[]l�������ЍЍЍ�-v�w_�7�]x�-[�{�m�#�kL�#�+סn�t���Q uuu�*AMAԥS��PT�M�E¦Ҟ��U+���Ƣѯ>\jc�2.�#4�G0H0H�x4�G0H�x����4�G�x�%L�+�G�G�׮7�'���~��Ȍ�����T9%���S�+��<#>�s��g�+#�EkUވ/�Z�I|D|�I�6��d}J���������������K�G�G�G�G�Wc���t-	Ü���R��}3�D��_R'�Rre�JMh���vqa�SSm�T��S��h�M^+�ʮ���p<\cD]��K�²�6�]n��T��71w�e���]V�t3���o^�h��s�6Y(�
֗������a��[�R�{<�}ɢ�[�͚���O�M��wi�߽��0��Nr;�y %��<tՉ=0]��,ZۛE|�Ʊ�Z��曥��~$��Zf�h;����Jr�\o>�`��n�:�]Jе~v�?��;���3sW$�*�v#V�SG�D6��s����6��i-�ȖEv:k
b�4��4/��!�d=�y��	��E���%�q�3b��FPLQY�9��t��i��(��T�m1�*��R(�T~8>�R�<��a� �� C�<�%�/�6ƀ�����4��gg~e������2�5fI�,�4�=��]������2���/�̇�G=���$ٞbI�$Y�,�c��$Y
hK�$­8)δMuC�j�!�n�ݲi��I�$�Rce���[Ȉ�-8n�I�.���΋�(��T���5��S�i�_�����W�����Ӛ!�tR:)��NJﶕNb!�v��) 
hS�t�����ے�v���/��LJ�H���P�HJ'��ң�H��T��V:)=ui蟔NJo������|§�^�՝���β:ۚ��ʴ�ՙߑ�O]��.�����iy�i?83���]�u���m�MkeZ��O��Uǒ����i��[+�§��Z{�Kl�Ri㳊��2���O⇏>��g�󌹙�gH��En�%�Ԭ����<��g���~�Q��3���M���z����BC�L$�<��	��Y4���*Z�e2�إ�{-÷34�+�hhDC#�4�����4~H�!�!B4���fN!n�����5F#��~�ދ��gi4�`LI���&(4�W�𡶦1*k�P'�_��B��lί�A+����J��BZ�m�VH+�Ҋ���)�Zq�V"��k��Pi��BZ!��VH+��
i���R-���O�ⶵB�LZ!��VH+��
i%�$���|�K�Y[H��6���_�
��)�H+��AZ���䴲�Ve/,U9KU~�*�W�+���D+~1�4,Ś��&P"P�G�ҔԈ*S�'ލ�,m�i�+� �%Vsq;�ʄub4	8>� N��@��w�[�׶�ՙV�XM�nW�nǏ{mK���j��!�k��J�4�J@	�֦��,sYw)����捉�ym �z�ڭ"(!(!h���M��~��u@�ѧ��\��6�
Z]$�&��X��
uޟ���Jۺ:V���h��mc��S@���nj��Bi�h�@c+�X��
4V���h�@�E�_a���JN�1(i��B�j�
�5��.i��BZ٘�gM	�3�33��V��?����BZI_]^I+������V-�V��ڲ��o1(i��BZ!�P����
i������A�9�W����W�_!��V�_!�P�B�
i���h�O�̒�&aP�%%%%%%%%%%%%%%%%%%%%��*G�\���u� ����:�h��+���,ݜ�V������t+�*/�n����[m�U�ҭ���v��._��<��V|3p���^>��/ӯ7����?n,�_oN��ͺ��M!L�R4�6U�e��׶��*C%Œ�����ZS-w͔\��;��Mj�w�,kS�V]�ϲ6Vd�5��1c�6�O�y[a���n��{��-��c���)�&4�#��,_)`
8�FQ
5 �����ʗ'��<��b��{��b%��(�X�621��LԀ����>�E|������fr5�b�ˤm��|,(�����V��K?I�k��Ҋ�q�H���W{sIG�jo]=|�;�*0v[��/��5,Nl��q�@�XW���!5v�ccoҚ���*}.`
#]����V �elg�<�	3�h��F�M��e�,c(���u'�BW��&��;qZ�2�8���ZL~h0f��ÙUs�M��h���ц�k;��������y�|:��ϧ�L���^?+>=$�?�w��l};��e������lw|ʞNߎ���|�|y��eg]k#;=g�������\��?	����Ɠ�=�.?d�f���r����}��%��)���i2����t8^��{���>��%J�Oip��w읜�].��P�-	����ӓ�})Y��e��В�:��y��=���A��2yN"�UJ�%���@v���^�&i�l;<����!A�^Bfd��ʽ�׿�����"��O�x�`�v��%�������Lo"������#���$}�e]�4�����׏�9�f���4�~JEȾ��k��������J�O������p�L=}��I�NG�N�������N���*	y�K�k*%l)��u(��-�O_�g�>�~���K^%%&����R����_�~{ӄ���~���!P�B_�5w��>�o����K���je����s'���Dn+{��^%�����Ċ��椙��z����Q]��6qu7�i�߁��|��R�V%�I��J'�|�����v�������%R)���x��6V�˗���P��h�l�^���>��ӷpC��{�T������uv��Y��6D}�V�����֝�j�T������w�-�'�.����w���8}�t�%��.ٯ�����oCV�|�{>��o߳�ϳF�!3w;	+�^=5E���,Ժz+����S�~�I������L�A�o���[l�N����?����S�~�����t�ۿ������2��E���&e/�rw������(��$L�q���.!�~�)>JH�r˧��F?Ǫ�R+]P@�C��*F/�s�5�\�.UJ{I��8^��?����~��O�+����� a<|��ۅ5���_���Z����-�z��[+���E2D��b{�<kR�.lh��THA�^��!/�����s���ow��e�h����������o����O���ь�{�֐�!��~�	�k���*��u����7)#�l䖔��f�-�F�;{����>?��B���o�$���:s��\���d�i!~q
O٧4�o�*�]}����]�m��í-�n�C�����(>��eV6gId_0Ee͓�U������U��r줟�\�$�Y����>��C�^N�2��r���M
������3���Ϗ˗�{������g��p��t���}�OR�o���"f�5O����A�)h�.ÝނёF�A�����i!�/�!_��as��'k�4�q m�z�̭�$���jk�Z�<��Nǌ�E���Pe�|M�tf{�AZ~0>	Ց���,Շ��G���ŉ���\~0f�bT3de-�߄a>eQ�^�E^}(��:Q'Ȣ೙Ee��������,R/ʢ�CY48Q�S|6���E$��E��"��,
>�E�e9u��E�����h�B�S���4Q[k�T]X:�it#�����Z{��uU\{4^ܥ��v��XW�k��ڣd)7��=Z{s�Qͱ�з���&Z{4޾D��m-.J���⢤DZ\���Eiq��u�`u�x��ƒ;b.��$�5z-ύ'M�K񕉕=�������m����Pi=T���C�V�CM�"��:�6��A��z���F�R��z�4M�ƀ)`���^����
�6�Ӹ��bE��V[�\�&��)mrA�\к���)mrA�M��)�kJ��&���~74�k[�I�V��ڟ�v}��j}��j�;F���a3�j�����x�<�'s�*ٮ\[DT����"��ZU�ED��-aѴ�xk�[��e�O�<�ED�����h�Y,���(-"��SZD4Q^���V�ʖ�ʒ3gӼ�KGi��RV��B���Y��3w��M�3�ӻ��K@�"���(-"��EDi��Q��F�V�������D�=hO����D�=�K�Kh#r���D l!l��aaaaa-�M[���m�k�^��ו�%�%�%�%��%&0�����*]��
�����&,������
��dH#aI��%с���������������������������[�����x--�����|��XJ�iף��S,��hv�\�9و��܈�37"��KK��!,!,!,!,!,!,!,Y�)aIm�mPo�m@������m`�җb	a	a�	 }� KKKho�Zg|F���l�[|�!�f�����������e����������?�%�%�%�%�%�%��+���JX�(�x����w�+KKKK��-�Ñ�MX�q�AXBXBX�Q�T��%kASЄ%n5`
x{��̷~s��z��AN����]��%�!�,������$�BXB[���X�4&���� �U�U�U�Ub����v�9�Ҭϡ4�90�Nm�[o����ۣy+a1�UX�&�N`E��V���Vz#͡�������
�
�
�
�
�
�
�
�
�
���
�
�
�
�J������VZ�����������W�`�`�`�`�E++++++�E�z.P.���N�a%�����*�*��5�J��*)��?v�A�
�
�
.�
�
M�BS(0M���$B�I�AS(c�Y�%Via
�X�X��P�UKKh
��dj�)����������M,iױ��c�����ݎ�����h���j�bI<�����z�:�:�������������������������������������������������{��Sv�>��q��ތ�^��z^�i��[����ܝ3�a-�t�gZ��z�����ZϚh1����5ѣ�͚hs���tN���n�>L}��n�<���U0�7�&�_չ�Zչ�\ՙVߤ�7i�MZ}�V���7i�M'�F�nuYks�js%��^	�J�D�fڙ�2Vc�E�o��CZ}ӯnB���I[�Ƅi�+�}	�dUg]S^���LMW�͇ϋ�tUg�
i��BZ!��VH+�Ҋ[�
i�mjřZIW��fҊe$��VH+��
i��BZY"G?����
�BZ!��VH+��3i��BZY_Yyu����M�xS+�J��LZI���
i��b�b�X��27�VH+�ˣV�U��G��;�~�UZ����_j-lkk�`�[�V�X׷ǵ���E�7o.��Ӣ�U��U�Vu6!4�@o�Z�ښ!䦵0���3pN�j�r��.��yh�g[���I�Đ�fm3�cf�˼3�61$1$1�,܀�	C�����D�ޑ!5�K�$�$��0흂��f����L}n��R>�T[�S�#�z�*���ޟz��i�L�?����U�RC#�z�*k�*�*��G�����ޟz��i�L#e������_�y������@�4Q%i��BZ!��VH+���O�
Q%i�mi%��hN�BTIZ�F�
i��BZ!��VH+D�ݸgm�亞*�m�$��VH+��J�
i��BT���Z1�2�~ok%��h&��F�
i%u$�UU�VH+D�~�?.w�(�!	 	!V����	!	!	!i��H[ ,�@�mD[ �Ң��h�h�-�R���3�d�ٰʞ/���U�f!	!3m9��Ӑ�͜"dj���IIIӐu:ن��!�;E��@HBHB�reқ$�s&yxzl걩Ǧ�zl�R�M=v�I=6� �n�Mlllll���N��cS�M=6�n�{l걩�&,B٬���WT�4� i��BZ!��VH+��IZ!$���׊��JJ�ќjH���0�VH+��
i��BZ!t�V�ٯ�V���&A�
i��BZ!$��VH+D�[=Nگl�/���V�$��&��F�
i%u$�		�VH+D��_I�+k~�M�k�g���l�r\v��f!�mT��$�$��F����!	K�+��Rkk���V;6;��b�t��V�!W1��0։��q��,d���W]��21B�)B֖�,Ls���������E�"��e�����7Y�YHBHz�5_A�z!�m�����z�ꩫ�a1u���SWO9K!d������m���L]�e�����ԑ�zӰ��z��	!Ǉ���+�z���BZ!��VH+����'i���ⶴ�"d4�Z�$�,���
i��BZ!��V!�^+ۯ���IZ!��VH+����
i�r��I����ᬭ�t21�I+���BZ1I+������
!d�J���!�Ao�N���a�佲��^d%�$��,	!	!	!-#!$!$A!$!$��"	!�ĸ��j�ק!-����C�!+�������r>Y%V��mnZ�,�����T��Kg�;ӻ�Lo��c�F*��[J��ta����vv���F^A���<�R�YA���x�8���w��ݧ���M�i �_��O���"�l���P��m���[���ңr���[��K�����E�?�;U)e��΂���c��=CSK��;���协���̕g-����K����OGI�ռo_ƞ�w��j�"+9ת�:��=����٣`��;����0K}��m�y�B�����[�,x��[u�1�K�7�8��4�W�n���������ʞS���gV*�j&Q��Ǔ����O�ƌ�H\�f͈�yB���E�������,9����Ȟ$�K�\�3=�#:y�E-3�,����,�}(Z��䇘���]W�"�0�v�(��D�{=f�{�J��TC�k�^A�C�v�%M0]C����Zo�E�jS"FC��uA����SSi�P�XۅQ�|��R&w�Pbz���:�H�1:I2�9�A��dИ1u�
b3	XA����L�L����� ��&cW��6<�
s�6�PV_'�������P$K�wk�����C'뫻>�0ҹ��R�Ҝ������{�\G�SL�kΫ��.@m:�d���M���sĜ��Ag����z���@��,�����ѱF����_�{TL���zՆ5QAP��@$�W𨯐QT���� �*��4)�����.��E�S$S��<Z�E�ԧ�$zհ�E�-�}�X�gҲ���_�e�~��C��s�����G �Q�k#B�a��-�E���������L�L�̎*947�����%�o��C���� ��E|���P�@ݜ8��=��#�r9R>�n*�us�T��I�'ka0{ U;6\:��#�g�*�����Tݤ,I^H�J�4zDSC4םV��ӊh:�m��k�/���hd��V��&"����Vq�l�Z,W8!m�&�{'�I�>��zp�.A��P ˤ ��2ti���@-Lxh����S#V�r��t��z\��dL8�zm�iRZv�!-]�;�Z��.�!��3K����g�?��?c5�ռ��8;-J�D�"��;={��h����z����Z��DY���u>�<u������d��!����?�u,�}{���{2_>����^�Kll�@�eݯR{
_f�:|�	g\yV�Y��
�¬���
�b���j�����O��:S���an0�l�Yus��]��M�b��Λ�_����v���`�W�?�3q�x}�$�W{��
�0�B���¬��87�+�F�
��j����Ya5�V�V�g+�
Sa�	p��*d*d*��l�n4V�2&��TX���U���/�����o�o��?�?�?N��o��^�����#��#��#A00��Y���LP>b����_���K?�]��]����o2���o��߯ɮ�O��i���<�}�6z�S8�6�
�¢�B���c��j4�+�F�
��jt:f��Źi�X!5�V�V�,ά8V�a�Ya5V����/b�a,����ocX�1��0�r�IqLI(���`�`�(��(��(���m��˭?�00�[�[T����r�G��G{���v�v돆������}\5^�l�P�B]t:��i���fhφ����-���t���$����4���ap��ĵ���l�#����?�����fS����	��?��w'|z���wiq`_$���q.V�/��Qz��� ���;�M.i����I�Ʌmra�\�&�!�mrQ�6�0��+�v#d�!��91�,�_n9�7k&��c����<�*0��K�ma١_��%�} ����9ߣ=�$$�y�:,���$�
�
�
�
�
�
�
�
�
�J��J�Z	X+�h�h�h�h�h�h�i�i�i�i�i�iE>���+�h�h�h�h�h�l�$Z!Z!ZaZ��Z�P+h#��L+	i�i�i�i�i�i�i�i�i�i�i%�:�b6O���#	a�"�(�#�^���4@��N��� )[ɶH����l%)[Y�V����le�YI�V����l5LPנBs|�|�Nz�-���ӌ-���N�Bg�G��eb�z��<�#<�c�47��C�?~���#��t�uM#��������eU�[�Yn-�E���?���G�7���������?��o��,�*RO�iSu����?�Y�����G���ۿ?����V	����Mp�xƨ~�?�7G�r��t�a��c�#�p�V���VQ����fq�(]�*
�Eì4Q�.���}:�-7?�2xht︴��Y^X묘���������[Ao�o�s|۠���L.�+ ǭr����P�&�)M ���d�.�+��8�_i4�4�VZ�+��x�p�������]�N�{w:
�S�Q(��pxp�<s�Q,�G5P�kV�ޯ�ޑR-j�T�,���Iڿ�Yڿ�:Skik�� ���-�;K���R���4�*,�iRC��&�:�:��W+��ƕ�A�J�y�e��-֕�hx�ԙ����~���q���i���y���e���ať������x+`s�+�.++�˿+N�+��+.���l�</���宬�_د8\8�8^8�8]8�8_�����^O���z�;�j�b!W5ײh37Xn� �f��`�_ 8K��H���.:0,k���AZ(�QDpQ �� ��Znk��x#�Ek�O��h�$Z8�V,�r�j��-{��E+ i� G��I$p	\DB �o�������,{�Ñ���`�v�X�a`iA`y@v��yW?��2^�¬�|q��r��<X;g��Np�����η����8��3F4���7vJ�f���d�VL�σ;��b�����6��svng��������C�v 7vF}��U:�_�p����g���/xy��n��Ή`��p8"2e����8˰�"�6�%h۫S���skf��M�/ClfY<���c�AM)���PK    �j2��ys-  i?     Simple Synth.pd�Zێ����<d.q�x�m�X �	�yrԶ�[�-9�ܽ������$S"�.L��}X,��*�g~�gq���j:K�,E�"/��ꗻ��Ct����Rȴ�{ڂ*��7���MKQ
v�b�R�4�o"ݪ)���TC���J��&���x!Taġ�m@���7!o��S���&�0���m+�@Bm4�Ew/��&�
��K�:���$Bb�$��>_Ư��(�|��ʔ�r��?��H��E���ӧ�T�׈J�x�_��(M�,v>��j���,�ND*E
	��_%^��~����"�b��o��&��������Yɽ��U�t[�4ɢ��S�� Nefn*935g�۞Y%ZH��(�������Kݾ�d�y�U"��T�.ΰ�,=4K�<�P�5�3�����{BJ(�h�Ň�j�?:��D�������`ɨ�����i�!-i�a�� k��T��8�2��R���}�Y�7_�?ə�s��:�?W3�J�&Y�;�-"�9��X[�Z�9�>Շ�G�p���V��L)F�(���N�E�93͡E���Cs����x��z�Ly&��bv��@˄,�Z�J'�v3����x�z�|��՚fH����&��
)�ҋ,LV�vZ�Q|��嗏�6�2cs��\��ʕ���$f����~s(�"�0��܆֩���kHa�.��v�΅�f�ĸS��N�r�@gv��S����E��O��NL�p�!\�r�v��i�4��4�����Fj�Q�R���2������NrE� u��jEF5��IYh�,��\�9�<Sf�)ϸ�Y�y�0~,���n��'�xc��I;E�fpR��
�k��!�^��b"�Q�3)>���Nz��saX�f�E�/��[J����f��2��_�<yj�V�#������U{Z<�(�����p�0���ؑ�)ы�IT��.p��l݊�>��k}|Xo{������"l�M%���i�H�6��:�����Mb/l�dRl�iT�ap�b������Z���Jҹl�w���m�#���1�!ò�^g7�1���uf��C8ߍD�����:��RE��H�|k�^�'$��fo�N���t�.˼6��u��m$R�63��ԝ�s_�"'8I�_�Y��F_�CK��#�:<�����yҞ�������o���iՋvm��ew66�&�
[�T�����qX	�+~Z]@p������t0�+�؝��`�y�.E
D�\a����sוnUI�;�;H�ˡ���4�J�H���4}�����k���9bY���]�y�A��|��MA;���{��������A?>X����}��6*��ǁ4}�Dz6�Q�[�q�)�cP߽sZ�Z!ٕ�9?��]S>4��V�O�q0mD�j:��N�۞�S��du;:���oƗ��ծ��ğ��?��0A��eQ�=r7��
2��V�jѷ���S�HK7@�����X����.�\� f�㼊R�ڮ?W'�W�s��1�Q�H�@R��t%�D����h�Acuz��K��ƭ�/�SRlgE��؏��{�{e�������*�*��Խ���s��C׶8�۫vE�I� ��2��������C�]�3�-���l��w�A�#mx`F����ʜN)��t�-#�L��edl+�$W�j2SnQEi�*zӴ��_4��4��_!{��A�����0�=$�L�d�Z��}���-�}m���6�Dy9F�ߒ�f�%<(�N$��q�|��s��K}�>BP�A�}Ǒdm?>�j���pS�RF"L1P�`�v�Q�ĂQFQ]�c�G��a8� �R��Di`��fi�J��b)�-���6(	Q	�@��o>jsT`���� ���+(��2N%D��GM����gό�`4�,W>j#/��%�z�`M�u�Gm��.3�ds�3�|��2�橏�ԪD�?ZN��3 ���q:��ߪv�_֖�@C+P��2*���T�̺&(f�]`_6��������)�Ʉ��Ni���Z{S_6���-D�k�aZ���x(5�h�}Bb�J�+��?(7�N��B�پ@o^��q����#�w��	��麖80�'P:�t,���I�Y��^��zzd�TΟ�����c_8��c_Ln��='��)��nb��|��/�B:0�#_�%Mr����#_�%��{���Ѩs� �B[n��>�HZ+���xs8~�._�k;���"�by�t�U9��b�Os�}O��t�S�e|���{�}/�䱩����>OVR���й����t���6H)?��b����^�m�R2��/u�\��s���OU�=i��}c9c��4���`gI���������'f�dߴO��O�S�=}�wt1�u{<���R��h��K�6�?��k��lN��0��v�x�������nO��n���zh���k���=]�)Ү'�zi���v�z-�c3����B��D��ţ\���ytQ5��sw�z�-���[�+L9���)��㿯t�wK�N����a��&9WU@5����掫:�l����a/窅?��/��c�|�����rOؗj�W���9�'��a��m���������C�����]���R�hѴ�#����{��_�w����C=�C�%�����W�纟�{�ڟ��W�h��'5����s�8"|h?�ֶmy�y�������ny���ǜ̴#��ܴXQl*3����l񎮎�-��W���W�i�v��I���X�J����]���?�+-?��4�_z��J�:��W� Z�KoF،qºbm[�gw�~j�������i��fUIw��N]o�"Gڧ+p�Dd�6z�]��u�]��u�]��u�]��u��%L�]g�s�:Ǯs�:Ǯs�:Ǯs�:Ǯs�:Ǯs�:Ǯs�:Ǯs�:Ǯs�:Ǯs�:Ǯs�:Ǯs�:��8v�c�9v�c�9v�c׹Is���u�]����������E�c�9v�c�9v��<s�!���w)t�{!��<9	�2���o�>�>�����w]Cvݙ�Юcȼ��]����w4�˼�<q�yG�;̻9d�ݔ|V>�Rȝ;��Ͻ�s
��I�c�I�c�9v�c�9v�c�9v�c�9v�c�9v�c�9v�c�9v�c�9v�c�9v�c�9v�c�9v�c�9v�c�9v�c�9v�c�9v�c�9v�c�9v�c�9v= 9v�c�9v�c�9v�c�9v�c�9v�c�9v�c�9v�c�9v�c�9v�c�9v}ŮO��c�!�>�nE�a�'�;캥�ޣ�9v}��8v�c�9v�c��9v�c�9v�c�9v�c�9v�c�9v�c�9v�c�9v�c��(�^r�:Ǯgi�]Ww�u�]��u�]���`�tB9v�c׽s���r�z�Q"=���8�]Ϣ�ze�K���<z��G�ue��}v=��뛹q�:Ǯs�z
���}��S~�$6��a��"M�"����O3��#�g#��U��V�;��$H��G�(���E��Ȅj�82��K��n�rB3��2��³���hT�1T�hUQQ4��E��UQZFe�(����VA)L�]X�(�-c�N�����wl�z�	ͽ�;�*,�&��Ң����j����t��	�²���-��|�P[!#��Ċ��v��bn �,Z��h-��Z��R�C4�͖TVm,oup�PK    �j2���E  ZV             ��    Note Transposer Delay.pdPK    �j2���n�  �             ��{  Realtime CC Processor.pdPK    �j2ݕjv�0  NH            ���  Sidestep Sequencer.pdPK    �j2��ys-  i?             ���I  Simple Synth.pdPK        X                                                                                                                                                                                                                                 ./._posplot.py                                                                                      000755  000765  000024  00000000305 10736566544 014352  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                             Mac OS X            	   2   �      �                                      ATTR �   �   �   -                  �   -  com.apple.quarantine q/0000;477adfe2;Firefox;|org.mozilla.firefox                                                                                                                                                                                                                                                                                                                            posplot.py                                                                                          000755  000765  000024  00000025254 10736566544 014012  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         #!/usr/bin/env python

# Very simple serial terminal
# (C)2002-2006 Chris Liechti <cliechti@gmx.net>

# Input characters are sent directly (only LF -> CR/LF/CRLF translation is
# done), received characters are displayed as is (or escaped trough pythons
# repr, useful for debug purposes)


import sys, os, serial, threading, time, re
from visual import *

EXITCHARCTER = '\x1d'   #GS/ctrl+]

#first choose a platform dependant way to read single characters from the console
if os.name == 'nt':
    import msvcrt
    def getkey():
        while 1:
            z = msvcrt.getch()
            if z == '\0' or z == '\xe0':    #functions keys
                msvcrt.getch()
            else:
                if z == '\r':
                    return '\n'
                return z

elif os.name == 'posix':
    import termios, sys, os
    fd = sys.stdin.fileno()
    old = termios.tcgetattr(fd)
    new = termios.tcgetattr(fd)
    new[3] = new[3] & ~termios.ICANON & ~termios.ECHO
    new[6][termios.VMIN] = 1
    new[6][termios.VTIME] = 0
    termios.tcsetattr(fd, termios.TCSANOW, new)
    def getkey():
        c = os.read(fd, 1)
        return c
    def clenaup_console():
        termios.tcsetattr(fd, termios.TCSAFLUSH, old)
    sys.exitfunc = clenaup_console      #terminal modes have to be restored on exit...

else:
    raise "Sorry no implementation for your platform (%s) available." % sys.platform

CONVERT_CRLF = 2
CONVERT_CR   = 1
CONVERT_LF   = 0
NEWLINE_CONVERISON_MAP = ('\n', '\r', '\r\n')

class Accelterm(object):
    def __init__(self, port, baudrate, parity, rtscts, xonxoff, echo=False, convert_outgoing=CONVERT_CRLF, repr_mode=0):
        self.serial = serial.Serial(port, baudrate, parity=parity, rtscts=rtscts, xonxoff=xonxoff, timeout=0.7)
        self.echo = echo
        self.repr_mode = repr_mode
        self.convert_outgoing = convert_outgoing
        self.newline = NEWLINE_CONVERISON_MAP[self.convert_outgoing]

    def start(self):
        self.alive = True
        #start serial->console thread
        self.receiver_thread = threading.Thread(target=self.reader)
        self.receiver_thread.setDaemon(1)
        self.receiver_thread.start()
        #enter console->serial loop
        self.transmitter_thread = threading.Thread(target=self.writer)
        self.transmitter_thread.setDaemon(1)
        self.transmitter_thread.start()
    
    def stop(self):
        self.alive = False
        
    def join(self, transmit_only=False):
        self.transmitter_thread.join()
        if not transmit_only:
            self.receiver_thread.join()

    def reader(self):
        """loop and copy serial->console"""
        out=accelout()
        while self.alive:
            data = self.serial.read(1)
            
            if self.repr_mode == 0:
                # direct output, just have to care about newline setting
                if data == '\r' and self.convert_outgoing == CONVERT_CR:
                    out.write('\n')
                else:
                    out.write(data)
            elif self.repr_mode == 1:
                # escape non-printable, let pass newlines
                if self.convert_outgoing == CONVERT_CRLF and data in '\r\n':
                    if data == '\n':
                        out.write('\n')
                    elif data == '\r':
                        pass
                elif data == '\n' and self.convert_outgoing == CONVERT_LF:
                    out.write('\n')
                elif data == '\r' and self.convert_outgoing == CONVERT_CR:
                    out.write('\n')
                else:
                    out.write(repr(data)[1:-1])
            elif self.repr_mode == 2:
                # escape all non-printable, including newline
                out.write(repr(data)[1:-1])
            elif self.repr_mode == 3:
                # escape everything (hexdump)
                for character in data:
                    out.write("%s " % character.encode('hex'))

    def writer(self):
        """loop and copy console->serial until EXITCHARCTER character is found"""
        while self.alive:
            try:
                c = getkey()
            except KeyboardInterrupt:
                c = '\x03'
            if c == EXITCHARCTER: 
                self.stop()
                break                                   # exit app
            elif c == '\n':
                self.serial.write(self.newline)         # send newline character(s)
                if self.echo:
                    sys.stdout.write(c)                 #local echo is a real newline in any case
            else:
                self.serial.write(c)                    # send character
                if self.echo:
                    sys.stdout.write(c)

class accelout(object):
    def __init__(self):
        self.patt=re.compile(r'X=(.{6}) Y=(.{6}) Z=(.{6})')
        self.buff=''
        self.pos=(0,0,0)
        self.time=time.time()
        self.plane=plane()
        
    def write(self, str):
        if str=='\n':
            mo= self.patt.search(self.buff)
            if mo:
                vec=self._vecfloat(mo.groups())
                #angles=self._angles(vec)
                self.plane.set(vec)
                sys.stdout.write('accel vector: x = %f y= %f z= %f\r\n'% vec)
            else:
                sys.stdout.write(self.buff)
            sys.stdout.flush()
            self.buff=''
        else:
            self.buff+=str

    def _vecfloat(self, vec):
        return (float(vec[0]),float(vec[1]),float(vec[2]))
    
    def _angles(self, vec):
        from math import atan2, pi
        x, y, z = (self._cp(float(vec[0])), self._cp(float(vec[1])), self._cp(float(vec[2])))
        yxangle= atan2(y, x)*180/pi
        zxangle= atan2(z, x)*180/pi
        zyangle= atan2(z, y)*180/pi
        return (yxangle, zxangle, zyangle)

    def _cp(self, g):
        ''' calculate position, from g's to meters'''
        return ((g* 9.8)**2)/2

class plane(object):
    def __init__(self):
        self.scene = display(title='boogiepants',\
                         width=600, height=600,\
                         background=(1,1,1))#, forward=(0,1,-.01))
        self.plane=box(pos=(0,0,0), length=1, height=1, width=0, color=(0,1,1))
        
    def set(self, vec):
        rate(100)
        flvec=(self._cp(float(vec[0])),self._cp(float(vec[1])),self._cp(float(vec[2])))
        flvec=vec
        self.plane.axis=flvec

    def _cp(self, g):
        ''' calculate position, from g's to meters'''
        return ((g* 9.8)**2)/2

def main():
    import optparse

    parser = optparse.OptionParser(usage="""\
%prog [options] [port [baudrate]]

accelterm - A simple terminal program for the serial port.""")

    parser.add_option("-p", "--port", dest="port",
        help="port, a number (default 0) or a device name (deprecated option)",
        default=None)
    
    parser.add_option("-b", "--baud", dest="baudrate", action="store", type='int',
        help="set baudrate, default 9600", default=9600)
        
    parser.add_option("", "--parity", dest="parity", action="store",
        help="set parity, one of [N, E, O], default=N", default='N')
    
    parser.add_option("-e", "--echo", dest="echo", action="store_true",
        help="enable local echo (default off)", default=False)
        
    parser.add_option("", "--rtscts", dest="rtscts", action="store_true",
        help="enable RTS/CTS flow control (default off)", default=False)
    
    parser.add_option("", "--xonxoff", dest="xonxoff", action="store_true",
        help="enable software flow control (default off)", default=False)
    
    parser.add_option("", "--cr", dest="cr", action="store_true",
        help="do not send CR+LF, send CR only", default=False)
        
    parser.add_option("", "--lf", dest="lf", action="store_true",
        help="do not send CR+LF, send LF only", default=False)
        
    parser.add_option("-D", "--debug", dest="repr_mode", action="count",
        help="""debug received data (escape non-printable chars)
--debug can be given multiple times:
0: just print what is received
1: escape non-printable characters, do newlines as ususal
2: escape non-printable characters, newlines too
3: hex dump everything""", default=0)

    parser.add_option("", "--rts", dest="rts_state", action="store", type='int',
        help="set initial RTS line state (possible values: 0, 1)", default=None)

    parser.add_option("", "--dtr", dest="dtr_state", action="store", type='int',
        help="set initial DTR line state (possible values: 0, 1)", default=None)

    parser.add_option("-q", "--quiet", dest="quiet", action="store_true",
        help="suppress non error messages", default=False)


    (options, args) = parser.parse_args()

    if options.cr and options.lf:
        parser.error("ony one of --cr or --lf can be specified")
    
    port = options.port
    baudrate = options.baudrate
    if args:
        if options.port is not None:
            parser.error("no arguments are allowed, options only when --port is given")
        port = args.pop(0)
        if args:
            try:
                baudrate = int(args[0])
            except ValueError:
                parser.error("baudrate must be a number, not %r" % args[0])
            args.pop(0)
        if args:
            parser.error("too many arguments")
    else:
        if port is None: port = 0
    
    convert_outgoing = CONVERT_CRLF
    if options.cr:
        convert_outgoing = CONVERT_CR
    elif options.lf:
        convert_outgoing = CONVERT_LF

    try:
        accelterm = Accelterm(
            port,
            baudrate,
            options.parity,
            rtscts=options.rtscts,
            xonxoff=options.xonxoff,
            echo=options.echo,
            convert_outgoing=convert_outgoing,
            repr_mode=options.repr_mode,
        )
    except serial.SerialException:
        sys.stderr.write("could not open port %r" % port)
        sys.exit(1)

    if not options.quiet:
        sys.stderr.write('--- accelterm on %s: %d,%s,%s,%s. Type Ctrl-] to quit. ---\n' % (
            accelterm.serial.portstr,
            accelterm.serial.baudrate,
            accelterm.serial.bytesize,
            accelterm.serial.parity,
            accelterm.serial.stopbits,
        ))
    if options.dtr_state is not None:
        if not options.quiet:
            sys.stderr.write('--- forcing DTR %s\n' % (options.dtr_state and 'active' or 'inactive'))
        accelterm.serial.setDTR(options.dtr_state)
    if options.rts_state is not None:
        if not options.quiet:
            sys.stderr.write('--- forcing RTS %s\n' % (options.rts_state and 'active' or 'inactive'))
        accelterm.serial.setRTS(options.rts_state)
        
    accelterm.start()
    accelterm.join(True)
    if not options.quiet:
        sys.stderr.write("\n--- exit ---\n")
    accelterm.join()


if __name__ == '__main__':
    main()
                                                                                                                                                                                                                                                                                                                                                    ./._posterm.py                                                                                      000755  000765  000024  00000000305 10736616470 014335  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                             Mac OS X            	   2   �      �                                      ATTR �   �   �   -                  �   -  com.apple.quarantine q/0000;477adfe2;Firefox;|org.mozilla.firefox                                                                                                                                                                                                                                                                                                                            posterm.py                                                                                          000755  000765  000024  00000023731 10736616470 013773  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         #!/usr/bin/env python

# Very simple serial terminal
# (C)2002-2006 Chris Liechti <cliechti@gmx.net>

# Input characters are sent directly (only LF -> CR/LF/CRLF translation is
# done), received characters are displayed as is (or escaped trough pythons
# repr, useful for debug purposes)


import sys, os, serial, threading, time, re

EXITCHARCTER = '\x1d'   #GS/ctrl+]

#first choose a platform dependant way to read single characters from the console
if os.name == 'nt':
    import msvcrt
    def getkey():
        while 1:
            z = msvcrt.getch()
            if z == '\0' or z == '\xe0':    #functions keys
                msvcrt.getch()
            else:
                if z == '\r':
                    return '\n'
                return z

elif os.name == 'posix':
    import termios, sys, os
    fd = sys.stdin.fileno()
    old = termios.tcgetattr(fd)
    new = termios.tcgetattr(fd)
    new[3] = new[3] & ~termios.ICANON & ~termios.ECHO
    new[6][termios.VMIN] = 1
    new[6][termios.VTIME] = 0
    termios.tcsetattr(fd, termios.TCSANOW, new)
    def getkey():
        c = os.read(fd, 1)
        return c
    def clenaup_console():
        termios.tcsetattr(fd, termios.TCSAFLUSH, old)
    sys.exitfunc = clenaup_console      #terminal modes have to be restored on exit...

else:
    raise "Sorry no implementation for your platform (%s) available." % sys.platform

CONVERT_CRLF = 2
CONVERT_CR   = 1
CONVERT_LF   = 0
NEWLINE_CONVERISON_MAP = ('\n', '\r', '\r\n')

class Accelterm(object):
    def __init__(self, port, baudrate, parity, rtscts, xonxoff, echo=False, convert_outgoing=CONVERT_CRLF, repr_mode=0):
        self.serial = serial.Serial(port, baudrate, parity=parity, rtscts=rtscts, xonxoff=xonxoff, timeout=0.7)
        self.echo = echo
        self.repr_mode = repr_mode
        self.convert_outgoing = convert_outgoing
        self.newline = NEWLINE_CONVERISON_MAP[self.convert_outgoing]

    def start(self):
        self.alive = True
        #start serial->console thread
        self.receiver_thread = threading.Thread(target=self.reader)
        self.receiver_thread.setDaemon(1)
        self.receiver_thread.start()
        #enter console->serial loop
        self.transmitter_thread = threading.Thread(target=self.writer)
        self.transmitter_thread.setDaemon(1)
        self.transmitter_thread.start()
    
    def stop(self):
        self.alive = False
        
    def join(self, transmit_only=False):
        self.transmitter_thread.join()
        if not transmit_only:
            self.receiver_thread.join()

    def reader(self):
        """loop and copy serial->console"""
        out=accelout()
        while self.alive:
            data = self.serial.read(1)
            
            if self.repr_mode == 0:
                # direct output, just have to care about newline setting
                if data == '\r' and self.convert_outgoing == CONVERT_CR:
                    out.write('\n')
                else:
                    out.write(data)
            elif self.repr_mode == 1:
                # escape non-printable, let pass newlines
                if self.convert_outgoing == CONVERT_CRLF and data in '\r\n':
                    if data == '\n':
                        out.write('\n')
                    elif data == '\r':
                        pass
                elif data == '\n' and self.convert_outgoing == CONVERT_LF:
                    out.write('\n')
                elif data == '\r' and self.convert_outgoing == CONVERT_CR:
                    out.write('\n')
                else:
                    out.write(repr(data)[1:-1])
            elif self.repr_mode == 2:
                # escape all non-printable, including newline
                out.write(repr(data)[1:-1])
            elif self.repr_mode == 3:
                # escape everything (hexdump)
                for character in data:
                    out.write("%s " % character.encode('hex'))

    def writer(self):
        """loop and copy console->serial until EXITCHARCTER character is found"""
        while self.alive:
            try:
                c = getkey()
            except KeyboardInterrupt:
                c = '\x03'
            if c == EXITCHARCTER: 
                self.stop()
                break                                   # exit app
            elif c == '\n':
                self.serial.write(self.newline)         # send newline character(s)
                if self.echo:
                    sys.stdout.write(c)                 #local echo is a real newline in any case
            else:
                self.serial.write(c)                    # send character
                if self.echo:
                    sys.stdout.write(c)

class accelout(object):
    def __init__(self):
        self.patt=re.compile(r'X=(.{6}) Y=(.{6}) Z=(.{6})')
        self.buff=''
        self.pos=(0,0,0)
        self.time=time.time()
        
    def write(self, str):
        if str=='\n':
            mo= self.patt.search(self.buff)
            if mo:
                vec=mo.groups()
                angles=self._angles(vec)
                sys.stdout.write('angles: y/x = %f x/z= %f y/z= %f\r\n'% angles)
            else:
                sys.stdout.write(self.buff)
            sys.stdout.flush()
            self.buff=''
        else:
            self.buff+=str

    def _angles(self, vec):
        from math import atan2, pi
        x, y, z = (self._cp(float(vec[0])), self._cp(float(vec[1])), self._cp(float(vec[2])))
        yxangle= atan2(y, x)*180/pi
        zxangle= atan2(z, x)*180/pi
        zyangle= atan2(z, y)*180/pi
        return (yxangle, zxangle, zyangle)

    def _cp(self, g):
        ''' calculate position, from g's to meters'''
        return ((g* 9.8)**2)/2

class posplot:
    from visual import *
    def __init__(self):
        

def main():
    import optparse

    parser = optparse.OptionParser(usage="""\
%prog [options] [port [baudrate]]

accelterm - A simple terminal program for the serial port.""")

    parser.add_option("-p", "--port", dest="port",
        help="port, a number (default 0) or a device name (deprecated option)",
        default=None)
    
    parser.add_option("-b", "--baud", dest="baudrate", action="store", type='int',
        help="set baudrate, default 9600", default=9600)
        
    parser.add_option("", "--parity", dest="parity", action="store",
        help="set parity, one of [N, E, O], default=N", default='N')
    
    parser.add_option("-e", "--echo", dest="echo", action="store_true",
        help="enable local echo (default off)", default=False)
        
    parser.add_option("", "--rtscts", dest="rtscts", action="store_true",
        help="enable RTS/CTS flow control (default off)", default=False)
    
    parser.add_option("", "--xonxoff", dest="xonxoff", action="store_true",
        help="enable software flow control (default off)", default=False)
    
    parser.add_option("", "--cr", dest="cr", action="store_true",
        help="do not send CR+LF, send CR only", default=False)
        
    parser.add_option("", "--lf", dest="lf", action="store_true",
        help="do not send CR+LF, send LF only", default=False)
        
    parser.add_option("-D", "--debug", dest="repr_mode", action="count",
        help="""debug received data (escape non-printable chars)
--debug can be given multiple times:
0: just print what is received
1: escape non-printable characters, do newlines as ususal
2: escape non-printable characters, newlines too
3: hex dump everything""", default=0)

    parser.add_option("", "--rts", dest="rts_state", action="store", type='int',
        help="set initial RTS line state (possible values: 0, 1)", default=None)

    parser.add_option("", "--dtr", dest="dtr_state", action="store", type='int',
        help="set initial DTR line state (possible values: 0, 1)", default=None)

    parser.add_option("-q", "--quiet", dest="quiet", action="store_true",
        help="suppress non error messages", default=False)


    (options, args) = parser.parse_args()

    if options.cr and options.lf:
        parser.error("ony one of --cr or --lf can be specified")
    
    port = options.port
    baudrate = options.baudrate
    if args:
        if options.port is not None:
            parser.error("no arguments are allowed, options only when --port is given")
        port = args.pop(0)
        if args:
            try:
                baudrate = int(args[0])
            except ValueError:
                parser.error("baudrate must be a number, not %r" % args[0])
            args.pop(0)
        if args:
            parser.error("too many arguments")
    else:
        if port is None: port = 0
    
    convert_outgoing = CONVERT_CRLF
    if options.cr:
        convert_outgoing = CONVERT_CR
    elif options.lf:
        convert_outgoing = CONVERT_LF

    try:
        accelterm = Accelterm(
            port,
            baudrate,
            options.parity,
            rtscts=options.rtscts,
            xonxoff=options.xonxoff,
            echo=options.echo,
            convert_outgoing=convert_outgoing,
            repr_mode=options.repr_mode,
        )
    except serial.SerialException:
        sys.stderr.write("could not open port %r" % port)
        sys.exit(1)

    if not options.quiet:
        sys.stderr.write('--- accelterm on %s: %d,%s,%s,%s. Type Ctrl-] to quit. ---\n' % (
            accelterm.serial.portstr,
            accelterm.serial.baudrate,
            accelterm.serial.bytesize,
            accelterm.serial.parity,
            accelterm.serial.stopbits,
        ))
    if options.dtr_state is not None:
        if not options.quiet:
            sys.stderr.write('--- forcing DTR %s\n' % (options.dtr_state and 'active' or 'inactive'))
        accelterm.serial.setDTR(options.dtr_state)
    if options.rts_state is not None:
        if not options.quiet:
            sys.stderr.write('--- forcing RTS %s\n' % (options.rts_state and 'active' or 'inactive'))
        accelterm.serial.setRTS(options.rts_state)
        
    accelterm.start()
    accelterm.join(True)
    if not options.quiet:
        sys.stderr.write("\n--- exit ---\n")
    accelterm.join()


if __name__ == '__main__':
    main()
                                       sphere_line.py                                                                                      000644  000765  000024  00000005052 11002300510 014534  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         from visual import *		
from bputil import *
from mark import *	
from math import *

class sphere_line(object):
    '''draws line between two points on the surface of the sphere'''
    def __init__(self, start, end, color=(0,1,0)):
        self.start=start
        self.end=end
        self.color=color
        self.c=curve(color=self.color, radius=.2)
        sphere_line.render(self)

    def render(self):
        v=vector(self.end.phi-self.start.phi,self.end.theta-self.start.theta)
        n=norm(v)
        for i in range(0, int(v.mag), 5):
            self.c.append(spherical_coord(10, self.start.phi + i * n[0],\
                                     self.start.theta + i * n[1]).sph2xyz())
        self.c.append(spherical_coord(10, self.end.phi, self.end.theta).sph2xyz())

    def show(self):
        self.c.visible=1

    def hide(self):
        self.c.visible=0

    def set_start(self, start):
        self.start=start
        self.render()

    def set_end(self,end):
        self.end=end
        self.render()

    def annotation_point(self):
        return spherical_coord(10, (self.start.phi+self.end.phi)/2,\
                               (self.start.theta+self.end.theta)/2)

class dir_marked_sphere_line(object):

    angles={mark.dirs.LEFT:[60,120],
            mark.dirs.RIGHT:[240, 300],
            mark.dirs.BOTH:[60,120,240,300]}

    def __init__(self, start, end, dirxn,color=(0,1,0)):
        self.start=start
        self.end=end
        self.color=color
        self.line = sphere_line(start, end, color=self.color)
        self.dir=dirxn
        self.myangle=atan2(self.end.theta-self.start.theta,
                           self.end.phi-self.start.phi)*180/pi
        self.components=[]
        self.render()
        
    def render(self):
        point=self.line.annotation_point()
        for i in dir_marked_sphere_line.angles[self.dir]:
            self.components.append(curve(color=self.color, radius=.2,
                                     pos=[point.sph2xyz(),\
                                     spherical_coord(10,\
                                         point.phi + dcos(self.myangle + i) * 20,\
                                         point.theta + dsin(self.myangle + i) * 20).sph2xyz()]))
    def hide(self):
        self.line.hide()
        for i in self.components:
            i.visible=0

    def show(self):
        self.line.show()
        for i in self.components:
            i.visible=1

    def annotation_point(self):
        return self.line.annotation_point()
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      test/                                                                                               000755  000765  000024  00000000000 11543522467 012675  5                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         test/test_bp_display.py                                                                             000644  000765  000024  00000005542 11005265715 016433  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         from bp_display import *
from mark import *

def test_disp_longitude_left():
    x=longitude(mark.dirs.LEFT, 60)
    itemtest(x)

def test_disp_longitude_right():
    x=longitude(mark.dirs.RIGHT, 180)
    itemtest(x)

def test_disp_longitude_both():
    x=longitude(mark.dirs.BOTH, 300)
    itemtest(x)

def test_disp_segment_left():
    x=segment(mark.dirs.LEFT, spherical_coord(10, 30, 50),
              spherical_coord(10, 20,70))
    itemtest(x)

def test_disp_segment_right():
    x=segment(mark.dirs.RIGHT, spherical_coord(10, 30, 220),
              spherical_coord(10, 20, 240))
    itemtest(x)

def test_disp_segment_both():
    x=segment(mark.dirs.BOTH, spherical_coord(10, 30, 320),
              spherical_coord(10, 20,300))
    itemtest(x)

def test_disp_list():
    x=disp_map(list_mark(longitude(mark.dirs.LEFT, 0),
                         longitude(mark.dirs.LEFT, 120),
                         longitude(mark.dirs.LEFT, 240)))
    listtest(x)

def test_disp_series_mark():
    x=disp_map(series_mark(longitude(mark.dirs.RIGHT, 60),
                           longitude(mark.dirs.RIGHT, 180),
                           longitude(mark.dirs.RIGHT, 300)))
    listtest(x)

def test_process_marks():
    marks= {'a':longitude(mark.dirs.BOTH, 300),
            'b':segment(mark.dirs.BOTH, spherical_coord(10, 30, 320),
              spherical_coord(10, 20,300)),
            'c':list_mark(longitude(mark.dirs.LEFT, 0),
                          longitude(mark.dirs.LEFT, 120),
                          longitude(mark.dirs.LEFT, 240)),
            'd':series_mark(longitude(mark.dirs.RIGHT, 60),
                            longitude(mark.dirs.RIGHT, 180),
                            longitude(mark.dirs.RIGHT, 300))}    
    x=bp_display(marks)
    proctest(x, 'a')
    proctest(x, 'b')
    proctestlist(x, 'c')
    proctestlist(x, 'd')

def proctest(x, mark):
    assert x.all_disp[mark].line.line.c.visible==0
    sleep(1)
    x.active_disp[mark]=x.all_disp[mark]
    assert x.all_disp[mark].line.line.c.visible==1
    sleep(1)
    del x.active_disp[mark]
    assert x.all_disp[mark].line.line.c.visible==0
    
def proctestlist(x, mark):
    assert x.all_disp[mark][0].line.line.c.visible==0
    sleep(1)
    x.active_disp[mark]=x.all_disp[mark]
    assert x.all_disp[mark][0].line.line.c.visible==1
    sleep(1)
    del x.active_disp[mark]
    assert x.all_disp[mark][0].line.line.c.visible==0
    
    
def itemtest(x):
    x=disp_map(x)
    x.hide()
    sleep(1)
    assert x.line.line.c.visible==0
    x.show()
    sleep(1)
    assert x.line.line.c.visible==1
    x.hide()

def listtest(x):
    x.hide()
    sleep(1)
    assert x[0].line.line.c.visible==0
    assert x[1].line.line.c.visible==0
    assert x[2].line.line.c.visible==0
    x.show()
    sleep(1)
    assert x[0].line.line.c.visible==1
    assert x[1].line.line.c.visible==1
    assert x[2].line.line.c.visible==1
    x.hide()
                                                                                                                                                              test/test_controller.py                                                                             000644  000765  000024  00000002460 10776564623 016502  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         from controller import *

class count(object):
    def __init__(self):
        self.val=0

    def inc(self):
        self.val+=1

    def dec(self):
        self.val-=1


def test__init__():
    x=meth_dict({}, 'inc', 'dec')
    assert len(x)==0
    y=meth_dict({'a':count()},  'inc', 'dec')
    assert len(y)==1
    assert y['a'].val==1

def test_multi():
    c=count()
    c1=count()
    x=meth_dict({'a':c,'b':c,'c':c1}, 'inc', 'dec')
    assert c.val==2
    assert c1.val==1
    del x['b']
    assert c.val==1
    assert c1.val==1

def test_add_remove():
    c1=count()
    c2=count()
    c3=count()
    x=meth_dict({'a':c1,'b':c2}, 'inc', 'dec')
    x['c']=c3
    assert c3.val==1
    del x['a']
    assert c1.val==0
    x['c']=c2
    assert c2.val==2
    assert c3.val==0

def test_pop():
    c1=count()
    x=meth_dict({'a':c1,'b':c1,'c':c1}, 'inc', 'dec')
    assert c1.val==3
    assert x.pop('a')==c1
    assert c1.val==2
    assert len(x)==2

def test_clear():
    c1=count()
    x=meth_dict({'a':c1,'b':c1,'c':c1}, 'inc', 'dec')
    assert c1.val==3
    x.clear()
    assert c1.val==0
    assert len(x)==0
    
def test_update():
    c1=count()
    x=meth_dict({'a':c1,'b':c1,'c':c1}, 'inc', 'dec')
    assert c1.val==3
    c2=count()
    y={'a':c2,'d':c2}
    x.update(y)
    assert c1.val==2
    assert c2.val==2
                                                                                                                                                                                                                test/test_mark.py                                                                                   000644  000765  000024  00000013247 11005010376 015227  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         from mark import *

class count(object):
    def __init__(self):
        self.val=0
    def inc(self, num):
        self.val+=num
        return self.val

def test_p():
    c=count()
    x=p(count.inc, c, 2)
    assert c.val==0
    x()
    assert c.val==2

def test_cc1():
    c= count()
    y=p(count.inc, c, 3)
    x=cc(y)
    assert c.val==0
    x()
    assert c.val==3
    x=cc(y,y,y,y)
    x()
    assert c.val==15
    
def test_longitude_left():
    c=count()
    y=p(count.inc, c, 2)
    x=longitude(mark.dirs.LEFT, 28, y)
    assert c.val==0
    x.cross(spherical_coord(2,10,20), spherical_coord(2,10,22))
    assert c.val==0
    x.cross(spherical_coord(2,10,29), spherical_coord(2,10,27))
    assert c.val==2
    x.cross(spherical_coord(2,10,27), spherical_coord(2,10,29))
    assert c.val==2

def test_longitude_right():
    c=count()
    y=p(count.inc, c, 2)
    x=longitude(mark.dirs.RIGHT, 28, y)
    assert c.val==0
    x.cross(spherical_coord(2,10,20), spherical_coord(2,10,22))
    assert c.val==0
    x.cross(spherical_coord(2,10,27), spherical_coord(2,10,29))
    assert c.val==2
    x.cross(spherical_coord(2,10,29), spherical_coord(2,10,27))
    assert c.val==2

def test_longitude_both():
    c=count()
    y=p(count.inc, c, 2)
    x=longitude(mark.dirs.BOTH, 28, y)
    assert c.val==0
    x.cross(spherical_coord(2,10,20), spherical_coord(2,10,22))
    assert c.val==0
    x.cross(spherical_coord(2,10,27), spherical_coord(2,10,29))
    assert c.val==2
    x.cross(spherical_coord(2,10,29), spherical_coord(2,10,27))
    assert c.val==4

def test_longitude_zero():
    c=count()
    y=p(count.inc, c, 2)
    x=longitude(mark.dirs.LEFT, 0, y)
    assert c.val==0
    x.cross(spherical_coord(2,10,20), spherical_coord(2,10,22))
    assert c.val==0
    x.cross(spherical_coord(2,10,2), spherical_coord(2,10,358))
    assert c.val==2
    x.cross(spherical_coord(2,10,358), spherical_coord(2,10,2))
    assert c.val==2

def test_segment_left():
    c=count()
    y=p(count.inc, c, 2)
    x=segment(mark.dirs.LEFT, spherical_coord(10,10,60),
              spherical_coord(10,22,75), y)
    assert c.val==0
    x.cross(spherical_coord(10,85,322), spherical_coord(10,87,324))
    assert c.val==0
    x.cross(spherical_coord(10,11,63), spherical_coord(10,12, 61))
    assert c.val==2
    x.cross(spherical_coord(10,12,61), spherical_coord(10,11, 63))
    assert c.val==2
    
def test_segment_left():
    c=count()
    y=p(count.inc, c, 2)
    x=segment(mark.dirs.LEFT, spherical_coord(10,10,60),
              spherical_coord(10,22,75), y)
    assert c.val==0
    x.cross(spherical_coord(10,85,322), spherical_coord(10,87,324))
    assert c.val==0
    x.cross(spherical_coord(10,11,63), spherical_coord(10,12, 61))
    assert c.val==2
    x.cross(spherical_coord(10,12,61), spherical_coord(10,11, 63))
    assert c.val==2
    
def test_segment_right():
    c=count()
    y=p(count.inc, c, 2)
    x=segment(mark.dirs.RIGHT, spherical_coord(10,10,60),
              spherical_coord(10,22,75), y)
    assert c.val==0
    x.cross(spherical_coord(10,85,322), spherical_coord(10,87,324))
    assert c.val==0
    x.cross(spherical_coord(10,11,63), spherical_coord(10,12, 61))
    assert c.val==0
    x.cross(spherical_coord(10,12,61), spherical_coord(10,11, 63))
    assert c.val==2
    
def test_segment_right():
    c=count()
    y=p(count.inc, c, 2)
    x=segment(mark.dirs.RIGHT, spherical_coord(10,10,60),
              spherical_coord(10,22,75), y)
    assert c.val==0
    x.cross(spherical_coord(10,85,322), spherical_coord(10,87,324))
    assert c.val==0
    x.cross(spherical_coord(10,11,63), spherical_coord(10,12, 61))
    assert c.val==0
    x.cross(spherical_coord(10,12,61), spherical_coord(10,11, 63))
    assert c.val==2
    
def test_segment_both():
    c=count()
    y=p(count.inc, c, 2)
    x=segment(mark.dirs.BOTH, spherical_coord(10,10,60),
              spherical_coord(10,22,75), y)
    assert c.val==0
    x.cross(spherical_coord(10,85,322), spherical_coord(10,87,324))
    assert c.val==0
    x.cross(spherical_coord(10,11,63), spherical_coord(10,12, 61))
    assert c.val==2
    x.cross(spherical_coord(10,12,61), spherical_coord(10,11, 63))
    assert c.val==4

def test_sequence_mark():
    c=count()
    y=p(count.inc, c, 2)
    x=series_mark(longitude(mark.dirs.RIGHT, 60, y),
                  longitude(mark.dirs.RIGHT, 180, y),
                  longitude(mark.dirs.RIGHT, 300, y))
    x.cross(spherical_coord(10,85,322), spherical_coord(10,87,324))
    assert c.val==0    
    x.cross(spherical_coord(10,10,59), spherical_coord(10,10,61))
    assert c.val==2    
    x.cross(spherical_coord(10,10,179), spherical_coord(10,10,181))
    assert c.val==4
    x.cross(spherical_coord(10,10,299), spherical_coord(10,10,301))
    assert c.val==6
    x.cross(spherical_coord(10,10,59), spherical_coord(10,10,61))
    assert c.val==8
    x.cross(spherical_coord(10,10,59), spherical_coord(10,10,61))
    assert c.val==8
    x.cross(spherical_coord(10,10,299), spherical_coord(10,10,301))
    assert c.val==8
    x.cross(spherical_coord(10,10,179), spherical_coord(10,10,181))
    assert c.val==10
    
def test_list_mark():
    c=count()
    y=p(count.inc, c, 2)
    x=list_mark(longitude(mark.dirs.RIGHT, 60, y),
                longitude(mark.dirs.RIGHT, 180, y),
                longitude(mark.dirs.RIGHT, 300, y))
    x.cross(spherical_coord(10,85,322), spherical_coord(10,87,324))
    assert c.val==0    
    x.cross(spherical_coord(10,10,59), spherical_coord(10,10,61))
    assert c.val==2    
    x.cross(spherical_coord(10,10,179), spherical_coord(10,10,181))
    assert c.val==4
    x.cross(spherical_coord(10,10,299), spherical_coord(10,10,301))
    assert c.val==6
    x.cross(spherical_coord(10,10,179), spherical_coord(10,10,181))
    assert c.val==8

                                                                                                                                                                                                                                                                                                                                                         testsupport.py                                                                                      000644  000765  000024  00000000222 11001033272 014654  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         
class count(object):
    def __init__(self):
        self.val=0

    def inc(self):
        self.val+=1

    def dec(self):
        self.val-=1

                                                                                                                                                                                                                                                                                                                                                                              util.py                                                                                             000644  000765  000024  00000000235 10776253671 013252  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         

class display_list(list):
    def show(self):
        for i in self:
            i.show()

    def hide(self):
        for i in self:
            i.hide()
                                                                                                                                                                                                                                                                                                                                                                   wii.py                                                                                              000644  000765  000024  00000004631 10757727267 013077  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         
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
                                                                                                       wii2.py                                                                                             000644  000765  000024  00000010057 10763162425 013142  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         import sys
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
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 wii3.py                                                                                             000644  000765  000024  00000005541 10767316416 013152  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         from hip_pos_display import *

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

                                                                                                                                                               ./._wiimote.py                                                                                      000755  000765  000024  00000000305 10744021067 014310  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                             Mac OS X            	   2   �      �    BINAhDmp                          ATTR .*�   �   �   -                  �   -  com.apple.quarantine q/0000;47902237;Firefox;|org.mozilla.firefox                                                                                                                                                                                                                                                                                                                            wiimote.py                                                                                          000755  000765  000024  00000047205 10744021067 013750  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         #!/usr/bin/python
#
# wiimote.py - Wii Remote data inspector. This will be used as a learning 
# framework until we have enough data to write an actual wiimote driver.
#
# Copyright (C) 2007 Will Woods <wwoods@redhat.com>
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
#
# requires pybluez - http://org.csail.mit.edu/pybluez/

from optparse import OptionParser
import bluetooth
import os
import sys
import math
import time

import fcntl,uinput,struct

version = 0.4 # Yeah. Lame.

parser = OptionParser()

parser.add_option("-v","--verbose",action="store_true",default=False,
        help="output extra information")
parser.add_option("-d","--debug",action="store_true",default=False,
        help="output noisy debugging info")
parser.add_option("-u","--uinput",action="store_true",default=False,
        help="use uinput to synthesize mouse events from wiimote")
parser.add_option("-i","--ir",action="store_true",default=False,
        help="Enable infrared camera (not useful without sensor bar)")

(opt,argv) = parser.parse_args()

def i2bs(x):
    '''Convert a (32-bit) int to a list of 4 byte values, e.g.
    i2bs(0xdeadbeef) = [222,173,190,239]
    12bs(0x4)        = [0,0,0,4]'''
    out=[]
    while x or len(out) < 4:
        out = [x & 0xff] + out
        x = x >> 8
    return out

class WiiDiscoverer(bluetooth.DeviceDiscoverer):
    def __init__(self,maxdevs=1):
        bluetooth.DeviceDiscoverer.__init__(self) # init parent
        self.wiimotes = []
        self.done = False
        self.inprogress = False
        self.maxdevs = maxdevs

    # We identify wiimotes by their device name at the moment
    def device_discovered(self,address,device_class,name):
        if not name:
            name = bluetooth.lookup_name(address)
        if name.startswith('Nintendo RVL-CNT'):
            print "Found wiimote at address %s" % address
            w=Wiimote(address,len(self.wiimotes))
            self.wiimotes.append(w)
            if len(self.wiimotes) == self.maxdevs:
                self.done = True

    def pre_inquiry(self):
        self.inprogress = True

    def inquiry_complete(self):
        self.inprogress = False
        self.done = True

buttonmap = {
    '2': 0x0001,
    '1': 0x0002,
    'B': 0x0004,
    'A': 0x0008,
    '-': 0x0010,
    'H': 0x0080,
    'L': 0x0100,
    'R': 0x0200,
    'D': 0x0400,
    'U': 0x0800,
    '+': 0x1000,
}

# BLUH. These should be less C-ish.
CMD_SET_REPORT = 0x52

RID_LEDS = 0x11
RID_MODE = 0x12
RID_IR_EN = 0x13 
RID_SPK_EN = 0x14
RID_STATUS = 0x15
RID_WMEM = 0x16
RID_RMEM = 0x17
RID_SPK = 0x18
RID_SPK_MUTE = 0x19
RID_IR_EN2 = 0x1a

MODE_BASIC = 0x30
MODE_ACC = 0x31
MODE_IR = 0x32
MODE_FULL = 0x3e

IR_MODE_OFF =  0
IR_MODE_STD =  1
IR_MODE_EXP =  3
IR_MODE_FULL = 5

FEATURE_DISABLE = 0x00
FEATURE_ENABLE = 0x04

# Max value for IR dots
DOT_MAX = 0x3ff 

def rotate(x,y,theta):
    '''Rotates the given (x,y) coordinates by theta radians around the center
    of the dots' view'''
    # Translate dot values so the center is (0,0)
    c=(DOT_MAX/2)
    x = c - x
    y = c - y
    # rotate about the center
    xprime = x*math.cos(theta) - y*math.sin(theta)
    yprime = x*math.sin(theta) + y*math.cos(theta)
    # now retranslate
    xprime = xprime + c
    yprime = yprime + c
    return (int(xprime),int(yprime))

class Wiimote(object):
    def __init__(self,addr,number=0):
        self.connected=False
        self.done=False
        self.addr=addr
        self.number=number
        self.mode       = 0
        self.ledmask    = 0
        self.buttonmask = 0

        self.force      = [0,0,0]
        self.force_zero = [0,0,0]
        self.force_1g   = [0,0,0]
        self.force_1g_diff = [0,0,0] # Difference between zero and 1g
        self.theta_g    = 0.0 # Angle of the remote with respect to gravity,
                              # calculated from the z-axis force. In radians.
        self.theta_g_x  = 0.0 # Same, but calculated from x-axis.

        self.dots       = [(DOT_MAX,DOT_MAX),(DOT_MAX,DOT_MAX)]
        self.theta      = 0.0 # dots' angle (again, in rad) from horizontal
        self.dotlist    = []  # a fifo queue of recent dots
        self.maxdots    = 10  # max length for dotlist
        self.pointer = [0,0]  # Location of pointer. range is (0,DOT_MAX)

        self.rx = bluetooth.BluetoothSocket(bluetooth.L2CAP)
        self.cx = bluetooth.BluetoothSocket(bluetooth.L2CAP)
    def connect(self):
        if opt.verbose: 
            print "Attaching to Wiimote #%i at %s" % (self.number+1,self.addr)
        self.rx.connect((self.addr,19))
        self.cx.connect((self.addr,17))
        self.setled(self.number)
        self.connected=True
    def disconnect(self):
        if opt.verbose: print "Disconnecting from Wiimote #%i"%(self.number+1)
        self.cx.close()
        self.rx.close()
        self.connected=False
    def mainloop(self):
        if opt.verbose: print "Receiving data from Wiimote #%i"%(self.number+1)
        while not self.done:
            self._getpacket()
            if opt.verbose and not self.done: self.showstatus()
        if opt.verbose: print
    def _handle_button_data(self,data):
        if len(data) != 4: return False
        # XXX: what's byte 1 for?
        newmask = (ord(data[2])<<8)+ord(data[3])
        # TODO: check newmask against current mask and send events?
        if newmask & buttonmap['H'] and not self.buttonmask & buttonmap['H']:
            print "Re-enabling IR"
            self.enable_IR()
        self.buttonmask = newmask
    def _handle_force_data(self,data):
        if len(data) != 3: return False
        self.force = [ord(d) for d in data]
        return True
    def _handle_IR_data(self,data):
        if len(data) != 6: return False
        if data ==' \xff'*6:
            self.dots=[(DOT_MAX,DOT_MAX),(DOT_MAX,DOT_MAX)]
        else:
            a,b,c,d,e,f = [ord(d) for d in data]
            # processing dots:
            # each tuple is 3 bytes in the form: x,y,extra
            # extra contains 8 bits of extra data as follows: [yyxxssss]
            # x and y are the high two bits for the full 10-bit x/y values.
            # s is some unknown info (size data?)
            x1=a+((c & 0x30) << 4) 
            y1=b+((c & 0xc0) << 2)
            x2=d+((f & 0x30) << 4)
            y2=e+((f & 0xc0) << 2)
            self.dots=[(x1,y1),(x2,y2)]
            self.dotlist.insert(0,self.dots)
            if len(self.dotlist) > self.maxdots:
                self.dotlist.pop()
        return True
    def _getpacket(self):
        data=self.rx.recv(1024)
        if len(data) == 4:    # button
            self._handle_button_data(data)
        elif len(data) == 7:  # button + accelerometer
            self._handle_button_data(data[0:4])
            self._handle_force_data(data[4:7])
        elif len(data) == 19: # button + accel + IR
            self._handle_button_data(data[0:4])
            self._handle_force_data(data[4:7])
            self._handle_IR_data(data[7:13])
            # I think the extra data is emitted if we see more than two dots
            extradata = data[13:19]
            if opt.debug and (extradata != "\xff"*len(extradata)):
                print "Interesting extradata: %s\n" % extradata.encode("hex")
        elif len(data) == 0:  # Wiimote went away!
            if opt.debug: print "Lost wiimote #%i" % (self.number+1)
            self.done = True
        else:
            print "Unknown packet len %i: 0x%s" % (len(data),data.encode("hex"))
    def setled(self,num):
        if opt.debug: print "setled(%i)" % num
        if num < 4:
            self.ledmask = self.ledmask | (0x10 << num)
            self._led_command()
    def clearled(self,num):
        if opt.debug: print "clearled(%i)" % num
        if num < 4:
            self.ledmask = self.ledmask & ~(0x10 << num)
            self._led_command()
    def buttons_str(self):
        buttonlist='+UDLRH-AB12'
        out=''
        for c in buttonlist:
            if not self.buttonmask & buttonmap[c]:
                c = '.'
            out = out + c
        return out
    def force_str(self):
        return "(% 4i,% 4i,% 4i)" % (self.force[0]-self.force_zero[0],
                                  self.force[1]-self.force_zero[1],
                                  self.force[2]-self.force_zero[2])
    def dots_str(self):
        (a,b),(c,d) = self.dots
        return "((%4i,%4i),(%4i,%4i))" % (a,b,c,d)
    def status_str(self):
        return "%s force=%s dots=%s" % \
                (self.buttons_str(),self.force_str(),self.dots_str())
    def showstatus(self):
        sys.stdout.write(self.status_str() + "\r")
        sys.stdout.flush()
    def setmode(self,mode):
        self.mode = mode
        # XXX wiimotulator.py has flags for setting 0x01 in the first byte for 
        # 'rmbl' and 0x04 for 'cont'. Both of these are always off.
        # No idea why.
        self._send_command(CMD_SET_REPORT,RID_MODE,[0,mode])
    def enable_force(self):
        self.setmode(self.mode | MODE_ACC)
        self.get_force_calibration()
    def enable_IR(self):
        self.setmode(self.mode | MODE_IR)
        self._send_command(CMD_SET_REPORT,RID_IR_EN,[FEATURE_ENABLE])
        self._send_command(CMD_SET_REPORT,RID_IR_EN2,[FEATURE_ENABLE])
        # Enable IR device
        self._write_mem(0x04b00030,[0x01])
        # Set sensitivity constants
        self._write_mem(0x04b00030,[0x08])
        self._write_mem(0x04b00006,[0x90])
       	self._write_mem(0x04b00008,[0xc0])
        self._write_mem(0x04b0001a,[0x40])
        self._write_mem(0x04b00033,[0x33])
        # Enable IR data output
        self._write_mem(0x04b00030,[8])
    def get_force_calibration(self):
        data=[ord(b) for b in self._read_mem(0x16,10)]
        self.force_zero = data[0:3]
        self.force_1g   = data[4:7]
        # XXX currently we don't know what data[3], data[7], or data[8:9] are
        if opt.debug: print "Got force calibration data: zero=%s, 1g=%s" % \
                (self.force_zero,self.force_1g)
        # Calculate the difference between zero and 1g for each axis
        for b in range(0,3):
            self.force_1g_diff[b] = self.force_1g[b] - self.force_zero[b]

    def _led_command(self):
        self._send_command(CMD_SET_REPORT,RID_LEDS,[self.ledmask])
    def _waitforpacket(self,header,max=32):
        r=''
        n=0
        while (n<max) and not r.startswith(header):
            r = self.rx.recv(1024)
            n = n + 1
        if opt.debug: print "Leaving _waitforpacket() after %i packets" % n
        if not r.startswith(header):
            return None
        else:
            return r
    def _waitforok(self):
        self._waitforpacket('\xa1\x22\x00')
    def _read_mem(self,offset,size):
        if size >= 16:
            print "ERROR: _read_mem can't handle size > 15 yet"
            return None
        # RMEM command wants: [offset,size]
        self._send_command(CMD_SET_REPORT,RID_RMEM,i2bs(offset)+[0,size])
        data = self._waitforpacket('\xa1\x21')
        if data:
            # TODO check error flag, continuation, etc
            return data[7:]
        else:
            return None
    def _write_mem(self,offset,data):
        # WMEM command wants: [offset,size,data]
        # offset = 32-bit, bigendian. data is 0-padded to 16 bytes.
        size = len(data)
        if size > 16: return False # Too much data!
        if size < 16: data = data + [0]*(16-size)
        self._send_command(CMD_SET_REPORT,RID_WMEM,i2bs(offset)+[size]+data)
        self._waitforok()
    def _send_command(self,cmd,report,data):
        if opt.debug: print "_send_command(%#x,%#x,%s)" % (cmd,report,data)
        self.cx.send(chr(cmd) + chr(report) + "".join([chr(d) for d in data]))
    def calc_theta_g(self):
        '''Use the z and x accelerometer values to figure out the wiimote's
        orientation with respect to gravity.'''
        # sanity - return if we have no calibration data
        if self.force_1g[0] == 0: return self.theta_g
        # rotating from face-up to upside-down, force[2] goes from 
        # force_1g[2] to force_zero[2]-force_1g[2]. The normal force of
        # gravity should be force_zero-force_1g - call this 'g'.
        # It seems intuitive that this should map to a cosine wave - we start
        # at 1g for face-up, then zero for a quarter-turn, -1g for half, etc.
        zg = float(self.force[2]-self.force_zero[2])/self.force_1g_diff[2]
        # If we're seeing more than 1g, probably this data isn't reliable
        # for determining orientation, so we ignore it
        if abs(zg) <= 1.0:
            self.theta_g = math.acos(zg)
        # Do the same thing with force[0] - it goes from 0->+/-1g->0, just like
        # a sine wave
        xg = float(self.force[0]-self.force_zero[0])/self.force_1g_diff[0]
        if abs(xg) <= 1.0:
            self.theta_g_x = math.asin(xg)
        # For convenience, return theta_g
        return self.theta_g

    def calc_pointer(self):
        '''Calculate the position of the pointer, taking into account the 
        rotation of the controller.
        Sets self.theta and self.pointer; returns self.pointer.'''
        # Credit for most of the math here goes to my esteemed colleague Mike
        # (mikem@redhat.com). Finally, all those years TA-ing Calc 1 are
        # paying off!
        # One of the dots is bogus/missing. Bail out.
        # TODO: keep track of the previous dot positions and guess instead of
        # immediately bailing out?
        if (DOT_MAX,DOT_MAX) in self.dots:
            return self.pointer
        ((x1,y1),(x2,y2)) = self.dots
        # FIXME: for some reason, py never goes above ~750.
        # Might be my bogus IR emitters (half-power every 15 degrees
        # away from center! Thanks, Radio Shack.) 
        # But it might also be that the IR camera is calibrated to 
        # assume the sensor bar should be on the bottom of the TV.
        # Since IR calibration is still Black Magick, I am forcing
        # a scale factor for y here.
        y1 = y1 * DOT_MAX / 760
        y2 = y2 * DOT_MAX / 760

        # Determine rotation angle. SOH CAH TOA ftw.
        if (x1 != x2):
            self.theta = math.atan(float(y2-y1)/float(x1-x2))
        else:
            self.theta = math.pi/2
            if y1 > y2:
                self.theta = -self.theta
        # If the accel. says we are upside-down, add half a turn to theta
        tg = math.degrees(self.calc_theta_g())
        if tg > 90.0:
            self.theta = self.theta+math.pi
        if tg < -90.0:
            self.theta = self.theta-math.pi
        # rotate dots around center by theta.
        (x1,y1) = rotate(x1,y1,self.theta)
        (x2,y2) = rotate(x2,y2,self.theta)
        # They should now be horizontal (y1 should be very close to y2).
        # Average the two X values (find the center between them)
        px = (x1+x2)/2
        # Horizontal means y1 = y2, so there's no need to average them.
        # In fact, let's output an error message if the rotate messed up.
        if y2 != y1:
            print "post-rotation Y delta=%i" % abs(y1-y2)
        # We do need to flip the incoming y data.
        py = DOT_MAX - y1

        # Do some scaling - ignore the outer edges of the screen
        # FIXME: fix scaling such that the center of the wiimote image
        # maps to the top of the screen
        # Center point of the screen is (c,c)
        c = DOT_MAX/2
        maxd = 0.33 * DOT_MAX # max allowable distance from center
        # If this point is less than (maxd) from the center of the image,
        # draw it.
        if (abs(px-c) <= maxd) and (abs(py-c) <= maxd):
            # px/py are in the range [c-maxd,c+maxd]
            px = px - (c-maxd)
            py = py - (c-maxd)
            # Now they're in the range [0,2*maxd]. Scale to DOT_MAX.
            px = px * (DOT_MAX/(2*maxd))
            py = py * (DOT_MAX/(2*maxd))
            # Hooray! We did it!
            self.pointer = [int(px),int(py)]
        return self.pointer
    def pointer_str(self):
        return "(%4i,%4i)" % (self.pointer[0],self.pointer[1])

def find_uinput():
    for n in ("/dev/uinput","/dev/input/uinput","/dev/misc/uinput"):
        if os.path.exists(n):
            return n
    return None

def init_uinput(dev):
    # Refs: http://svn.navi.cx/misc/trunk/python/uinput_test.py
    #       http://blog.davr.org/ + http://davr.org/wiimotulator.py.txt
    #       http://www.popies.net/ams/ (ABS_[XY] device used as mouse)
    fd = os.open(dev,os.O_RDWR)
    # Write the user device info
    absmax  = [0] * (uinput.ABS_MAX+1)
    absmin  = [0] * (uinput.ABS_MAX+1)
    absfuzz = [0] * (uinput.ABS_MAX+1) 
    absflat = [0] * (uinput.ABS_MAX+1) 
    absmax[uinput.ABS_X] = DOT_MAX 
    absmax[uinput.ABS_Y] = DOT_MAX
    absfuzz[uinput.ABS_X] = 2
    absfuzz[uinput.ABS_Y] = 2
    user_dev_data = struct.pack(uinput.user_dev_pack,"Nintendo Wiimote",
            uinput.BUS_USB,1,1,1,0,*(absmax + absmin + absfuzz + absflat))
    if opt.debug:
        print "user_dev_data: %s" % user_dev_data.encode("hex")
    os.write(fd,user_dev_data)
    # Set the event bits
    fcntl.ioctl(fd,uinput.UI_SET_EVBIT,  uinput.EV_ABS)
    fcntl.ioctl(fd,uinput.UI_SET_ABSBIT, uinput.ABS_X)
    fcntl.ioctl(fd,uinput.UI_SET_ABSBIT, uinput.ABS_Y)
    fcntl.ioctl(fd,uinput.UI_SET_EVBIT,  uinput.EV_KEY)
    fcntl.ioctl(fd,uinput.UI_SET_EVBIT,  uinput.EV_SYN)
    fcntl.ioctl(fd,uinput.UI_SET_KEYBIT, uinput.BTN_MOUSE)
    # TODO: Other bits...
    # Create the device!
    fcntl.ioctl(fd,uinput.UI_DEV_CREATE)
    return fd

def destroy_uinput(fd):
    fcntl.ioctl(fd,uinput.UI_DEV_DESTROY)

def uinput_event(fd,evtype,code,value):
    os.write(fd,struct.pack(uinput.event_pack,time.time(),0,evtype,code,value))

def uinput_abs_report(fd,point):
    uinput_event(fd,uinput.EV_ABS,uinput.ABS_X,DOT_MAX-point[0])
    uinput_event(fd,uinput.EV_ABS,uinput.ABS_Y,DOT_MAX-point[1])
    uinput_event(fd,uinput.EV_SYN,0,0)
        
if __name__ == '__main__':
    # Do this early so we bail out early if you're not root..
    if opt.uinput:
        uinput_dev = find_uinput()
        if uinput_dev:
            print "Found uinput dev at %s" % uinput_dev
        else:
            print "Could not open uinput dev. (Are you root? Is uinput loaded?)"
            sys.exit(1)

    print "Scanning for wiimotes - press 1+2 to make your wiimote discoverable."

    #d = WiiDiscoverer()
    #d.find_devices()
    #while not d.done:
    #    d.process_event()
    #if not d.wiimotes:
    #    print "No wiimotes found."
    #    sys.exit(1)
    #wiimotes=d.wiimotes
    #for w in wiimotes:
    #    w.connect()

    # Just connect to my wiimote
    w=Wiimote("00:17:AB:29:7B:2A",0)

    w.connect()
    print "Enabling accelerometer."
    w.enable_force()

    if opt.ir:
        print "Turning on IR camera."
        w.enable_IR()

    if opt.uinput and uinput_dev:
        print "Initializing uinput device."
        fd = init_uinput(uinput_dev)

    try:
        last=time.time()
        while not w.done:
            w._getpacket()
            w.showstatus()
            if uinput:
                t = time.time()
                if (t - last) > 0.03:
                    last = t
                    w.calc_pointer()
                    uinput_abs_report(fd,w.pointer)
    finally:
        w.disconnect()
        if uinput_dev: 
            destroy_uinput(fd)
            os.close(fd)
                                                                                                                                                                                                                                                                                                                                                                                           x.py                                                                                                000644  000765  000024  00000005101 10757734177 012545  0                                                                                                    ustar 00jstoner                         staff                           000000  000000                                                                                                                                                                         Python 2.5.1 (r251:54863, Dec 31 2007, 17:07:30) 
[GCC 4.0.1 (Apple Inc. build 5465)] on darwin
Type "copyright", "credits" or "license()" for more information.

    ****************************************************************
    Personal firewall software may warn about the connection IDLE
    makes to its subprocess using this computer's internal loopback
    interface.  This connection is not visible on any external
    interface and no data is sent to or received from the Internet.
    ****************************************************************
    
IDLE 1.2.1      
>>> ================================ RESTART ================================
>>> 
>>> ================================ RESTART ================================
>>> 
>>> ================================ RESTART ================================
>>> 
>>> ================================ RESTART ================================
>>> 
>>> from wii import *

Traceback (most recent call last):
  File "<pyshell#0>", line 1, in <module>
    from wii import *
  File "/Users/jstoner/Documents/projects/boogiepants/wii.py", line 47, in <module>
    class wii_remote_discovery_delegate(NSObject):
error: wii_remote_discovery_delegate is overriding existing Objective-C class
>>> ================================ RESTART ================================
>>> 
not discovered, error  188
>>> ================================ RESTART ================================
>>> 
not discovered, error  188
>>> ================================ RESTART ================================
>>> 
not discovered, error  188
>>> ================================ RESTART ================================
>>> 
>>> ================================ RESTART ================================
>>> 
>>> ================================ RESTART ================================
>>> 
>>> ================================ RESTART ================================
>>> 
not discovered, error  268435459
>>> ================================ RESTART ================================
>>> 
not discovered, error  268435459
>>> ================================ RESTART ================================
>>> 
>>> ================================ RESTART ================================
>>> 
>>> ================================ RESTART ================================
>>> 
>>> ================================ RESTART ================================
>>> 
not discovered, error  268435459
>>> ================================ RESTART ================================
>>> 
not discovered, error  -536870195
>>> ================================ RESTART ================================
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               