
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

a = 1;
b = 2;

assert( 3 == scope.eval( "a + b" ) );

a = { b : 2 };
assert( 2 == scope.eval( "a.b" ) );

a = { b : { c : 3 } };
assert( 3 == scope.eval( "a.b.c" ) );


scope.eval( "x = 5" );
assert( 5 == x );

var y = 5;
assert( 5 == y );
scope.eval( "y =6;" );
assert( 6 == y );
