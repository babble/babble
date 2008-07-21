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

var a = 5;
a |= 11;
print( a );

a = 5;
a |= 11;
print( a );

a = 5;
a |= "11";
print( a );

a = 5;
a |= "eleven";
print( a );

a = "five";
a |= 11;
print( a );

a = "five";
a |= "eleven";
print( a );

print( 5 | 11 );
print( 5 | "11" );
print( 5 | "a11" );

foo = Object();
foo.a = 5;
foo.a |= 11;
print( foo.a );

a = 4;
b = 1;
print( a >> b );

a = 4;
b = 1;
print( a << b );

print( 11 % 5 );
print( "a" % 5 );
print( 11 % "a" );

print( ( 4 >> 1 ) );
print( ( 4 >>> 1 ) );

print( 5 > 6 );
print( 5 < 6 );
print( 2 & 10 );
print( ~10 );
print( ~"aasd" );
print( 2 ^ 10 );

var a = 12;
print( a * 2 );
print( a * "asd" );

print( a == 12 ? "good" : "bad" );
print( a != 12 ? "bad" : "good" );

a = b;
print( a & 5 );
a = "qsdsad";
print( a & 5 );
