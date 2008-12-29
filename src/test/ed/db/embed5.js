db = connect( "test" );
t = db.embed5;
t.drop();

t.save( { comments : [ { name : "eliot" , foo : 1 } ] } );
assert( t.findOne( { "comments.name" : "eliot"  } ) );

t.save( { comments : [ { name : "bob" , foo : 1 } ] } );
t.ensureIndex( { "comments.name" : 1 } );

assert( "bob" == t.find().sort( { "comments.name" : 1 } )[0].comments[0].name );
assert( "eliot" == t.find().sort( { "comments.name" : -1 } )[0].comments[0].name );

