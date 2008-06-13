
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


