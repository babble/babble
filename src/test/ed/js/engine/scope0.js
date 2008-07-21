
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

var a = 1;
var b = 2;

function foo(){
    return a + b;
}

print( "3=" + foo() );
a = 2;
b = 3;
print( "5=" + foo() );

var v = 13;
function bar(){
    v = v + 2;
    return v;
}
print( bar() );
print( bar() );
print( v );

function asdads( a ){
    a = 5;
    var b = a + 2;
    return a + b;
}

    
