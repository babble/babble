
db = connect( "foo" , "127.0.0.1" );
t = db.jni3;

for( z = 0; z < 2; z++ ) {
    print(z);

t.drop();

if( z > 0 ) {
    t.ensureIndex({_id:ObjId()});
    t.ensureIndex({i:1});
}

for( i = 0; i < 1000; i++ )
    t.save( { i:i, z: "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" } );

assert( 33 == db.dbEval(function() { return 33; } ) );

	db.dbEval( function() { db.jni3.save({i:-1, z:"server side"}) } );

assert( db.jni3.findOne({i:-1}) );

assert( 2 == t.find( { $where : 
                       function(){ 
                           return obj.i == 7 || obj.i == 8;
                       } 
                     } ).length() );


// NPE test
assert( 0 == t.find( { $where : 
                       function(){ 
                           asdf.asdf.f.s.s();
                       } 
                     } ).length() );


t.ensureIndex({z:1});
t.ensureIndex({q:1});

assert( 2 == t.find( { $where : 
                       function(){ 
                           return obj.i == 7 || obj.i == 8;
                       } 
                     } ).length() );

for( i = 1000; i < 2000; i++ )
    t.save( { i:i, z: "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" } );

assert( t.find().count() == 2001 );

assert( t.validate().valid );

}
