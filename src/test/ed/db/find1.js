db = connect( "test" );
t = db.find1;
t.drop();

t.save( { a : 1 , b : "hi" } );
t.save( { a : 2 , b : "hi" } );

assert( t.findOne( { a : 1 } ).b != null );
assert( t.findOne( { a : 1 } , { a : 1 } ).b == null );

assert( t.find( { a : 1 } )[0].b != null );
assert( t.find( { a : 1 } , { a : 1 } )[0].b == null );

id = t.findOne()._id;

assert( t.findOne( id ) );
assert( t.findOne( id ).a );
assert( t.findOne( id ).b );

assert( t.findOne( id , { a : 1 } ).a );
assert( ! t.findOne( id , { a : 1 } ).b );
