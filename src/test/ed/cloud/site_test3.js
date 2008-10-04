
u = { name : "foo" , _id : ObjectId() , _ns : "asd" };
assert.eq( 3 , u.keySet().length );

n = Cloud.Site.prototype._copyUser( u );
assert.eq( 1 , n.keySet().length );
assert( n.name );
assert( ! n._id );

assert.eq( 3 , u.keySet().length );
