
a = 1;

function myPackage(){
    a = 5;
    var b = 6;
}

it = createPackage( myPackage );
assert.eq( 1 , a );
assert.eq( 5 , it.a );
assert( ! it.b );
assert.eq( 1 , it.keySet().length );

it2 = createPackage( __path__._package1 );
assert.eq( 9 , it2.a );
assert( ! it2.b );

// sanity
assert.eq( 1 , a );
assert.eq( 5 , it.a );
assert( ! it.b );
assert.eq( 1 , it.keySet().length );
