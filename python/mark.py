from bputil import *
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
