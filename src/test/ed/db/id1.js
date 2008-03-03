
s = "000000000000000000000001";
o = ObjectId(s);

assert( o.toString() == s );
assert( ObjectId(s) == o );

try {
    new ObjectId(s);
    assert( false );
}
catch ( e ){
    
}



t = connect( "test" ).id1Test;
t.remove( {} );

id = ObjectId();

t.save( { _id : id , name : "a" } );
t.save( { _id : id , name : "b" } );

assert( t.find().length() == 1 );
assert( t.find()[0].name == "b" );




