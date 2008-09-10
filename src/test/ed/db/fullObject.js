db = connect( "test" );
t = db.fullObject1;
t.drop();

t.save( { a : 1 , n : 1 } );
t.save( { a : 2 , n : 2 } );
t.save( { a : 3 , n : 3 } );
t.save( { a : 4 , n : 4 } );

assert.eq( 4 , t.find().toArray().length );

assert.eq( 1 , t.find( { a : 2 } ).toArray().length );
assert.eq( 2 , t.find( { a : 2 } )[0].n );

assert.eq( 1 , t.find( { $where : function(){ return obj.a == 2 } } ).toArray().length );
assert.eq( 2 , t.find( { $where : function(){ return obj.a == 2 } } )[0].n );

