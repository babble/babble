
function foo( a , b ){
    return a + b;
}

assert.eq( 5 , foo( 3 , 2 ) );

assert.eq( 5 , foo.apply( null , [ 3 , 2 ] , null ) );
assert.eq( 5 , foo.apply( null , [ 3 , 2 ] , {} ) );
assert.eq( 5 , foo.apply( null , null , { a : 3 , b : 2 } ) );
assert.eq( 5 , foo.apply( null , [ 3 ] , { b : 2 } ) );
assert.raises( 
    function(){
        foo.apply( null , [ 3 ] , { a : 1 , b : 2 } );
    }
);
