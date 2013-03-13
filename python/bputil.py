
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
