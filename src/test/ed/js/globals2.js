
function foo(){
    return a + b();
}

assert.eq( 2 , foo.getGlobals().length );
assert( foo.getGlobals().indexOf( "a" ) >= 0 );
assert( foo.getGlobals().indexOf( "b" ) >= 0 );
