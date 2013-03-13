from mark import *

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

