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

var b = 17;
print( b );
for ( var i = 5 ; i >= 0 ; i = i + -1 ){
    print( i );
    var b = i;
}
print( b );


var o = Object();
o.a = 5;
o.b = 6;
for ( bar in o ){
    print( bar );
}

function silly( ooo ){
    for ( var i in ooo ){
        print( i + " : " + ooo[i] );
    }
}

silly( o );
