db = connect( "test" );
t = db.null2;
t.drop();

t.save( { a : 1 , b : 2 } )
t.save( { a : 1 , b : null } )
t.save( { a : 1 } );

assert.eq( 3 , t.find( { a : 1 } ).length() )
assert.eq( 2 , t.find( { a : 1 , b : null } ).length() )
assert.eq( 2 , t.find( { b : null } ).length() )
