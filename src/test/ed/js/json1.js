
assert( tojson( { a : true , b : true }));

assert( { a : true }.toJSON().contains( "true" ) );
assert.eq( { a : true }.toJSON() , { a : true , b : function(){} }.toJSON() );
