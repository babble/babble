
db = connect( "test" );
t = db.where3;
t.drop();

t.save( { a : 1 } );
t.save( { a : 2 } );
t.save( { a : 3 } );

t.ensureIndex( { a : 1 } );

assert.eq( 3 , t.find().length() );

assert.eq( 2 , t.find( { a : { $gt : 1 } } ).length() );
assert.eq( 1 , t.find( { a : { $gt : 2 } } ).length() );
assert.eq( 0 , t.find( { a : { $gt : 3 } } ).length() );

assert.eq( 2 , t.find( { a : { $lt : 3 } } ).length() );
assert.eq( 1 , t.find( { a : { $lt : 2 } } ).length() );
assert.eq( 0 , t.find( { a : { $lt : 1 } } ).length() );
        
assert.eq( 1 , t.find( { a : { $lt : 3 , $gt : 1 } } ).length() );
assert.eq( 2 , t.find( { a : { $lt : 3 , $gt : 1 } } )[0].a );

// ------

t.drop();

t.save( { a : new Date( 1 ) } );
t.save( { a : new Date( 2 ) } );
t.save( { a : new Date( 3 ) } );

assert.eq( 3 , t.find().length() );

assert.eq( 2 , t.find( { a : { $gt : new Date( 1 ) } } ).length() );
assert.eq( 1 , t.find( { a : { $gt : new Date( 2 ) } } ).length() );
assert.eq( 0 , t.find( { a : { $gt : new Date( 3 ) } } ).length() );

assert.eq( 2 , t.find( { a : { $lt : new Date( 3 ) } } ).length() );
assert.eq( 1 , t.find( { a : { $lt : new Date( 2 ) } } ).length() );
assert.eq( 0 , t.find( { a : { $lt : new Date( 1  )} } ).length() );
        
assert.eq( 1 , t.find( { a : { $lt : new Date( 3 ) , $gt : new Date( 1 ) } } ).length() );
assert.eq( new Date( 2 ) , t.find( { a : { $lt : new Date( 3 ) , $gt : new Date( 1 ) } } )[0].a );
