
__path__._once1a();
assert( blah );
try {
    __path__._once1a();
    assert( "shouldn't be here" );
}
catch( e ) {
    
}

__path__._once1b.load();
assert( blah2 );
__path__._once1b.load();

