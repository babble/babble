
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

function A( name ){
    print( "A" );
    this.name = name;
};


function B( name ){
    this.__proto__.constructor.apply( this , arguments );
    print( "B" );
};

B.prototype = new A();

B.prototype.foo = 17;

b = new B( "eliot" );

print( b.foo );
print( b.name );
