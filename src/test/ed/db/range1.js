
db = connect( "test" );
t = db.range1;

t.drop();

for ( i=1; i<=10; i++ )
    t.save( { num : i } );

t.ensureIndex( { num : 1 } );

assert( t.find().sort( { num : 1 } ).toArray()[0].num == 1 );
assert( t.find().sort( { num : -1 } ).toArray()[0].num == 10 );

assert( 10 == t.find().toArray().length );
assert( 5 == t.find( { num : { $gt : 5 } } ).toArray().length );
assert( 4 == t.find( { num : { $lt : 5 } } ).toArray().length );

assert( 1 == t.find( { num : { $gt : 5 , $lt : 7 } } ).toArray().length );


