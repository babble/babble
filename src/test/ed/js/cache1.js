
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

assert( 0 == stupid.inc );
foo.cache( 100000 )
assert( 1 == stupid.inc , stupid.inc );
foo.cache( 100000 )
assert( 1 == stupid.inc );

foo.cache( 100000 , { name : "asd" } );
assert( 2 == stupid.inc );
foo.cache( 100000 , { name : "asd" } );
assert( 2 == stupid.inc );

foo.cache( 100000 , "nuts" );
assert( 3 == stupid.inc );
foo.cache( 100000 , "nuts" );
assert( 3 == stupid.inc );



foo.cache( 100000 , { name : "asd" , foo : 1 } );
assert( 4 == stupid.inc );
foo.cache( 100000 , { name : "asd" , foo : 1 } );
assert( 4 == stupid.inc );

foo.cache( 100000 , { name : "asd" , foo : 2 } );
assert( 5 == stupid.inc );
foo.cache( 100000 , { name : "asd" , foo : 2 } );
assert( 5 == stupid.inc );


foo.cache( 100000 , { name : "asd" , foo : { z : 1 } } );
assert( 6 == stupid.inc );
foo.cache( 100000 , { name : "asd" , foo : { z : 1 } } );
assert( 6 == stupid.inc );

foo.cache( 100000 , { name : "asd" , foo : { z : 2 } } );
assert( 7 == stupid.inc );
foo.cache( 100000 , { name : "asd" , foo : { z : 2 } } );
assert( 7 == stupid.inc );
