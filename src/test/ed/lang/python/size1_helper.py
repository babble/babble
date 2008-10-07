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
_10gen.pyBoolean = True

_10gen.pyInt = 1254

_10gen.pyString = 'hello this is a string'
_10gen.pyString2 = _10gen.pyString + ' with some appended text'

def pyFunction():
    return 15
_10gen.pyFunction = pyFunction

class pyClass(object):
    x = 14
_10gen.pyClass = pyClass

_10gen.pyDict1 = {'a': 'b'}

_10gen.pyDict2 = {_10gen.pyString : _10gen.pyString2}

def pyModifyDict2():
    _10gen.pyDict2[_10gen.pyString] = _10gen.pyString2

def pyModifyDict3():
    _10gen.pyDict2[_10gen.pyString2] = _10gen.pyString2
_10gen.pyModifyDict2 = pyModifyDict2
_10gen.pyModifyDict3 = pyModifyDict3
