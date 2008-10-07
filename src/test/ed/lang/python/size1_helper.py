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

pyBoolean = True

pyInt = 1254

pyString = 'hello this is a string'
pyString2 = pyString + ' with some appended text'

def pyFunction():
    return 15

class pyClass(object):
    x = 14

pyDict1 = {'a': 'b'}

pyDict2 = {pyString : pyString2}

def pyModifyDict2():
    pyDict2[pyString] = pyString2

def pyModifyDict3():
    pyDict2[pyString2] = pyString2
