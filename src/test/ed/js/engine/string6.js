
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

print( "\70" );
print( "\4".length );


print( /\64/.test( "4" ) );


A = function(){
    this.z = 11;
};

A.prototype.toString = function(){
    return "hi " + this.z;
};

a = new A();
print( a );


print(String.fromCharCode(65));

print(String.fromCharCode(65, 69));

print(typeof String.fromCharCode());
print(String.fromCharCode().length);

print( "asdfasdf".split("d").length );
print( "asdfasdf".split("d")[0] );

print( "asdfasdf".split("d",1).length );
print( "asdfasdf".split("d",1)[0] );

print( "abc".replace( /a/ , null ) );

print( "foo.foo.com".split( "." ) );
print( "foo.foo.com".split( /\./ ) );

print( "foo.foo.com".replace( "." , "Z" )  );

print( "abc.rb".replace( ".rb$" , "" ) );
