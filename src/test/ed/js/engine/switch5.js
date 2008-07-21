
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

a = 5;
b = 5;

switch ( a ){
case b: print( "here" ); break;
case 6: print( "6" );
default: print( "d" );
}


function foo1( z ){
    a = 5;
    switch( z ){
    case 1 : print( "a" ); break;
    case 2 : print( "b" );
    case 3 : print( "c" ); break;
    case 4 : print( "d" );
    case a : print( "d" );
    default: print( "ZZ" );
    }
}

foo1( 0 );
foo1( 1 );
foo1( 2 );
foo1( 3 );
foo1( 4 );
foo1( 6 );
foo1( 5 );


function foo( z ){
    e = "e";
    switch( z ){
    case "a" : print( "a" ); break;
    case "b" : print( "b" );
    case "c" : print( "c" ); break;
    case "d" : print( "d" );
    case e : print( "d" );
    default: print( "ZZ" );
    }
}

foo( "a" );
foo( "b" );
foo( "c" );
foo( "d" );
foo( "e" );
foo( "sad" );
