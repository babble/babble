
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

var a = 4;
var b = 4;
var c = 5;

if ( a == b ){
    print( "good 1" );
}
else {
    print( "bad 1" );
}

if ( a == c ){
    print( "bad 2" );
}
else {
    print( "good 2" );
}

function foo( a , b ){
    if ( a == b ){
        print( "was true" );
    }
    else {
        print( "was false" );
    }
}

foo( a , b );
foo( a , c );

var d = a == b;
print( d );

if ( d ){
    print( "good 3" );
}

if ( ! d ){
    print ( "bad 4" );
}

var e = a == c;
if ( e ){
    print( "bad 5" );
}

if ( e ){
    print( "e" );
}
else if ( a ){
    print( "a" );
}
else {
    print( "else" );
}


if ( e ) print( "e" );
else if ( a ) print( "a" );
else if ( c );
else if ( d ) print( "a" );

var a = 5;
var b = 6;
print( a || b );

a = { };
print( ! ( a || a.b.c ) );

a = null;
print( ( ! a || a.asd ) );


a = 2;
print( a );
if ( a = 1 ){
    print( "hi" );
}
print( a );
