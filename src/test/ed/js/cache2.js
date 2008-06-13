
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


