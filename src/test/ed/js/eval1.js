
a = 1;
b = 2;

assert( 3 == scope.eval( "a + b" ) );

a = { b : 2 };
assert( 2 == scope.eval( "a.b" ) );

a = { b : { c : 3 } };
assert( 3 == scope.eval( "a.b.c" ) );


scope.eval( "x = 5" );
assert( 5 == x );

var y = 5;
assert( 5 == y );
scope.eval( "y =6;" );
assert( 6 == y );

