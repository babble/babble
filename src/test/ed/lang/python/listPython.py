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

__pyL = [1, 2];

def getList():
    return __pyL

def pyManipList(l): # expecting [1, 3, 5]
    if len([x for x in l if x % 3 == 0]) != 1: return False

    if l.count(3) != 1: return False


    return True

def pyGetLength(l):
    return len(l)

def pyCheckEven(l, n):
    return l[n] % 2 == 0

def pyGetSecretLength():
    return len(__pyL)
