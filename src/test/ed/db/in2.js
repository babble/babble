// in2.js

db=connect("test");
t=db.in2;
t.drop();

t.save( { a : 1 , n : 1 } );
t.save( { a : 2 , n : 2 } );
t.save( { a : 3 , n : 3 } );
t.save( { a : 4 , n : 4 } );

assert.eq( 4 , t.find().toArray().length );
assert.eq( 2 , t.find( { n : { $in : [ 1 , 2 ] } } ).toArray().length );

t.save( { a : 5 } );
// this next line fails
//assert.eq( 2 , t.find( { n : { $in : [ 1 , 2 ] } } ).toArray().length );

