
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


