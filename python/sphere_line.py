from visual import *		
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
