
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

function zzz( a ){
    return 1 + a;
};

function xxx(){
    return zzz( 1 );
};

assert( 2 == xxx() );

xxx.getScope( true )["zzz"] = function( a ){
    return 2 + a;
};
assert( 3 == xxx() );

xxx.clearScope();
assert( 2 == xxx() );

now = new Date();

function other(){
    while ( (new Date()).getTime() - now.getTime() < 200 ){
        xxx.getScope( true )["zzz"] = function( a ){
            return 2 + a;
        };
        assert( 3 == xxx() );
        
        xxx.clearScope();
        assert( 2 == xxx() );
    }
};

t = new Array();
for ( i=0; i<3; i++ )
    t[i] = fork( other );

t.forEach( function(z){ z.start(); } );
t.forEach( function(z){ z.join(); } );


buf = "";
function foo(){
    print( "eliot" );
};

foo.getScope( true ).print = function( s ){
    buf += s;
};

foo();

assert( "eliot" == buf );
