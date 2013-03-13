#!/usr/bin/python

from mark import *
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


if __name__ == '__main':
    boogiepants()
