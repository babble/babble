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

db = _10gen.connect( "test" )
t = db.pydb5
t.drop()

class Test(object):
    def __init__(self, name='not set'):
        self.a = 1
    def postLoad(self):
        self.c = 3

j = Test()

t.setConstructor(Test)
t.save(j)
j2 = t.findOne()
_10gen.assert.eq( j2.a , 1 )
_10gen.assert.eq( j2.c , 3 )

