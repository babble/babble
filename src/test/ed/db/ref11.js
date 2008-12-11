db = connect( "test" );

a = db.ref11a;
b = db.ref11b;

a.drop();
b.drop();

theA = { a : 1 , b : 2 };

a.save( theA );
assert.eq( 1 , a.find().length() );

theACopy = a.findOne( {} , { b : 1 } );
theACopy.b = 3;

b.save( { c : 1 , d : theACopy } );
assert.eq( 1 , b.find().length() );

assert.eq( 2 , b.findOne().d.b );

