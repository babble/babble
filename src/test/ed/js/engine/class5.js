
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

function Person( name ){
    this._name = name;
}

Person.prototype.something = function( a ){
    print( a )
};

Person.prototype.printName = function(){
    print( this._name );
}

var p = new Person( "eliot" );


Person.prototype.a = "before";
print( Person.prototype.a );
print( p.a );


Person.prototype.a = "after";
print( p.a );

print( p._name );
p.something("yo");

p.printName();

p.foo = function() { 
    print( this._name );
};
p.foo();


a = {};
a.a = {};
a.a.b = function(){
    this.z = 1;
};

foo = new a.a.b();
print( foo.z );


function Post() {
    this.x = 5;
};

ns = {Post: Post};

p1 = new Post();
p2 = new ns.Post();
print(p1.x);
print(p2.x);
