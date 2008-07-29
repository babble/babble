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

local.src.test.ed.lang.python.module2();

assert(somePythonFoo(4) == 5);
assert(somePythonBar("kitties") == "kitties is great");

var c = PythonClass("hello");

assert( c.myattr == "hello" );
assert( c.allattr == "puppies" );
assert( c.meth1() == 123 );
assert( c.attr2 == 'myattr' );

var c2 = PythonClassicClass("goodbye");

assert( c2.myattr == "goodbye" );
assert( c2.allattr == "kitties" );
assert( c2.meth1() == "classic" );
assert( c2.attr2 == 989 );
