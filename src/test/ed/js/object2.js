
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

o = { a : 1 , b : 2 }
assert( 1 == o.a );
o.__preGet = function( z ){
    if ( z == "a" )
        this[z] = null;
};
assert( null == o.a );


var foo = 1;

function A(){

};

a = new A();
assert( null == a.sillya , a.sillya );

A.prototype.__notFoundHandler = function( name ){
    this[name] = foo++;
    return this[name];
};

assert( 1 == a.sillya );
assert( 1 == a.sillya );

assert( 2 == a.asdasdasd );
