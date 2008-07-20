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

var i = 5;
for ( var i=0; i<2; i = i + 1 ){
    var b = i;
    print( b );
}
for ( var ii=0; ii<2; ii = ii + 1 ){
    var b = ii;
    print( b );
}
print( i );

var i = 17;
function foo(){
    var i = 213;
};
foo();
print( i );

var i = 17;
function foo2(){
    i = 213;
};
foo2();
print( i );


function bb( j ){
    for ( var i=0; i<4; i = i + 1 ){
        print( i + " " + j );
    }
}

function a(){
    for ( var i=0; i<4; i = i + 1 ){
        bb( i );
    }
}

a();

for ( var i=0, j=10; i<10; i++ ){
    print( i + "," + j );
    break;
}
