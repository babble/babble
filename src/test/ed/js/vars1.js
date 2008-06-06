
function blah( a ){
    scope["a"] = a + 1;
    print( arguments.length )
    return a;
}

a = 1;
assert( a == 1 )
b = blah(4)
assert( 5 == b , b );
assert( a == 1 )
