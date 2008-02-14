db = connect( "test" );
t = db.t;

t.remove( {} );

t.save( { a : 1 } );
t.save( { a : 2 } );
t.save( { a : 3 } );
t.save( { a : 4 } );

t.ensureIndex( { a : 1 } );

c = t.find().sort( { a : 1 } );
assert( c.next().a == 1 );
assert( c.next().a == 2 );
assert( c.next().a == 3 );
assert( c.next().a == 4 );
assert( ! c.hasNext() );

c = t.find().sort( { a : 1 } ).skip(1);
assert( c.next().a == 2 );
assert( c.next().a == 3 );
assert( c.next().a == 4 );
assert( ! c.hasNext() );

c = t.find().sort( { a : 1 } ).skip(1).limit(1);
assert( c.next().a == 2 );
assert( ! c.hasNext() );

