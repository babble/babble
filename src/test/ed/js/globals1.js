
a = 1;

function foo( b ){
    var c = 3;
    d = 4;
    
    return a + b + c + d;
}

assert.eq( foo(2) , 10 );
assert.eq( 2, foo.getGlobals().length );
assert( foo.getGlobals().indexOf( "a" ) >= 0 );
assert( foo.getGlobals().indexOf( "d" ) >= 0 );
assert( foo.getGlobals().indexOf( "b" ) < 0 );
assert( foo.getGlobals().indexOf( "c" ) < 0 );
