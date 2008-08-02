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

var c = new PythonClass("hello");

assert( c.myattr == "hello" );
assert( c.allattr == "puppies" );
assert( c.meth1() == 123 );
assert( c.attr2 == 'myattr' );

var c2 = new PythonClassicClass("goodbye");

assert( c2.myattr == "goodbye" );
assert( c2.allattr == "kitties" );
assert( c2.meth1() == "classic" );
assert( c2.attr2 == 989 );

var c3 = new Callable(21);

assert( c3( 2 ) == 23 );
assert( c3( 1 ) == 22 );
assert( c3( -1 ) == 20 );

c3.valueOf(); // this should at least not throw an exception

// unknown attribute -> null (or undefined in real JS)
assert( c3.randomSuperLongUnusedAttributeNameThatNoOneWillEverUse == null );

try{
    var c4 = Callable(141);
}
catch(e if (new String(e.getClass()) == "class java.lang.UnsupportedOperationException") ) {
}

assert( e );

e = null;
// Make sure that we don't accidentally replace a wrapped object
try {
    c3.zoom(function(){ return Callable(11); });
}
catch(e){
    print("Hi " + e.getClass());
}

assert(e);
assert( c3( 1 ) == 22 );

assert( pyc1( 1, 4, 9, 16 ) == 9 );
