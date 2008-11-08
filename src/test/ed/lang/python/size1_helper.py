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

@_10gen.exposePy
def pyFunction():
    return 15

class pyClass(object):
    x = 14
_10gen.pyClass = pyClass

_10gen.pyDict1 = {'a': 'b'}

_10gen.pyDict2 = {_10gen.pyString : _10gen.pyString2}

@_10gen.exposePy
def pyModifyDict2():
    _10gen.pyDict2[_10gen.pyString] = _10gen.pyString2

@_10gen.exposePy
def pyModifyDict3():
    _10gen.pyDict2[_10gen.pyString2] = _10gen.pyString2

@_10gen.exposePy
def pyModifyDict4():
    _10gen.pyDict2[_10gen.pyString2] = _10gen.pyString

_10gen.pyList1 = [1, 2]
_10gen.pyList2 = [1, 1]
_10gen.pyList3 = [1, 3]

