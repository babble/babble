
o = { a : 1 , b : 2 }
assert( 1 == o.a );
o.__preGet = function( z ){
    if ( z == "a" )
        this[z] = null;
};
assert( null == o.a );

