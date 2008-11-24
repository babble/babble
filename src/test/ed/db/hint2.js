
db = connect( "test" );
t = db.hint2;
t.drop();

for ( i=0; i<100; i++ )
    t.save( { num : i , foo : i } );

t.ensureIndex( { num : 1 } );

assert( t.find( { num : 17 } ).explain().nscanned <= 3 );
assert.eq( t.find( { num : 17 , foo : 17  } ).explain().nscanned , 100 , "maybe dwight fixed the query optimizer" );

t.setHintFields( [ { num : 1 } ] );
assert.lt( t.find( { num : 17 , foo : 17  } ).explain().nscanned , 3 );
