
db = connect( "test" );
t = db.updateTest;
t.remove( {} );

o = { name : "foo" , z : 17 , num : 0 };
t.save( o );
t.ensureIndex( { name : 1 } );
assert( t.findOne( { name : "foo" } ).z == 17 );

t.update( { name : "foo" } , { $inc : { num : 1 } } , { ids : false }  );

assert( t.findOne( { name : "foo" }  ) );
assert( t.findOne( { name : "foo" } ).z == 17 );

