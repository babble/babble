
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
a += 1;
print( a );

b = Object();
b.a = 1;
b.a += 2;
print( b.a );

var c = 0;
function foo(){
    var d = c;
    c = c + 1;
    if ( d == 0 ){
        return "a";
    }
    if ( d == 1 )
        return "b";
    if ( d == 2 )
        return "c";
    if ( d == 3 )
        return "d";

    return "e";
}
b[ foo() ] += 4;
print( b.a );


a = 5;
f = ( a += 1 );
print( f );
print( a );

a = Object();
a.a = 32;
f = ( a.a += 1 );
print( f );
print( a.a );

a = 5;
print( a++ + " " + a++ );

a = 17;
print( ++a );
print( a );

a = Object();
a.a = 100;
print( a.a++ );

function bar( stupid ){
    print( stupid );
    stupid++;
    print( stupid );
}

bar( 500 );
