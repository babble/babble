db = connect( "test" );

a = openFile( "/etc/resolv.conf" );
db._files.save( a );

b = db._files.findOne( a._id );
assert( a.length );
assert( a.length == b.length )

cid = b.next._id;
assert( cid );
assert( db._chunks.findOne( cid ) );

b.remove();
assert( ! db._chunks.findOne( cid ) );
assert( ! db._files.findOne( a._id ) );
