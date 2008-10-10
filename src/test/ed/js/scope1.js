
var x = 5;
print( scope.keySet() );
assert.eq( scope.keySet().length , 2 );
assert( scope.keySet().contains( "arguments" ) );
assert( scope.keySet().contains( "x" ) );

assert( scope.containsKeyLocalOrGlobal( "x" ) );
assert( scope.containsKeyLocalOrGlobal( "String" ) );

x = null;
assert( scope.containsKeyLocalOrGlobal( "x" ) );

var z = null;
assert( scope.containsKeyLocalOrGlobal( "z" ) );

assert( ! scope.containsKeyLocalOrGlobal( "y" ) );


