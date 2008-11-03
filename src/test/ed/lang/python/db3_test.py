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
t = db.pydb3;
t.drop();

orig= { "name" : "a" , "things" : [ 1 , 2 ] };
_10gen.assert.eq( 2 , len( orig["things"] ) );

orig["things"].append( 3.1 );
_10gen.assert.eq( 3 , len( orig["things"] ) );

t.save( orig );
after = t.findOne();

_10gen.assert.eq( 3 , len( after["things"] ) );


after["things"].append( 4 );
_10gen.assert.eq( 4 , len( after["things"] ) );

after['things'][1] = 9

_10gen.assert.eq( sum(after['things']), 17.1 )

_10gen.assert.eq( str(after['things']), str([1, 9, 3.1, 4]) )
_10gen.assert.eq( repr(after['things']), repr([1, 9, 3.1, 4]) )
