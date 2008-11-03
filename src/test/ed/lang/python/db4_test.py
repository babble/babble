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

db = _10gen.connect( "test" );
t = db.pydb4;
t.drop();

class Test(object):
    def __init__(self, name='not set'):
        self.name = name
        self.gpa = 0
    _transientFields = ['gpa']

j = Test('Jim')
j.gpa = 4.0

t.setConstructor(Test)
t.save(j)
j2 = t.findOne()
_10gen.assert( j2 )

_10gen.assert( not j2.gpa , '_transientFields broken' )

j3 = t.findOne({'_id': str(j._id)})
#_10gen.assert( j3 , '_id not working yet')
