
db = connect( "test" );
t = db.range2;
t.drop();

for ( i=1; i<=10; i++ )
    t.save( { num : i } );

for( pass = 0; pass < 2; pass++ ) {
    assert( 1 == t.find( { num : { $gt : 5 , $lt : 7 } } ).length() );
    t.ensureIndex( { num : 1 } );
}


