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

class C(object):
    pass

pythonObj1 = C()
pythonObj1.a = 1
pythonObj1.b = 2
pythonObj1.c = 4
pythonObj1.d = 8

pythonObj2 = C()
pythonObj2.foo = "hi"
pythonObj2.bar = "hello"
pythonObj2.baz = "yo"

def pythonDelete(o, attr):
    delattr(o, attr)
