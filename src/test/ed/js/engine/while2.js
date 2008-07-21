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

var foo = 5;
var bar = true;

while ( foo && bar ){
    print( foo );
    foo = foo + -1;
}

do {
    print( foo );
} while ( foo );

t1:
while( bar ){
    print( "once 1" );
    break t1;
}

while( bar ){
    print( "once 2" );
    break;
}

foo = 2;
while ( foo >= 0 ){
    foo = foo + -1;
    if ( foo != 0 )
        continue;
    print( "once 3" );
}


a = 1;
c = 0;
while ( a < 5 ){
    b = a;
    a = a + 1
}
print( a );
print( b );
print( c );
