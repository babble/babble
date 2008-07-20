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

var s = "asdasd";

print( s.charCodeAt( 1 ) );
print( s.charAt( 1 ) );
print( s.substring( 1 ) );
print( s.substring( 1 , 2 ) );
print( s.indexOf( "s" ) );
print( s.indexOf( "s" , 3 ) );
print( s.lastIndexOf( "s" ) );
print( s.lastIndexOf( "s" , s.length ) );

for ( var i=0; i<s.length; i++ )
    print( s.lastIndexOf( "s" , s.length - i ) );


print( "a=b".split( "=" ).length );
print( "a=b".split( "=" )[0] );
print( "a=b".split( "=" )[1] );

print( "a=".split( "=" ).length );
print( "a=".split( "=" )[0] );
print( "a=".split( "=" )[1] );
