db = connect( "test" );
t = db.in3;
t.drop();

t.save( { a : 1 } );
t.save( { a : 2 } );

all = [];
t.find().forEach( function(z){ all.add( z._id ); } );

assert.eq( 2 , t.find().toArray().length , "normal" );
assert.eq( 2 , t.find( { _id : { $in : all } } ).toArray().length , "in" );

try {
    t.save( { _id : 5 } );
    assert( 0 , "blah" );
}
catch ( e ){
}

t.drop();
id = ObjectId();
t.save( { _id : id , n : 17 } );
assert.eq( id , t.findOne()._id );
