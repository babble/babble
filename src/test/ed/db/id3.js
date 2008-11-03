db = connect( "test" );
t = db.id3;
t.drop();

t.save( { a : 1 } );
assert( t.findOne() );
assert( ! t.findOne( { a : null } ) );
assert( ! t.findOne( { _id : null } ) );
