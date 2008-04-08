
m = new Map();

m[ { foo : 2 } ] = 3;
assert( 3 == m[ { foo : 2 } ] );

m[ "asd" ] = 1.1;
assert( 1.1 == m.asd );

assert( isArray( m.values() ) );
assert( isArray( m.keys() ) );

assert( 2 == m.keys().length );
assert( 2 == m.values().length );
