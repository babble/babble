
db = connect( "test" );
ta = db.ref3a;
ta.remove( {} );
tb = db.ref3b;
tb.remove( {} );

// ----

a1 = { name : 1 }
ta.save( a1 );

b = { name : "some b" , as : [ a1 ] }
tb.save( b );

b = tb.findOne( { name : "some b" } );
assert( b );
//print( tojson( b ) );
assert( 1 == b.as.length );
assert( b.as[0].name == 1 );

a1.foo = 17;
ta.save( a1 );
assert( 17 == tb.findOne().as[0].foo );

b.as.add( { name : "some a 2" } );
tb.save( b );

b = tb.findOne( { name : "some b" } );
assert( 2 == b.as.length );

a = ta.findOne();
a.foo = 17;
ta.save( a );

b = tb.findOne( { name : "some b" } );
assert( 2 == b.as.length );
assert( b.as[0].foo == 17 );


