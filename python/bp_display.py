from visual.controls import *
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

