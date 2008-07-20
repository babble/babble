
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

print( [] && 0 )
print( "a" && 0 )

print( 0 && true );

a = 0;
b = true;

print( a && b );

print( b && a );
print( a || b );
print( b || a );

print( [] && 4 );
print( [] && -1 );
print( [] && 0 );
print( [] && false );


print( [] || 4 );
print( [] || -1 );
print( [] || 0 );
print( [] || false );
