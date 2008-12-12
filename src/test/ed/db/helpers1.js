db = connect( "test" );

a = db.helpers1a;
b = db.helpers1b;

a.drop();
b.drop();

a.save( { a : 1 } );
a.save( { a : 2 } );

res = a.recoverTo( "helpers1b" );
assert( res.total == a.count() );
assert.eq( 0 , res.errors );

assert.eq( a.count() , b.count() );
