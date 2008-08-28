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

def somePythonFoo(x):
    return x+1

def somePythonBar(y):
    return "%s is great"%y

class PythonClass(object):
    def __init__(self, x):
        self.myattr = x
        self.allattr = "puppies"

    def meth1(self):
        return 123

    attr2 = 'myattr'

class PythonClassicClass:
    def __init__(self, x):
        self.myattr = x
        self.allattr = "kitties"

    def meth1(self):
        return "classic"

    attr2 = 989

class Callable(object):
    def __init__(self, y):
        self.y = y

    def __call__(self, x):
        return x + self.y

    def zoom(self, f):
        return f()

pyc1 = PythonClassicClass(None)

def call_c1(*args):
    return args[2]
pyc1.__call__ = call_c1
