// index6.js Test indexes on array subelements.

db = connect( "test" );
r = db.ed.db.index5;
r.drop();

r.save( { comments : [ { name : "eliot", foo : 1 } ] } );
r.ensureIndex( { "comments.name": 1 } );
assert( r.findOne( { "comments.name": "eliot" } ) );
