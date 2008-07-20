
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

var a = 3;
var b = 4;

function foo( a , b ){
    var v = function(){
        return a + b;
    }
    return v;
}

function foo2( a , b ){
    var v = function(){
        print( c );
        return a + b;
    }
    a = 100;
    return v;
}



var f = foo( a , b );
var f2 = foo2( a , b );

a = 4; b = 5;

var c = 2;

print( "7 = " + f() );
print( "104 = " + f2() );
