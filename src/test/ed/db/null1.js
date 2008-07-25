db = connect( "test" );
t = db.null1;
t.drop();

t.save( { a : 1 } );
t.save( { a : 1  , b : 2 } );

assert( t.find().toArray().length == 2 );
assert( t.find( { a : 1 } ).toArray().length == 2 );
assert( t.find( { a : 1 , b : null } ).toArray().length == 1 );
