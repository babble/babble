
db = connect( "test" );
t = db.ne;
t.drop();

for ( i=1; i<=10; i++ )
    t.save( { num : i } );

for( pass = 0; pass < 2; pass++ ) {
    assert( 9 == t.find( { num : { $ne : 3 } } ).length() );
    assert( 10 == t.find( { zzznum : { $ne : 3 } } ).length() );
    t.ensureIndex( { num : 1 } );
}


assert(t.validate().valid);
