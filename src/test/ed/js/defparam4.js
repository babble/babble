
function foo( a , b = 5 ){
    return a + b;
}

assert( 6 == foo( 1 ) );
assert( 3 == foo( 1 , 2 ) );
assert( 3 == foo( 1 , 2 , 3 ) );
