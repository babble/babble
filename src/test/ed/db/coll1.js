// coll1.js

db = connect( "test" );
a = db.coll1a;
b = db.coll1b;

a.drop();
b.drop();

a.save( { other : b } );
b.save( { num : 1 } );

assert.eq( 1 , a.findOne().other.findOne().num );
