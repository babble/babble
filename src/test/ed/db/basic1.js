
db = connect( "test" );
t = db.basic1;
t.drop();

o = { a : 1 };
t.save( o );

assert.eq( 1 , t.findOne().a );

o.a = 2;
o._save();

assert.eq( 2 , t.findOne().a );
