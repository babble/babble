
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

// testing POS
// i have NO idea what its supposed to do...
print( 5 + + 6 );

a = 5;
b = 3;

print( a + + b );
print( a );
print( b );

print( + b );
b = -7;
print( b );
print( + b );
print( b );
print( + -7 );

if ( m = 3 )
    print( "Z1" );

if ( m = null )
    print( "Z2" );

print( "Z3" );
