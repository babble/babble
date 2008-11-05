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

from _10gen import db, local
cursor = db.coll.find()

local.src.test.ed.lang.python.dbcursor1_template({
        'result': {
            'search_results' : cursor.toArray()
            }
        })

cursor = db.coll.find()

i = 0
for obj in cursor:
    assert obj.funny == 1
    i += 1

assert( i != 0 )
