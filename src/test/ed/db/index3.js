
db = connect( "foo");
core.db.db();

t = db.t;
deleteIndexes( "t" );
t.remove( {} );
sleep( 100 );

assert( t.getIndexes().length() == 0 );

t.ensureIndex( { name : 1 } );
sleep( 100 );

t.save( { name : "a" } );

t.ensureIndex( { name : 1 } );
sleep( 1000 );

assert( t.getIndexes().length() == 1 );




