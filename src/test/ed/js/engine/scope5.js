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

a = 2;
function foo(){
    var a;
    a = 5;
    return {};
}
print(a); 
foo();
print(a); 

a = 2;
function foo2(){
    var a;
    a = 5;
    return function(){};
}
print(a); 
foo2();
print(a); 


a = 2;
function foo3(){
    var a;
    print( a ? "y" : "n" );
    a = 5;
    print( a ? "y" : "n" );
    return function(){};
}
print(a); // output: 2
foo3();
print(a); // output: 5
