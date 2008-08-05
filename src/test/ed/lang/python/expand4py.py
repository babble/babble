'''
    Copyright (C) 2008 10gen Inc.
  
    This program is free software: you can redistribute it and/or  modify
    it under the terms of the GNU Affero General Public License, version 3,
    as published by the Free Software Foundation.
  
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.
  
    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
'''

print o

if o:
    print "yay"
else:
    assert 0 , "shit"

assert o.a == 1
assert o.b == 4

assert dir(o) == ['a', 'b']

def foo(a=None, b=None, **kwds):
    assert a is not None
    assert b is not None

foo(**o)

def bar(c=3, a=None, b=None, **kwds):
    assert a is not None
    assert b is not None
    assert c == 3

bar(**o)

class Foo(object):
    def __init__(self, c=3, a=None, b=None, **kwds):
        self.a=a
        self.b = b
        self.c = c


afoo = Foo(c=4, **o)

assert afoo.a is not None
assert afoo.b is not None
assert afoo.c == 4
