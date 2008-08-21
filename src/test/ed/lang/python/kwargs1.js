/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

local.src.test.ed.lang.python.kwargs1py();

var result = pyFoo.apply(null, [1, 2, 3], {a: 'a', b: 'b'});

var args = result[0];
var kwargs = result[1];
assert.eq( args.length , 3 );
assert.eq( args[0] , 1 );
assert.eq( args[1] , 2 );
assert.eq( args[2] , 3 );

assert.eq( kwargs.a , 'a' );
assert.eq( kwargs.b , 'b' );
