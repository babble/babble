
db = connect( "test" );
t = db.embeddedIndexTest;

t.remove( {} );

o = { name : "foo" , z : { a : 17 } };
t.save( o );

assert( t.findOne().z.a == 17 );
assert( t.findOne( { z : { a : 17 } } ).z.a == 17 );

t.ensureIndex( { z : { a : 1 } } );

assert( t.findOne().z.a == 17 );
assert( t.findOne( { z : { a : 17 } } ).z.a == 17 );

o = { name : "bar" , z : { a : 18 } };
t.save( o );

print( tojson( t.find() ) );

assert( t.find().length() == 2 );
assert( t.find().sort( { z : { a : 1 } } ).length() == 2 );
assert( t.find().sort( { z : { a : -1 } } ).length() == 2 );

assert( t.find().sort( { z : { a : 1 } } )[0].name == "foo" );
assert( t.find().sort( { z : { a : -1 } } )[1].name == "bar" );
