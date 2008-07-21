
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

function Base(){
    
};

Base.prototype.setName = function( name ){
    this.name = name;
};

// ----

function A(){

};
A.prototype = new Base();
A.prototype.setName( "A" );
print( A.name );

a = new A();
print( a.name );

// ----

function B(){

};
B.prototype = new Base();
B.prototype.setName( "B" );

b = new B();
print( b.name );

a = new A();
print( a.name );
