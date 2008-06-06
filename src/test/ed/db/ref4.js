
db = connect( "test" );
ta = db.ref3a;
ta.remove( {} );
tb = db.ref3b;
tb.remove( {} );

// ----

a1 = { name : "some a" }
ta.save( a1 );

b = { name : "some b" , a1 : a1 }
tb.save( b );

b = tb.findOne( { name : "some b" } );
assert( b.a1.name == "some a" );

a2 = { name : "some a 2" };
ta.save( a2 );
b.a2 = a2;
ta.save( b.a2 );
tb.save( b );

a1.foo = 17;
ta.save( a1 );

assert( tb.findOne().a1.foo == 17 );


