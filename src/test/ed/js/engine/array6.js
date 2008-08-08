
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

a = new Array( 5 )
print( a.length );
a.push( "abc" );
print( a.length );
print( a[a.length-1] );

b = new Array( a );
print( b.length );
print( b[0] );

// ---

a = [ 1 , 2 , 3 ]
print( a );

a.foo = 7
print( a.foo );
print( a.foo ? "yes" : "no" );
delete( a.foo );
print( a.foo ? "yes" : "no" );

delete a[1]
print( a );
