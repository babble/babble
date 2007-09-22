function foo(){
    return 5;
}
SYSOUT( foo() );

function foo2( a ){
    return a;
}
SYSOUT( foo2( 4 ) );

function foo3( a ){
    a = a + 1;
    return a;
}
SYSOUT( foo3( 9 ) );


function foo4( a ){
    b = a + 1;
    return b;
}
SYSOUT( foo4( 10 ) );

/*
function foo5( a ){
    a = a + 1;
    var c = a + 2;
    c = c + 1 ;
    return c;
}
SYSOUT( foo5( 10 ) );
*/

/*
function foo6( a ){
    var c = a + 2;
    c = c + 1;
    return c;
}
SYSOUT( foo6( 13 ) );
*/
