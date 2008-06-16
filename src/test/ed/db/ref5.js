
db = connect( "test" );
ta = db.ref5a;
ta.remove( {} );
tb = db.ref5b;
tb.remove( {} );

// ----

a = { name : "some a" };
ta.save( a );

b = { name : "some b" , mya : a };
tb.save( b );

a = { name : "some a 2" };
ta.save( a );

b = { name : "some b 2" , mya : a };
tb.save( b );

assert( 1 == tb.find( { mya : a } ).length() );
assert( 2 == tb.find().length() );


