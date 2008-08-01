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

local.src.test.ed.lang.python.delete1py();

var x = { a: 1, b: 2, c: 4, d: 8 };

assert( Object.keys(x).length == 4 );

pythonDelete( x , 'a' );
assert( Object.keys(x).length == 3 );

assert( Object.keys(pythonObj1).length == 4 );

delete pythonObj1.a;
assert( Object.keys(pythonObj1).length == 3 );

delete pythonObj1.b;
assert( Object.keys(pythonObj1).length == 2 );

assert( Object.keys(pythonObj2).length == 3 );
delete pythonObj2['foo'];
assert( Object.keys(pythonObj2).length == 2 );

var y = { $foo: 21 };

assert( Object.keys(y).length == 1 );

pythonDelete( y , '$foo' );

assert( Object.keys(y).length == 0 );

