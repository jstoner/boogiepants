from bp_display import *
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
