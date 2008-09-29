
__path__._once1a();
assert( blah );
try {
    __path__._once1a();
    assert( "shouldn't be here" );
}
catch( e ) {
    
}

__path__._once1b.loadOnce();
assert( blah2 );
__path__._once1b.loadOnce();

__path__._once1c();
assert( blah3 );
__path__._once1c();

