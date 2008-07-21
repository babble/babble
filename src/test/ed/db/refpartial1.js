
db = connect( "test" );

ta = db.ref7a;
tb = db.ref7b;

ta.drop();
tb.drop();

ta.save( { a : 1 , b : 1 } );

b = { a : 1 , thing : ta.findOne( {} , { a : 1 } ) };
tb.save( b );

