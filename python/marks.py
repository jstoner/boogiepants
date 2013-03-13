from bputil import *
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
