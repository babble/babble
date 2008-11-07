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

# Used by both expose1_test and expose2_test
from _10gen import getglobal
import _10gen
_10gen.pyX = getglobal('x')
_10gen.pyY = getglobal('y')

def pythonAddAttr(obj, k, v):
    setattr(obj, k, v)

def pythonAddFoo(obj):
    obj.foo = "yippee"

import _10gen
_10gen.pythonAddAttr = pythonAddAttr
_10gen.pythonAddFoo = pythonAddFoo

if hasattr(_10gen, 'jsObj'):
    _10gen.jsObj.pyBool = True
    _10gen.jsObj.pyLong = 123L
