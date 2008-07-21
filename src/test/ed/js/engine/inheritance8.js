
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

A = function(){
    
}

A.prototype.foo = function(){
    return "A.foo";
}

A.prototype.fun = function(){
    return "A";
}

B = function(){
    
}

B.prototype.bar = function(){
    return "B.bar"
}

B.prototype.fun = function(){
    return "B";
}

B.prototype.__proto__ = A.prototype;


b = new B();
print( b.bar() );
print( b.foo() );
print( b.fun() );

print( "---" );

function A() { }
A.prototype.myMethod = function() { return "A"; }

function B() { }
B.prototype.myMethod = function() { return "B"; }

var a = new A();

a.__proto__ = B.prototype;

print( a.myMethod() );
