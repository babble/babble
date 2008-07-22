// ref8.js

db = connect( "test" );
t = db.ref8;

t.remove( {} );

t.save( { a : 1 , b : 2 } );

assert( 1 == t.find( t.findOne() ).sort( { a : 1 } ).toArray().length );

