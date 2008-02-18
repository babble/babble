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

print( [ 6 , 3 , 2 , 3 , 3 ].indexOf( 3 ) );
print( [ 6 , 3 , 2 , 3 , 3 ].indexOf( 3 , 2 ) );
print( [ 6 , 3 , 2 , 3 , 3 , 1 ].indexOf( 3 , 10 ) );



print( [ 1 , 2 , 3 , 4 , 5 ].splice( 2 ) );
print( [ 1 , 2 , 3 , 4 , 5 ].splice( 2 , 1 ) );
print( [ 1 , 2 , 3 , 4 , 5 ].splice( 2 , 1 , "a" ) );

a = [ 1 , 2 , 3 , 4 , 5 ];
print( a.splice( 2 ) );
print( a );
print( [ 1 , 2 , 3 , 4 , 5 ].splice( 2 , 1 ) );
print( [ 1 , 2 , 3 , 4 , 5 ].splice( 2 , 1 , "a" ) );


a = [ 1 , 2 , 3 , 4 , 5 ];
print( a.splice( 2 , 2 , "a" , "b" ) );
print( a );

a = [ 1 , 2 ];
print( a );
b = [ 3 ];
print( a.concat( b ) );
print( a );
print( b );
print( a.concat( b ).length );



print( [ 6 , 3 , 2 , 3 , 3 ].lastIndexOf( 3 ) );
print( [ 6 , 3 , 2 , 3 , 3 ].lastIndexOf( 3 , 2 ) );
print( [ 6 , 3 , 2 , 3 , 3 , 1 ].lastIndexOf( 3 , 10 ) );

print( [ 6 , 3 , 2 , 3 , 3 ].join() );
print( [ 6 , 3 , 2 , 3 , 3 ].join( "-") );

a = [ 1 , 2 , 3 ];
print( a.reverse() );
print( a );

print( a.pop() );
print( a );

a = [];
print( a.unshift( 5 ) );
print( a.unshift( 7 ) );
print( a );
print( a.shift() );
print( a );
print( a.shift() );
print( a );

a.unshift( 1 );
print( a.unshift( 7 , 2 , 3) );
print( a );

print( [ 1 , 2 , 3, 4 , 5 , 6 ].slice( 1 ) );
print( [ 1 , 2 , 3, 4 , 5 , 6 ].slice( 1 , 1 ) );
print( [ 1 , 2 , 3, 4 , 5 , 6 ].slice( 1 , 3 ) );
print( [ 1 , 2 , 3, 4 , 5 , 6 ].slice( 1 , -1 ) );

print( [ 1 , 2 ].toSource().replace( /\s+/g , "" ) );

print( [ 1 , 2 ].valueOf().length );
a = [ 1 , 2 ];
print( a === a.valueOf() );

/* uncomment when using spider monkey 1.8
print( [ 1 , 2 , 3 , 4  , 5  ].reduce( function( a , b ){
            if ( ! a )
                a = 0;
            return a + b;
        } ) );

print( [ 1 , 2 , 3 , 4  , 5  ].reduce( function( a , b ){
            if ( ! a )
                a = 0;
            return a + b;
        } , 2 ) );

print( [ 1 , 2 , 3 , 4  , 5  ].reduce( function( a , b ){
            return " " + a + " " + b;
        }  ) );

print( [ 1 , 2 , 3 , 4  , 5  ].reduceRight( function( a , b ){
            return " " + a + " " + b;
        }  ) );


print( [ 1 , 2 , 3 , 4  , 5  ].reduce( function( a , b ){
            return " " + a + " " + b;
        } , "Z" ) );

print( [ 1 , 2 , 3 , 4  , 5  ].reduceRight( function( a , b ){
            return " " + a + " " + b;
        } , "Z" ) );

*/
