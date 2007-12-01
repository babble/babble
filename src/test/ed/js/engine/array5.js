a = [ 5 , 6 ];
print( a.filter( function( z ){ return z == 5; } ) );

a.forEach( function( z ){ print( z + 1 ); } );

print( a.every( function( z ){ return z == 5; } ) );
print( a.every( function( z ){ return z >= 5; } ) );

print( a.map( function( z ){ return z + 1; } ) );

print( a.some( function( z ){ return z == 5; } ) );
print( a.some( function( z ){ return z < 5; } ) );

print( a.sort() );
print( [ 6 , 5 ].sort() );
print( [ 6 , 5 ].sort() );

print( [ 6 , 123 , 1239 ,1231 ,129 , -123 , 91012 , -12 , 5 ].sort() );

print( [ 6 , 123 , 1239 ,1231 ,129 , -123 , 91012 , -12 , 5 ].sort( function( l , r ){ return l - r; } ) );
print( [ 6 , 123 , 1239 ,1231 ,129 , -123 , 91012 , -12 , 5 ].sort( function( l , r ){ return r - l; } ) );
