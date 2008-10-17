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

import os

def exec_orig_wsgi(filename, globals):
    d = os.path.dirname(__file__) # ../src/main/ed/lang/python/lib/wsgiref/
    
    for i in range(7):
        d = os.path.dirname(d)

    # d = ../
    execfile(os.path.join(d, 'include', 'jython', 'Lib', 'wsgiref', filename), globals)

