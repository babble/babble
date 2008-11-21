
db = connect( "test" );
t = db.hint1;
t.drop();

for ( i=0; i<100; i++ )
    t.save( { num : i , foo : i } );

t.ensureIndex( { num : 1 } );

assert( t.find( { num : 17 } ).explain().nscanned <= 3 );
assert.eq( t.find( { num : 17 , foo : 17  } ).explain().nscanned , 100 , "maybe dwight fixed the query optimizer" );
assert( t.find( { num : 17 , foo : 17 } ).hint( { num : 1 } ).explain().nscanned <= 3 );
assert( t.find( { num : 17 , foo : 17 } ).hint( "num_1" ).explain().nscanned <= 3 );

assert.eq( 1 , t.find( { num : 17 , foo : 17 } ).hint( "num_1" ).toArray().length );
assert.eq( 17 , t.find( { num : 17 , foo : 17 } ).hint( "num_1" ).toArray()[0].num );

