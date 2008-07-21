
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

m = new Map();

m[ { foo : 2 } ] = 3;
assert( 3 == m[ { foo : 2 } ] );

m[ "asd" ] = 1.1;
assert( 1.1 == m.asd );

assert( isArray( m.values() ) );
assert( isArray( m.keys() ) );

assert( 2 == m.keys().length );
assert( 2 == m.values().length );
