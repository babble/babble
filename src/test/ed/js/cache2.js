
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

stupid = { inc : 0 };

function foo( a ){
    stupid.inc++;
    return a;
}


o = { blah : 1 };

foo.cache( 100000 , o );
assert( 1 == stupid.inc );
foo.cache( 100000 , o );
assert( 1 == stupid.inc );

o.blah = 2;

foo.cache( 100000 , o );
assert( 2 == stupid.inc );
foo.cache( 100000 , o );
assert( 2 == stupid.inc );
