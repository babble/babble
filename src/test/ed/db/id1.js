
t = connect( "test" ).id1Test;
t.remove( {} );

id = ObjectId();

t.save( { _id : id , name : "a" } );
t.save( { _id : id , name : "b" } );

assert( t.find().length() == 1 );
assert( t.find()[0].name == "b" );
