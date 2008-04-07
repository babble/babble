
m = new Map();
m[ { foo : 2 } ] = 3;
assert( 3 == m[ { foo : 2 } ] );

assert( isArray( m.values() ) );
assert( isArray( m.keys() ) );

assert( 1 == m.keys().length );
