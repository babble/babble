
t1 = connect("testa").checksum1;
t2 = connect("testb").checksum1;

t1.drop();
t2.drop();

assert( t1.checksum() == 0 );
assert( t2.checksum() == 0 );

foo = t1.save( { a : 1 } );
t2.save( { _id : foo._id , a : 1 } );

assert( t1.checksum() != 0 );
assert.eq( t1.checksum() , t2.checksum() );

