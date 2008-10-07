
db = connect( "test" );
t = db.basic2;
t.drop();

o = { n : 2 };
t.save( o );

assert.eq( 2 , t.find( o._id )[0].n );
assert.eq( 2 , t.find( o._id , { n : 1 } )[0].n );

t.remove( o._id );
assert.eq( 0 , t.find().count() );

assert(t.validate().valid);
