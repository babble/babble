// index6.js Test indexes on array subelements.

function index( q ) {
    assert( q.explain().cursor.match( /^BtreeCursor/ ) );
}

db = connect( "test" );
r = db.ed.db.index5;

r.drop();
r.save( { comments : [ { name : "eliot", foo : 1 } ] } );
r.ensureIndex( { "comments.name": 1 } );
assert( r.findOne( { "comments.name": "eliot" } ) );
index( r.find( { "comments.name": "eliot" } ) );

r.drop();
r.save( { title: "t", comments : [ { name : "eliot", foo : 1 } ] } );
r.ensureIndex( { "comments.name": 1 } );
assert( r.findOne( { "comments.name": "eliot" } ) );
index( r.find( { "comments.name": "eliot" } ) );

r.drop();
r.save( { title: "t", comments : [ { name : "eliot", foo : 1 }, { name : "e", foo : 2 } ] } );
r.ensureIndex( { "comments.name": 1 } );
assert( r.findOne( { "comments.name": "eliot" } ) );
index( r.find( { "comments.name": "eliot" } ) );

r.drop();
r.save( { title: "t", comments : [ { name : "eliot", foo : "o" }, { name : "e", foo : "t" } ] } );
r.ensureIndex( { "comments.name": 1 } );
assert( r.findOne( { "comments.name": "eliot" } ) );
index( r.find( { "comments.name": "eliot" } ) );

r.drop();
r.save( { title: "t", comments : [ { foo : "o", name : "eliot" }, { foo : "t", name : "e" } ] } );
r.save( { title: "z", comments : [ { foo : "v", name : "sam" }, { foo : "q", name : "eliot" } ] } );
r.ensureIndex( { "comments.name": 1 } );
assert( r.findOne( { "comments.name": "eliot" } ) );
index( r.find( { "comments.name": "eliot" } ) );

r.drop();
r.save( { title: "t", comments : [ { foo : "o", name : "eliot" }, { foo : "t", name : "e" } ] } );
r.save( { title: "z", comments : "abc" } );
r.ensureIndex( { "comments.name": 1 } );
assert( r.findOne( { "comments.name": "eliot" } ) );
index( r.find( { "comments.name": "eliot" } ) );

r.drop();
r.save( {"title" : "How the west was won" , "comments" : [{"text" : "great!" , "author" : "sam"},{"text" : "ok" , "author" : "julie"}] , "_id" : "497ce79f1ca9ca6d3efca325"} );
r.ensureIndex( { "comments.author": 1 } );
