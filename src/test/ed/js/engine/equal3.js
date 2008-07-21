
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

print( 3 == 3 );
print( 3.0 == 3 );

print( 3.0 == "3" );
print( 3 == "3" );
print( 3 == "3.0" );

print( "abc" === "abc" );
print( "abc" == "abc" );
print( "abc" === "abcd" );
print( "abc" == "abcd" );

print( { a : "b" } == { a : "b" } );
print( { a : "b" } === { a : "b" } );

print( { a : "b" } == { a : "c" } );
print( { a : "b" } === { a : "c" } );


print( 3.0 != "3" );
print( 3 != "3" );
print( 3 != "3.0" );

print( "abc" !== "abc" );
print( "abc" != "abc" );
print( "abc" !== "abcd" );
print( "abc" != "abcd" );
