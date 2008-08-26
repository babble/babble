
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

x = new XMLHttpRequest( "GET" , "http://www.10gen.com/~~/headers" );
assert( x.send() );
assert( x.responseText , x.responseText );
assert( x.responseText.match( /Host/ ) );
assert( x.header.match( /Date:/ ) );
assert.eq( "OK" , x.statusText );
assert( x.readyState == 4)



x = new XMLHttpRequest( "GET" , "http://www.10gen.com/~~/headers" );
var last = 0;
x.onreadystatechange = function(){
    last = this.readyState;
}
assert( x.send() );
assert( x.readyState < 4 );

for ( var i=0; i<1000; i++ ){
    if ( last == 4 )
        break;
    sleep( 5 );
}

assert( last == 4 );
