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

def pythonGetInstanceAttr(cls, attr):
    n = cls()
    return getattr(n, attr)

def pythonGetClassAttr(cls, attr):
    return getattr(cls, attr)

def pythonExtend(cls):
    def method(self, arg1):
        return self.foo + arg1
    cls.pyMeth = method

    cls.pyList = [-1, 2, -3];


pyStr = str   # deliberately expose
pyCallable = callable
pyObject = object
