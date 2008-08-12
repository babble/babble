
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

foo = function(){
    this.a = 4;
}


foo.prototype = {
    get b(){
        return this.a+1; 
    }
};

foo.prototype.__defineGetter__( "c" , 
                                function() { 
                                    return this.a+1; 
                                }
                              );

foo.prototype.__defineSetter__( "c" , 
                                function( z ) { 
                                    return this.c = z;
                                }
                              );

f = new foo();

print( f.a );
print( f.b );
print( f.c );

print( foo.prototype.__lookupSetter__( "c" ) == null );
print( foo.prototype.__lookupSetter__( "d" ) == null );

print( foo.prototype.__lookupGetter__( "b" ) == null );
print( foo.prototype.__lookupGetter__( "e" ) == null );
