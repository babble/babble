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

import _10gen
def somePythonFoo(x):
    return x+1
_10gen.somePythonFoo = somePythonFoo

def somePythonBar(y):
    return "%s is great"%y
_10gen.somePythonBar = somePythonBar

class PythonClass(object):
    def __init__(self, x):
        self.myattr = x
        self.allattr = "puppies"

    def meth1(self):
        return 123

    attr2 = 'myattr'
_10gen.PythonClass = PythonClass

class PythonClassicClass:
    def __init__(self, x):
        self.myattr = x
        self.allattr = "kitties"

    def meth1(self):
        return "classic"

    attr2 = 989
_10gen.PythonClassicClass = PythonClassicClass

class Callable(object):
    def __init__(self, y):
        self.y = y

    def __call__(self, x):
        return x + self.y

    def zoom(self, f):
        return f()
_10gen.Callable = Callable

_10gen.pyc1 = pyc1 = PythonClassicClass(None)


def call_c1(*args):
    return args[2]
pyc1.__call__ = call_c1
