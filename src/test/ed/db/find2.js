// Test object id find.

function testObjectIdFind( db ) {
    r = db.ed_db_find2_oif;
    r.drop();

    z = [ {}, {}, {} ];
    for( i = 0; i < z.length; ++i )
	r.save( z[ i ] );
    
    center = r.find( { _id: z[ 1 ]._id } );
    assert.eq( 1, center.count() );
    assert.eq( z[ 1 ]._id, center[ 0 ]._id );

    left = r.find( { _id: { $lt: z[ 1 ]._id } } );
    assert.eq( 1, left.count() );
    assert.eq( z[ 0 ]._id, left[ 0 ]._id );

    right = r.find( { _id: { $gt: z[ 1 ]._id } } );
    assert.eq( 1, right.count() );
    assert.eq( z[ 2 ]._id, right[ 0 ]._id );
}

db = connect( "test" );
testObjectIdFind( db );