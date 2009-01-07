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

function foo(a, b, c){
    return arguments.length;
};

print( foo( 1 , 2 , 3 , null ) );

print( foo( null ) );
print( foo( null , 5 ) );
print( foo( null , 5 , null ) );

// make sure x is compiled as an object, not a double
function f() {
    var x = 0;
    x += arguments[0];
    return x;
}

print( f(3) );
