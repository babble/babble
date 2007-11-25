a = [ 5 , 6 ];
print( a.filter( function( z ){ return z == 5; } ) );

a.forEach( function( z ){ print( z + 1 ); } );

print( a.every( function( z ){ return z == 5; } ) );
print( a.every( function( z ){ return z >= 5; } ) );

print( a.map( function( z ){ return z + 1; } ) );

print( a.some( function( z ){ return z == 5; } ) );
print( a.some( function( z ){ return z < 5; } ) );
