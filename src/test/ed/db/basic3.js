
ok = false;

try{
    connect( "test" ).foo.save( { "a.b" : 5 } );
    ok = false;
}
catch ( e ){
    ok = true;
}
assert( ok , ". in names aren't allowed doesn't work" );

connect( "test" ).foo.find( { "a.b" : 1 } ).length();
