function foo(){
    return 5;
}
print( foo() );

function foo2( a ){
    return a;
}
print( foo2( 4 ) );

function foo3( a ){
    a = a + 1;
    return a;
}
print( foo3( 9 ) );


function foo4( a ){
    b = a + 1;
    return b;
}
print( foo4( 10 ) );


function foo5( a ){
    a = a + 1;
    var c = a + 2;
    c = c + 1 ;
    return c;
}
print( foo5( 10 ) );


function foo6( a ){
    var c = a + 2;
    c = c + 1;
    return c;
}
print( foo6( 13 ) );

