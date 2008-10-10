
var x = 5;
assert.eq( scope.keySet().length , 2 );
assert.eq( scope.keySet( false ).length , 2 );
assert( scope.keySet( true ).length > 2 )

assert( scope.keySet().contains( "arguments" ) );
assert( scope.keySet().contains( "x" ) );

assert( scope.containsKeyLocalOrGlobal( "x" ) );
assert( scope.containsKeyLocalOrGlobal( "String" ) );

x = null;
assert( scope.containsKeyLocalOrGlobal( "x" ) );

var z = null;
assert( scope.containsKeyLocalOrGlobal( "z" ) );

assert( ! scope.containsKeyLocalOrGlobal( "y" ) );


assert( scope.containsKey( "x" ) );
assert( scope.containsKey( "z" ) );
assert( scope.containsKey( "x" , false ) );
assert( scope.containsKey( "x" , true ) );

assert( scope.containsKey( "String" , true ) );
assert( ! scope.containsKey( "String" , false ) );
assert( ! scope.containsKey( "String" ) );

