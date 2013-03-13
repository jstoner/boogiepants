from visual import *
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

