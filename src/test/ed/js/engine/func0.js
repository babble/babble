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

function foo(){
    return 5;
}
print( foo() );

function foo2( a ){
    return a;
}
print( foo2( 4 ) );

function foo3( a ){
    a = a + 1;
    return a;
}
print( foo3( 9 ) );


function foo4( a ){
    b = a + 1;
    return b;
}
print( foo4( 10 ) );


function foo5( a ){
    a = a + 1;
    var c = a + 2;
    c = c + 1 ;
    return c;
}
print( foo5( 10 ) );


function foo6( a ){
    var c = a + 2;
    c = c + 1;
    return c;
}
print( foo6( 13 ) );
