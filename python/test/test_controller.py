from controller import *

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
