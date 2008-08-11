
db = connect( "test" );
t1 = db.checksum1a;
t2 = db.checksum1b;

t1.drop();
t2.drop();

assert( t1.checksum() == 0 );
assert( t2.checksum() == 0 );

t1.save( { a : 1 } );
t2.save( { a : 1 } );

assert( t1.checksum() != 0 );
assert.eq( t1.checksum() , t2.checksum() );

